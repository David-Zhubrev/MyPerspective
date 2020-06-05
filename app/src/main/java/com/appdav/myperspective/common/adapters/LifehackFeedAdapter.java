package com.appdav.myperspective.common.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.LifehackData;
import com.appdav.myperspective.common.views.LikeButton;
import com.appdav.myperspective.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class LifehackFeedAdapter extends RecyclerView.Adapter<LifehackFeedAdapter.LifehackViewHolder> {

    public enum ToBeLiked {Liked, Disliked}

    public interface LifehackFeedAdapterClickListener {
        void onLikeButtonClicked(String lifehackId, ToBeLiked toBeLiked);

        void onProfilePicClicked(String uId);

        void onDeleteLifehackClicked(String lifehackId);
    }

    private RecyclerView recyclerView;

    private LifehackFeedAdapterClickListener listener;
    private ArrayList<LifehackData> lifehacks;
    private Picasso picasso;
    private String currentUserId;

    public LifehackFeedAdapter(String currentUserId, ArrayList<LifehackData> lifehacks, LifehackFeedAdapterClickListener listener) {
        this.listener = listener;
        this.lifehacks = lifehacks;
        this.currentUserId = currentUserId;
        picasso = DaggerServiceComponent.builder().build().getPicasso();
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void setLifehackArray(ArrayList<LifehackData> lifehacks) {
        this.lifehacks = lifehacks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LifehackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lifehack_feed, parent, false);
        return new LifehackViewHolder(v);
    }

    public void updateItem(int index) {
        LifehackViewHolder vh = (LifehackViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
        if (vh != null) {
            vh.setLikeCounterText(lifehacks.get(index).getLikes() == null ? 0 : lifehacks.get(index).getLikes().size());
            vh.likeButton.initialize(lifehacks.get(index).isLikedByCurrentUser(), vh.clickedByUser);
            if (vh.clickedByUser) vh.clickedByUser = false;
        }
    }

    public void deleteItem(int index) {
        LifehackFeedAdapter.this.notifyItemRemoved(index);
    }

    @Override
    public void onBindViewHolder(@NonNull LifehackViewHolder holder, int position) {
        LifehackData lifehackData = lifehacks.get(position);
        if (lifehackData.getPhotoUri() != null) {
            String photoUri;
            if (lifehackData.getPhotoUri().isEmpty()) photoUri = Constants.DEFAULT_USER_PIC_URL;
            else photoUri = lifehackData.getPhotoUri();
            picasso.load(photoUri).centerCrop().resize(100, 100)
                    .into(holder.ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.loader.setVisibility(View.GONE);
                            picasso.load(photoUri).centerCrop().fit().placeholder(holder.ivPhoto.getDrawable())
                                    .into(holder.ivPhoto);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        }
        if (lifehackData.getAuthorUid().equals(currentUserId) || currentUserId.equals(Constants.USER_HAS_EDITOR_RIGHTS)) {
            holder.enableMenuButton(lifehackData.getLifehackId());
        } else holder.menu.setVisibility(View.GONE);
        holder.tvAuthor.setText(lifehackData.getAuthorText());
        holder.tvContent.setText(lifehackData.getContent());
        holder.setLikeCounterText(lifehackData.getLikes() == null ? 0 : lifehackData.getLikes().size());
        holder.likeButton.initialize(lifehackData.isLikedByCurrentUser(), false);
        holder.ivPhoto.setOnClickListener(v -> listener.onProfilePicClicked(lifehackData.getAuthorUid()));
        holder.likeButton.setOnClickListener(v -> {
            holder.clickedByUser = true;
            listener.onLikeButtonClicked(lifehackData.getLifehackId(), holder.likeButton.isLiked() ? ToBeLiked.Disliked : ToBeLiked.Liked);
        });

    }

    @Override
    public int getItemCount() {
        return lifehacks.size();
    }

    class LifehackViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_photo_lifehack)
        CircleImageView ivPhoto;
        @BindView(R.id.tv_text_lifehack)
        TextView tvContent;
        @BindView(R.id.tv_author_lifehack)
        TextView tvAuthor;
        @BindView(R.id.tv_counter_lifehack)
        TextView tvLikeCounter;
        @BindView(R.id.like_button_lifehack)
        LikeButton likeButton;
        @BindView(R.id.loader_lifehack)
        ImageView loader;
        @BindView(R.id.iv_lifehack_menu)
        ImageView menu;
        @BindView(R.id.root_lifehack_item)
        View root;

        private boolean clickedByUser = false;
        private PopupMenu popupMenu;

        private void createPopupMenu(String lifehackId) {
            popupMenu = new PopupMenu(menu.getContext(), menu);
            popupMenu.getMenuInflater().inflate(R.menu.lifehack_item_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_lifehack_delete) {
                    listener.onDeleteLifehackClicked(lifehackId);
                }
                return true;
            });
        }

        private void enableMenuButton(String lifehackId) {
            createPopupMenu(lifehackId);
            menu.setVisibility(View.VISIBLE);
            root.post(() -> {
                Rect delegateArea = new Rect();
                menu.setEnabled(true);
                menu.setOnClickListener(v -> showPopupMenu());
                menu.getHitRect(delegateArea);
                delegateArea.left -= 50;
                delegateArea.bottom += 50;
                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, menu);
                if (menu.getParent() instanceof View) {
                    ((View) menu.getParent()).setTouchDelegate(touchDelegate);
                }
            });
        }

        private void showPopupMenu() {
            popupMenu.show();
        }

        private void setLikeCounterText(int likeCount) {
            Context context = tvLikeCounter.getContext();
            if (likeCount == 0) {
                tvLikeCounter.setText(null);
                return;
            }
            String result = context.getString(R.string.like_counter_prefix) + " " + likeCount + " ";
            switch (likeCount) {
                case 1:
                    result += context.getString(R.string.like_counter_one_person_postfix);
                    break;
                case 2:
                default:
                    result += context.getString(R.string.like_counter_more_than_one_postfix);
                    break;

            }
            tvLikeCounter.setText(result);
        }

        LifehackViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ViewGroup v = (ViewGroup) root;
            LayoutTransition t = v.getLayoutTransition();
            t.enableTransitionType(LayoutTransition.CHANGING);
        }
    }

}

package com.appdav.myperspective.common.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.UserData;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private ArrayList<UserData> users;
    private Picasso picasso;
    private UserListClickListener listener;

    public boolean isAttachedToRecyclerView = false;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        isAttachedToRecyclerView = true;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        isAttachedToRecyclerView = false;
    }

    public UserListAdapter(ArrayList<UserData> users, UserListClickListener listener) {
        this.users = users;
        picasso = DaggerServiceComponent.builder().build().getPicasso();
        this.listener = listener;
    }

    public interface UserListClickListener {
        void onUserItemClicked(String userId, boolean hasEditorsRights, boolean isBannedFromCreatingEntries);
    }


    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserListViewHolder(v);
    }

    public void updateDataSet(ArrayList<UserData> updatedList) {
        this.users = updatedList;
        notifyDataSetChanged();
    }

    public void updateItem(int index) {
        notifyItemChanged(index);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        UserData user = users.get(position);
        String photoUrl;
        if (user.getPhotoUri() == null || user.getPhotoUri().isEmpty())
            photoUrl = Constants.DEFAULT_USER_PIC_URL;
        else photoUrl = user.getPhotoUri();
        picasso.load(photoUrl).centerCrop().resize(50, 50).into(holder.iv, new Callback() {
            @Override
            public void onSuccess() {
                holder.loader.setVisibility(View.GONE);
                picasso.load(photoUrl).centerCrop().fit().placeholder(holder.iv.getDrawable()).into(holder.iv);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        holder.tvName.setText(user.getUserNameAndSurname());
        if (user.hasEditorRights) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(R.string.editors_room_editor);
        } else holder.tvStatus.setVisibility(View.GONE);
        if (user.bannedFromCreatingEntries) {
            holder.tvBanned.setVisibility(View.VISIBLE);
        } else holder.tvBanned.setVisibility(View.GONE);
        holder.root.setOnClickListener(v -> {
            if (listener != null)
                listener.onUserItemClicked(user.getuId(), user.hasEditorRights, user.bannedFromCreatingEntries);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_photo_user_item)
        CircleImageView iv;
        @BindView(R.id.iv_loader_user_item)
        ImageView loader;
        @BindView(R.id.tv_name_user_item)
        TextView tvName;
        @BindView(R.id.tv_status_user_item)
        TextView tvStatus;
        @BindView(R.id.tv_banned_user_item)
        TextView tvBanned;
        @BindView(R.id.root_user_item)
        View root;

        UserListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

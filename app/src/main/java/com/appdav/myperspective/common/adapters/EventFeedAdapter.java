package com.appdav.myperspective.common.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.views.FavButton;
import com.appdav.myperspective.R;
import com.appdav.myperspective.contracts.ProfileEditorContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventFeedAdapter extends RecyclerView.Adapter<EventFeedAdapter.EventFeedViewHolder> {


    private Picasso picasso;

    private RecyclerView recyclerView;

    private ArrayList<EventData> events;
    private EventFeedCallback callback;
    private boolean hasEditorsRights = false;

    public EventFeedAdapter(ArrayList<EventData> events, EventFeedCallback callback) {
        this.events = events;
        this.callback = callback;
        picasso = DaggerServiceComponent.builder().build().getPicasso();
    }

    public EventFeedAdapter(ArrayList<EventData> events, EventFeedCallback callback, boolean hasEditorsRights) {
        this.events = events;
        this.callback = callback;
        picasso = DaggerServiceComponent.builder().build().getPicasso();
        this.hasEditorsRights = hasEditorsRights;
    }

    //interface used to pass onClick events from ViewHolders to presenter or any other class
    public interface EventFeedCallback {
        void onButtonGoClicked(String eventId);

        void onButtonWontGoClicked(String eventId);

        void onButtonChangeClicked(String eventId);

        void onButtonFavouriteClicked(String eventId, boolean toBeAdd);

        void onButtonRemoveEventClicked(String eventId);

        void onButtonShowPeopleList(EventData eventData);
    }

    //this method is used to update dataset without creating new instance
    public void setDataSet(ArrayList<EventData> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void removeEventFromAdapter(int index) {
        this.notifyItemRemoved(index);
    }

    @NonNull
    @Override
    public EventFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_feed, parent, false);
        return new EventFeedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventFeedViewHolder holder, int position) {

        EventData event = events.get(position);
        String imageUrl;
        if (event.getImageUrl() == null || event.getImageUrl().isEmpty())
            imageUrl = Constants.DEFAULT_EVENT_PIC_URL;
        else imageUrl = event.getImageUrl();
        picasso.load(imageUrl).centerCrop().resize(160, 100)
                .into(holder.ivEvent, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.ivLoader.setVisibility(View.GONE);
                        picasso.load(imageUrl).centerCrop().fit().placeholder(holder.ivEvent.getDrawable()).into(holder.ivEvent);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        if (hasEditorsRights) holder.enableEditMenu(event.getEventId());
        if (event.getEventType() == EventData.EVENT_TYPE_INSTANT) holder.instantEventIcon.setVisibility(View.VISIBLE);
        holder.tvName.setText(event.getName());
        holder.tvDate.setText(event.getEventDateAndTimeText());
        holder.setEventInfoText(event.getInfo());

        holder.setPeopleGoingText(event.getPeopleGoing());
        if (event.getPeopleGoing() != null && event.isCurrentUserGoing() == EventData.GOING)
            holder.setDeterminateView(true);
        else if (event.getPeopleNotGoing() != null && event.isCurrentUserGoing() == EventData.NOT_GOING)
            holder.setDeterminateView(false);
        else if (event.isCurrentUserGoing() == EventData.INDETERMINATE)
            holder.setIndeterminateView();
        holder.tvPeopleGoing.setOnClickListener(v -> callback.onButtonShowPeopleList(event));
        holder.btnFav.initialize(event.isInCurrentUserFavs());
        holder.btnFav.setOnClickListener(v -> {
            if (holder.btnFav.isActive()) {
                holder.btnFav.setInactive();
                callback.onButtonFavouriteClicked(event.getEventId(), false);
            } else {
                holder.btnFav.setActive();
                callback.onButtonFavouriteClicked(event.getEventId(), true);
            }
            holder.btnFav.setClickable(false);
        });
        holder.btnChange.setOnClickListener(v -> {
            callback.onButtonChangeClicked(event.getEventId());
            holder.isClickedByUser = true;
            holder.setLockedView();
        });
        holder.btnGo.setOnClickListener(v -> {
            callback.onButtonGoClicked(event.getEventId());
            holder.isClickedByUser = true;
            holder.setLockedView();
        });
        holder.btnWontGo.setOnClickListener(v -> {
            callback.onButtonWontGoClicked(event.getEventId());
            holder.isClickedByUser = true;
            holder.setLockedView();
        });
    }

    //method overrided to get instance of RecyclerView which is using that adapter
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    //this method is used to refresh ViewHolder with specific index without redrawing it
    public void updatePeopleGoingList(int index) {
        EventFeedViewHolder vh = (EventFeedViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
        if (vh != null) {
            if (events.get(index).isCurrentUserGoing() == EventData.GOING) {
                vh.setDeterminateView(true);
            } else if (events.get(index).isCurrentUserGoing() == EventData.NOT_GOING) {
                vh.setDeterminateView(false);
            } else if (events.get(index).isCurrentUserGoing() == EventData.INDETERMINATE) {
                vh.setIndeterminateView();
            }
            vh.setPeopleGoingText(events.get(index).getPeopleGoing());
        }
    }

    //this method is used to setup FavButton after getting callback from data model
    //FavButton is locked after user's action, so that onClick() method couldn't be invoked before database writes previous action
    //After that database action callback is received, and callback should invoke that method, so that FavButton could be used again
    public void setEventReadyAfterAddingToFavs(int index, boolean isUpdateSuccessful) {
        EventFeedViewHolder vh = (EventFeedViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
        if (vh != null) {
            if (!isUpdateSuccessful) {
                vh.btnFav.initialize(!vh.btnFav.isActive());
            } else vh.btnFav.initialize(vh.btnFav.isActive());
            vh.btnFav.setClickable(true);
        }
    }

    //returns dataset size
    @Override
    public int getItemCount() {
        return events.size();
    }


    //ViewHolder extension class
    class EventFeedViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_event)
        ImageView ivEvent;
        @BindView(R.id.iv_event_loader)
        ImageView ivLoader;

        @BindView(R.id.tv_event_name)
        TextView tvName;
        @BindView(R.id.tv_event_date)
        TextView tvDate;
        @BindView(R.id.tv_event_info)
        TextView tvInfo;
        @BindView(R.id.tv_event_decision)
        TextView tvDecision;
        @BindView(R.id.tv_event_people_going)
        TextView tvPeopleGoing;

        @BindView(R.id.button_event_go)
        Button btnGo;
        @BindView(R.id.button_event_wont_go)
        Button btnWontGo;
        @BindView(R.id.button_event_change)
        Button btnChange;
        @BindView(R.id.button_fav)
        FavButton btnFav;

        @BindView(R.id.pb_indeterminate)
        ProgressBar pb;
        @BindView(R.id.root_event_item)
        View root;

        @BindView(R.id.linear_layout_event_feed)
        View animatedTvContainer;

        @BindView(R.id.iv_menu_event)
        ImageView menu;

        @BindView(R.id.iv_instant_event_icon_event)
        ImageView instantEventIcon;

        private AnimatorSet animatorSet;

        private boolean isClickedByUser = false;

        private PopupMenu popupMenu;

        private String infoText;

        private void enableEditMenu(String eventId) {
            popupMenu = new PopupMenu(menu.getContext(), menu);
            popupMenu.getMenuInflater().inflate(R.menu.event_item_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_remove_event) {
                    callback.onButtonRemoveEventClicked(eventId);
                }
                return true;
            });
            menu.setVisibility(View.VISIBLE);
            root.post(() -> {
                Rect delegateArea = new Rect();
                menu.setEnabled(true);
                menu.setOnClickListener(v -> popupMenu.show());
                menu.getHitRect(delegateArea);
                delegateArea.left -= 50;
                delegateArea.bottom += 50;
                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, menu);
                if (menu.getParent() instanceof View) {
                    ((View) menu.getParent()).setTouchDelegate(touchDelegate);
                }
            });

        }

        //This method is invoked after user's click on one of three buttons: btnGo, btnWontGo, btnChange
        //This method uses animator object to remove any visible buttons
        // and prevent user from creating new onClick callbacks before result of action is written to database
        // and specific callback invokes method to set another view
        void setLockedView() {
            animate();
        }

        void animate() {
            if (!isClickedByUser) return;
            animatorSet.start();
        }

        void reverseAnimate() {
            if (!isClickedByUser) return;
            reversedAnimatorSet.start();
        }


        //Method is used to easily create String and put it inside tvPeopleGoing with specific array of users, depending arrays length
        void setPeopleGoingText(ArrayList<String> peopleGoing) {
            Context context = tvPeopleGoing.getContext();
            int peopleCount;
            if (peopleGoing == null) peopleCount = 0;
            else peopleCount = peopleGoing.size();

            String resultText;
            switch (peopleCount) {
                case 0:
                    resultText = context.getString(R.string.people_going_no_one);
                    break;
                case 1:
                    resultText = context.getString(R.string.people_going_one_prefix) + " " + peopleGoing.get(0);
                    break;
                case 2:
                    resultText = context.getString(R.string.people_going_many_prefix) + " " +
                            peopleGoing.get(0) + " " + context.getString(R.string.and) + " " +
                            peopleGoing.get(1);
                    break;
                case 5:
                case 6:
                case 4:
                    resultText = context.getString(R.string.people_going_many_prefix) + " " + peopleGoing.get(0) + context.getString(R.string.comma) + " ";
                    resultText += peopleGoing.get(1) + " " + context.getString(R.string.and_more) + " ";
                    resultText += peopleCount - 2 + " " + context.getString(R.string.people_going_less_than_5_postfix);
                    break;

                case 3:
                default:
                    resultText = context.getString(R.string.people_going_many_prefix) + " " + peopleGoing.get(0) + context.getString(R.string.comma) + " ";
                    resultText += peopleGoing.get(1) + " " + context.getString(R.string.and_more) + " ";
                    resultText += peopleCount - 2 + " " + context.getString(R.string.people_going_more_than_5_postfix);
                    break;
            }
            tvPeopleGoing.setText(resultText);

        }

        //this method sets views for item, for which user didn't decide whether he's going or not
        private void setIndeterminateView() {
            btnGo.setVisibility(View.VISIBLE);
            btnWontGo.setVisibility(View.VISIBLE);
            btnChange.setVisibility(View.GONE);
            tvDecision.setVisibility(View.GONE);
            reverseAnimate();
            isClickedByUser = false;
        }

        private void expandInfoText() {
            tvInfo.setText(infoText);
        }

        private void setEventInfoText(String infoText) {
            if (infoText.length() > 170) {
                this.infoText = infoText;
                Context context = tvInfo.getContext();
                String newText = infoText.substring(0, 150);
                if (newText.charAt(149) != ' ') {
                    for (int i = newText.length() - 1; i > 1; i--) {
                        char c = newText.charAt(i);
                        if (c == ' ') {
                            newText = newText.substring(0, i);
                            break;
                        }
                    }
                }
                newText += context.getString(R.string.event_3_dots) + " ";
                String showMore = context.getString(R.string.event_show_more);
                newText += showMore;
                SpannableString ss = new SpannableString(newText);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        expandInfoText();
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(context.getResources().getColor(R.color.colorPrimary));
                    }
                };
                ss.setSpan(clickableSpan, newText.length() - showMore.length(), newText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvInfo.setText(ss);
                tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
                tvInfo.setHighlightColor(Color.TRANSPARENT);
            } else tvInfo.setText(infoText);
        }


        //this method sets views for item, for which user decided whether he's going or not
        //boolean parameter sets views for users positive decision if true and negative decision if false
        void setDeterminateView(boolean userIsGoing) {
            btnWontGo.setVisibility(View.GONE);
            btnGo.setVisibility(View.GONE);
            btnChange.setVisibility(View.VISIBLE);
            tvDecision.setVisibility(View.VISIBLE);
            if (userIsGoing) tvDecision.setText(R.string.will_go);
            else tvDecision.setText(R.string.wont_go);
            reverseAnimate();
            isClickedByUser = false;
        }


        private EventFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            Context context = itemView.getContext();
            initAnimators(context);
            ViewGroup viewGroup = (ViewGroup) animatedTvContainer;
            LayoutTransition transition = viewGroup.getLayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGING);
        }

        private AnimatorSet reversedAnimatorSet;

        //this method is used to inflate animator objects for each of animated views and put them into one AnimatorSet used by animate() method
        private void initAnimators(Context context) {
            AnimatorSet pbAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.event_item_progress_bar_animator);
            AnimatorSet reversePbAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.event_item_progress_bar_reverse_animator);
            pbAnimator.setTarget(pb);
            reversePbAnimator.setTarget(pb);

            ObjectAnimator buttonGoAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_animator);
            ObjectAnimator reverseButtonGoAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_reverse_animator);
            reverseButtonGoAnimator.setTarget(btnGo);
            buttonGoAnimator.setTarget(btnGo);

            ObjectAnimator buttonWontGoAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_animator);
            ObjectAnimator reverseButtonWontGoAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_reverse_animator);
            reverseButtonWontGoAnimator.setTarget(btnWontGo);
            buttonWontGoAnimator.setTarget(btnWontGo);

            ObjectAnimator buttonChangeAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_animator);
            ObjectAnimator reverseButtonChangeAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_reverse_animator);
            reverseButtonChangeAnimator.setTarget(btnChange);
            buttonChangeAnimator.setTarget(btnChange);

            ObjectAnimator tvAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_animator);
            ObjectAnimator reverseTvAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.event_item_button_reverse_animator);
            reverseTvAnimator.setTarget(tvDecision);
            tvAnimator.setTarget(tvDecision);

            animatorSet = new AnimatorSet();
            reversedAnimatorSet = new AnimatorSet();
            animatorSet.playTogether(buttonGoAnimator, buttonWontGoAnimator, buttonChangeAnimator, tvAnimator, pbAnimator);
            reversedAnimatorSet.playTogether(reversePbAnimator, reverseButtonGoAnimator, reverseButtonWontGoAnimator, reverseButtonChangeAnimator, reverseTvAnimator);
        }
    }
}

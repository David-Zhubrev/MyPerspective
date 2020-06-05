package com.appdav.myperspective.common.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.CalendarRecyclerViewHolder> {

    private Picasso picasso;
    private CalendarAdapterOnClickListener listener;
    private ArrayList<EventData> events;

    public CalendarEventAdapter(ArrayList<EventData> events, CalendarAdapterOnClickListener listener) {
        picasso = DaggerServiceComponent.builder().build().getPicasso();
        this.listener = listener;
        this.events = events;
    }

    public interface CalendarAdapterOnClickListener {
        void onClick(EventData event);
    }

    @NonNull
    @Override
    public CalendarRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new CalendarRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarRecyclerViewHolder holder, int position) {
        EventData event = events.get(position);
        if (event.getEventType() == EventData.EVENT_TYPE_BIRTHDAY) {
            String imageUrl;
            if (event.getImageUrl() == null || event.getImageUrl().isEmpty())
                imageUrl = Constants.DEFAULT_USER_PIC_URL;
            else imageUrl = event.getImageUrl();
            picasso.load(imageUrl)
                    .centerCrop()
                    .resize(50, 50)
                    .into(holder.iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            picasso.load(imageUrl)
                                    .placeholder(holder.iv.getDrawable())
                                    .centerCrop()
                                    .fit()
                                    .into(holder.iv);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
            holder.tvName.setText(holder.tvName.getContext().getString(R.string.tv_calendar_birthday));
            holder.tvCounter.setText(event.getName());
            holder.button.setVisibility(View.GONE);
        } else if (event.getEventType() == 0 || event.getEventType() == EventData.EVENT_TYPE_COMMON) {
            String photoUrl;
            if (event.getImageUrl() == null || event.getImageUrl().isEmpty())
                photoUrl = Constants.DEFAULT_EVENT_PIC_URL;
            else photoUrl = event.getImageUrl();
            picasso.load(photoUrl)
                    .centerCrop()
                    .resize(50, 50)
                    .into(holder.iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            picasso.load(photoUrl)
                                    .placeholder(holder.iv.getDrawable())
                                    .fit()
                                    .centerCrop()
                                    .into(holder.iv);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
            holder.tvName.setText(event.getName());
            holder.setPeopleCounterText(event.getPeopleGoing());
            holder.button.setOnClickListener(v -> listener.onClick(event));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateAdapter(ArrayList<EventData> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    class CalendarRecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_name_item_calendar)
        TextView tvName;
        @BindView(R.id.tv_people_counter_item_calendar)
        TextView tvCounter;
        @BindView(R.id.civ_item_calendar)
        CircleImageView iv;
        @BindView(R.id.button_remind_item_calendar)
        Button button;

        void setPeopleCounterText(ArrayList<String> peopleGoing) {
            Context context = tvCounter.getContext();
            String result;
            if (peopleGoing == null || peopleGoing.isEmpty()) {
                result = context.getString(R.string.people_going_no_one);
            } else {
                int count = peopleGoing.size();
                switch (count) {
                    case 1:
                        result = context.getString(R.string.people_going_one_prefix) + " " + count + " "
                                + context.getString(R.string.people_going_more_than_5_postfix);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        result = context.getString(R.string.people_going_many_prefix) + " " +
                                count + " " + context.getString(R.string.people_going_less_than_5_postfix);
                        break;
                    case 5:
                    default:
                        result = context.getString(R.string.people_going_many_prefix) + " " +
                                count + " " + context.getString(R.string.people_going_more_than_5_postfix);
                        break;
                }
            }
            tvCounter.setText(result);
        }

        CalendarRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

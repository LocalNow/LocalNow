package com.example.localnow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localnow.R;
import com.example.localnow.model.Event;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void updateList(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.subtitle.setText(event.getDate());

        // Set Icon based on type
        String type = event.getType();
        if (type != null) {
            if (type.equals("Music")) {
                holder.icon.setImageResource(R.drawable.ic_marker_yellow);
            } else if (type.equals("Marathon")) {
                holder.icon.setImageResource(R.drawable.ic_marker_green);
            } else if (type.equals("Food")) {
                holder.icon.setImageResource(R.drawable.ic_marker_pink);
            } else {
                holder.icon.setImageResource(R.drawable.ic_marker_yellow); // Default
            }
        } else {
            holder.icon.setImageResource(R.drawable.ic_marker_yellow); // Default
        }

        // Set Bookmark
        if (event.isBookmarked()) {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark);
        } else {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_outline); // Assuming you have an outline or just
                                                                              // hide it
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(),
                    com.example.localnow.EventDetailActivity.class);
            intent.putExtra("id", event.getId());
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("image", event.getImage());
            intent.putExtra("isBookmarked", event.isBookmarked());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView subtitle;
        ImageView bookmark;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.eventIcon);
            title = itemView.findViewById(R.id.eventTitle);
            subtitle = itemView.findViewById(R.id.eventSubtitle);
            bookmark = itemView.findViewById(R.id.bookmarkIcon);
        }
    }
}

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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Event> events;

    public NotificationAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());

        // D-Day calculation
        String dDay = calculateDDay(event.getDate());
        holder.tvDday.setText(dDay);

        // Icon logic (Example)
        holder.ivIcon.setImageResource(R.drawable.ic_notification);
    }

    private String calculateDDay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return "";
        try {
            // Extract the first date if it's a range (e.g., "2025.12.01 ~ ...")
            String cleanDate = dateStr.split("~")[0].trim();
            cleanDate = cleanDate.replaceAll("[^0-9]", ""); // Remove non-digits

            if (cleanDate.length() != 8)
                return "D-?";

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
            java.util.Date eventDate = sdf.parse(cleanDate);
            java.util.Date today = new java.util.Date();

            // Reset time part for accurate day diff
            java.util.Calendar cal1 = java.util.Calendar.getInstance();
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal1.setTime(today);
            cal2.setTime(eventDate);

            // Clear time components
            cal1.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal1.set(java.util.Calendar.MINUTE, 0);
            cal1.set(java.util.Calendar.SECOND, 0);
            cal1.set(java.util.Calendar.MILLISECOND, 0);

            cal2.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal2.set(java.util.Calendar.MINUTE, 0);
            cal2.set(java.util.Calendar.SECOND, 0);
            cal2.set(java.util.Calendar.MILLISECOND, 0);

            long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
            long dayDiff = diff / (24 * 60 * 60 * 1000);

            if (dayDiff < 0)
                return "Ended";
            if (dayDiff == 0)
                return "D-Day";
            return "D-" + dayDiff;

        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvDday;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvDate = itemView.findViewById(R.id.tv_notification_date);
            tvDday = itemView.findViewById(R.id.tv_notification_dday);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
        }
    }
}

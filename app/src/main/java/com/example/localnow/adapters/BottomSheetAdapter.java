package com.example.localnow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localnow.R;

import java.util.List;

public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {

    private final List<PageData> pageDataList;

    public BottomSheetAdapter(List<PageData> pageDataList) {
        this.pageDataList = pageDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bottom_sheet_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PageData data = pageDataList.get(position);
        holder.pageTitle.setText(data.pageTitle);

        if (data.pageSubtitle != null && !data.pageSubtitle.isEmpty()) {
            holder.pageSubtitle.setText(data.pageSubtitle);
            holder.pageSubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.pageSubtitle.setVisibility(View.GONE);
        }

        holder.itemTitle.setText(data.itemTitle);
        holder.itemDescription.setText(data.itemDescription);
        holder.itemIcon.setImageResource(data.iconResId);

        // Set bookmark icon based on state
        updateBookmarkIcon(holder.itemBookmark, data.isBookmarked);

        // Bookmark click handler
        holder.itemBookmark.setOnClickListener(v -> {
            data.isBookmarked = !data.isBookmarked;
            updateBookmarkIcon(holder.itemBookmark, data.isBookmarked);

            if (data.bookmarkClickListener != null) {
                data.bookmarkClickListener.onBookmarkClick(data.eventId, data.isBookmarked);
            }
        });

        // Handle click if needed (e.g., open detail)
        holder.itemCard.setOnClickListener(v -> {
            if (data.onClickListener != null) {
                data.onClickListener.onClick(v);
            }
        });
    }

    private void updateBookmarkIcon(ImageView imageView, boolean isBookmarked) {
        if (isBookmarked) {
            imageView.setImageResource(R.drawable.ic_bookmark);
        } else {
            imageView.setImageResource(R.drawable.ic_bookmark_outline);
        }
    }

    @Override
    public int getItemCount() {
        return pageDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pageTitle, pageSubtitle, itemTitle, itemDescription;
        ImageView itemIcon, itemBookmark;
        View itemCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pageTitle = itemView.findViewById(R.id.pageTitle);
            pageSubtitle = itemView.findViewById(R.id.pageSubtitle);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemBookmark = itemView.findViewById(R.id.itemBookmark);
            itemCard = itemView.findViewById(R.id.itemCard);
        }
    }

    public interface BookmarkClickListener {
        void onBookmarkClick(int eventId, boolean isBookmarked);
    }

    public static class PageData {
        String pageTitle;
        String pageSubtitle;
        String itemTitle;
        String itemDescription;
        int iconResId;
        View.OnClickListener onClickListener;
        int eventId;
        double lat;
        double lng;
        boolean isBookmarked;
        BookmarkClickListener bookmarkClickListener;

        public PageData(String pageTitle, String pageSubtitle, String itemTitle, String itemDescription,
                int iconResId, View.OnClickListener onClickListener) {
            this.pageTitle = pageTitle;
            this.pageSubtitle = pageSubtitle;
            this.itemTitle = itemTitle;
            this.itemDescription = itemDescription;
            this.iconResId = iconResId;
            this.onClickListener = onClickListener;
            this.isBookmarked = false;
        }

        public PageData setEventInfo(int eventId, double lat, double lng) {
            this.eventId = eventId;
            this.lat = lat;
            this.lng = lng;
            return this;
        }

        public PageData setBookmarked(boolean bookmarked) {
            this.isBookmarked = bookmarked;
            return this;
        }

        public PageData setBookmarkClickListener(BookmarkClickListener listener) {
            this.bookmarkClickListener = listener;
            return this;
        }
    }
}

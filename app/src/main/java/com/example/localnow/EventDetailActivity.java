package com.example.localnow;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {
    private int eventId;
    private boolean isBookmarked;
    private ImageView bookmarkIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Get data from Intent
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");
        String image = getIntent().getStringExtra("image");
        eventId = getIntent().getIntExtra("id", -1);

        // Use BookmarkManager for global state
        com.example.localnow.utils.BookmarkManager bookmarkManager = com.example.localnow.utils.BookmarkManager
                .getInstance(this);
        isBookmarked = bookmarkManager.isBookmarked(eventId);

        // UI References
        TextView tvTitle = findViewById(R.id.tv_event_title);
        TextView tvDate = findViewById(R.id.tv_event_date);
        TextView tvLocation = findViewById(R.id.tv_event_location);
        ImageView ivImage = findViewById(R.id.iv_event_image);
        bookmarkIcon = findViewById(R.id.iv_bookmark);

        // Set Data
        tvTitle.setText(title != null ? title : "Event Title");
        tvDate.setText(date != null ? date : "Date");
        tvLocation.setText(location != null ? "장소 : " + location : "장소 : Unknown");

        // Load Image with Glide
        if (image != null && !image.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.ic_map_placeholder) // Use a placeholder
                    .error(R.drawable.ic_map_placeholder)
                    .into(ivImage);
        }

        // Set Initial Bookmark State
        updateBookmarkIcon();

        // Bookmark Click Listener
        bookmarkIcon.setOnClickListener(v -> toggleBookmark());
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
            bookmarkIcon.setColorFilter(android.graphics.Color.RED); // Make it red
        } else {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark); // Or outline if available
            bookmarkIcon.setColorFilter(android.graphics.Color.GRAY);
        }
    }

    private void toggleBookmark() {
        com.example.localnow.utils.BookmarkManager bookmarkManager = com.example.localnow.utils.BookmarkManager
                .getInstance(this);

        // Check if we have a valid event ID (from server)
        if (eventId == -1) {
            android.widget.Toast.makeText(this, "서버 연결이 필요합니다 (MockData)", android.widget.Toast.LENGTH_SHORT).show();
            // Still toggle locally for demo
            bookmarkManager.toggleBookmark(eventId);
            isBookmarked = bookmarkManager.isBookmarked(eventId);
            updateBookmarkIcon();
            return;
        }

        if (isBookmarked) {
            // Remove Bookmark
            com.example.localnow.api.RetrofitClient.getApiService().deleteBookmark(eventId)
                    .enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                bookmarkManager.removeBookmark(eventId);
                                isBookmarked = false;
                                updateBookmarkIcon();
                                android.widget.Toast.makeText(EventDetailActivity.this, "북마크 제거됨",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                // Fallback to local
                                bookmarkManager.removeBookmark(eventId);
                                isBookmarked = false;
                                updateBookmarkIcon();
                                android.widget.Toast.makeText(EventDetailActivity.this, "북마크 제거됨 (로컬)",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            // Fallback to local
                            bookmarkManager.removeBookmark(eventId);
                            isBookmarked = false;
                            updateBookmarkIcon();
                            android.widget.Toast.makeText(EventDetailActivity.this, "북마크 제거됨 (로컬/오프라인)",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Add Bookmark
            com.example.localnow.model.BookmarkRequest request = new com.example.localnow.model.BookmarkRequest(
                    eventId);
            com.example.localnow.api.RetrofitClient.getApiService().addBookmark(request)
                    .enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                bookmarkManager.addBookmark(eventId);
                                isBookmarked = true;
                                updateBookmarkIcon();
                                android.widget.Toast.makeText(EventDetailActivity.this, "북마크 추가됨",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                // Fallback to local
                                bookmarkManager.addBookmark(eventId);
                                isBookmarked = true;
                                updateBookmarkIcon();
                                android.widget.Toast.makeText(EventDetailActivity.this, "북마크 추가됨 (로컬)",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            // Fallback to local
                            bookmarkManager.addBookmark(eventId);
                            isBookmarked = true;
                            updateBookmarkIcon();
                            android.widget.Toast.makeText(EventDetailActivity.this, "북마크 추가됨 (로컬/오프라인)",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

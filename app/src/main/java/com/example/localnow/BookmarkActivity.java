package com.example.localnow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localnow.adapters.EventAdapter;
import com.example.localnow.model.Event;
import com.example.localnow.utils.MockData;
import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        RecyclerView recyclerView = findViewById(R.id.rv_bookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchBookmarks(recyclerView);
    }

    private void fetchBookmarks(RecyclerView recyclerView) {
        com.example.localnow.api.RetrofitClient.getApiService().getBookmarks()
                .enqueue(new retrofit2.Callback<com.example.localnow.model.EventResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.localnow.model.EventResponse> call, retrofit2.Response<com.example.localnow.model.EventResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            EventAdapter adapter = new EventAdapter(response.body().getData());
                            recyclerView.setAdapter(adapter);
                        } else {
                            // Fallback to local bookmarks
                            loadLocalBookmarks(recyclerView);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.localnow.model.EventResponse> call, Throwable t) {
                        loadLocalBookmarks(recyclerView);
                        android.widget.Toast.makeText(BookmarkActivity.this, "로컬 북마크 표시 중",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLocalBookmarks(RecyclerView recyclerView) {
        com.example.localnow.utils.BookmarkManager bookmarkManager = com.example.localnow.utils.BookmarkManager
                .getInstance(this);

        List<Event> allEvents = MockData.getEvents();
        List<Event> bookmarkedEvents = new ArrayList<>();

        for (Event event : allEvents) {
            if (bookmarkManager.isBookmarked(event.getId())) {
                bookmarkedEvents.add(event);
            }
        }

        EventAdapter adapter = new EventAdapter(bookmarkedEvents);
        recyclerView.setAdapter(adapter);
    }
}

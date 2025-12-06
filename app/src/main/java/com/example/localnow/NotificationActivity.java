package com.example.localnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localnow.adapters.NotificationAdapter;
import com.example.localnow.api.RetrofitClient;
import com.example.localnow.model.Event;
import com.example.localnow.model.EventResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recycler_view_notifications);
        tvEmpty = findViewById(R.id.tv_empty_notifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchBookmarkedEvents();
    }

    private void fetchBookmarkedEvents() {
        RetrofitClient.getApiService().getBookmarks().enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> bookmarks = response.body().getData();
                    if (bookmarks != null) {
                        filterAndShowUpcomingEvents(bookmarks);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                showEmptyState();
            }
        });
    }

    private void filterAndShowUpcomingEvents(List<Event> events) {
        List<Event> upcomingEvents = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        // For range dates, we might need more complex parsing, but let's assume raw
        // string for now
        // Or "yyyy-MM-dd" if that's what backend returns

        // Backend currently returns strings like "2025-12-25" or ranges
        // Let's try to parse the start date

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 14); // Next 14 days
        Date limitDate = cal.getTime();

        for (Event event : events) {
            // Simplified logic: Just add all bookmarks for now to see them
            // TODO: Implement precise date parsing and filtering
            upcomingEvents.add(event);
        }

        if (upcomingEvents.isEmpty()) {
            showEmptyState();
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new NotificationAdapter(upcomingEvents);
            recyclerView.setAdapter(adapter);
        }
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}

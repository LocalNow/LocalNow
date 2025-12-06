package com.example.localnow;

import android.os.Bundle;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localnow.adapters.EventAdapter;
import com.example.localnow.model.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.rv_calendar_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        allEvents = new ArrayList<>();
        adapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Fetch real events from server
        fetchEvents();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateEventsForDate(selectedDate);
        });
    }

    private void fetchEvents() {
        com.example.localnow.api.RetrofitClient.getApiService().getEvents()
                .enqueue(new retrofit2.Callback<com.example.localnow.model.EventResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.localnow.model.EventResponse> call,
                            retrofit2.Response<com.example.localnow.model.EventResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Event> events = response.body().getData();
                            if (events != null) {
                                allEvents = events;

                                // Update for today initially
                                Calendar today = Calendar.getInstance();
                                String todayStr = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                        today.get(Calendar.YEAR),
                                        today.get(Calendar.MONTH) + 1,
                                        today.get(Calendar.DAY_OF_MONTH));
                                updateEventsForDate(todayStr);

                                android.widget.Toast.makeText(CalendarActivity.this, "일정을 불러왔습니다",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            android.widget.Toast.makeText(CalendarActivity.this, "일정을 불러오지 못했습니다",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.localnow.model.EventResponse> call, Throwable t) {
                        android.widget.Toast.makeText(CalendarActivity.this, "네트워크 오류: " + t.getMessage(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEventsForDate(String selectedDate) {
        List<Event> eventsOnDate = new ArrayList<>();

        for (Event event : allEvents) {
            if (isEventOnDate(event, selectedDate)) {
                eventsOnDate.add(event);
            }
        }

        adapter.updateList(eventsOnDate);
    }

    private boolean isEventOnDate(Event event, String date) {
        String startDate = event.getStartDate();
        String endDate = event.getEndDate();

        if (startDate == null || startDate.isEmpty()) {
            return false;
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = startDate;
        }

        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }
}

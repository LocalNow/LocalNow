package com.example.localnow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localnow.adapters.EventAdapter;
import com.example.localnow.utils.MockData;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EventAdapter adapter;
    private List<com.example.localnow.model.Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.rv_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        allEvents = new ArrayList<>();
        adapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        android.widget.EditText searchBox = findViewById(R.id.et_search);

        // Fetch events from server or use MockData
        fetchEvents(recyclerView, searchBox);

        // Setup Search Listener
        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void fetchEvents(RecyclerView recyclerView, android.widget.EditText searchBox) {
        com.example.localnow.api.RetrofitClient.getApiService().getEvents()
                .enqueue(new retrofit2.Callback<com.example.localnow.model.EventResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.localnow.model.EventResponse> call,
                            retrofit2.Response<com.example.localnow.model.EventResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            allEvents = response.body().getData();
                        } else {
                            allEvents = MockData.getEvents();
                        }
                        // Update adapter with all events initially
                        adapter.updateList(allEvents);

                        // If there's already text, filter it
                        if (searchBox.getText().length() > 0) {
                            filterEvents(searchBox.getText().toString());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.localnow.model.EventResponse> call,
                            Throwable t) {
                        allEvents = MockData.getEvents();
                        adapter.updateList(allEvents);
                        if (searchBox.getText().length() > 0) {
                            filterEvents(searchBox.getText().toString());
                        }
                    }
                });
    }

    private void filterEvents(String query) {
        List<com.example.localnow.model.Event> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList = new ArrayList<>(allEvents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (com.example.localnow.model.Event event : allEvents) {
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(event);
                } else if (event.getLocation() != null && event.getLocation().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(event);
                }
            }
        }
        adapter.updateList(filteredList);
    }
}

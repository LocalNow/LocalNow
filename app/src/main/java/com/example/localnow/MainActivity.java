package com.example.localnow;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private com.kakao.vectormap.MapView mapView;
    private com.kakao.vectormap.KakaoMap kakaoMap;
    private java.util.List<com.example.localnow.model.Event> pendingEvents = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kakao Map initialization
        try {
            mapView = findViewById(R.id.map_view);
            mapView.post(() -> {
                try {
                    mapView.start(new com.kakao.vectormap.MapLifeCycleCallback() {
                        @Override
                        public void onMapDestroy() {
                            android.util.Log.d("MainActivity", "Map destroyed");
                        }

                        @Override
                        public void onMapError(Exception e) {
                            android.util.Log.e("MainActivity", "Map error: " + e.getMessage());
                            android.widget.Toast.makeText(MainActivity.this, "Map error: " + e.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, new com.kakao.vectormap.KakaoMapReadyCallback() {
                        @Override
                        public void onMapReady(com.kakao.vectormap.KakaoMap map) {
                            kakaoMap = map;
                            com.kakao.vectormap.LatLng songdoPosition = com.kakao.vectormap.LatLng.from(37.3948,
                                    126.6392);
                            com.kakao.vectormap.camera.CameraUpdate cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory
                                    .newCenterPosition(songdoPosition, 15);
                            kakaoMap.moveCamera(cameraUpdate);
                            android.util.Log.d("MainActivity", "Map initialized successfully at Songdo");

                            // Add markers if events were already fetched
                            if (!pendingEvents.isEmpty()) {
                                addMarkersToMap(pendingEvents);
                            }
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Failed to start Kakao Map", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to initialize Kakao Map", e);
            android.widget.Toast.makeText(this, "Map unavailable: " + e.getMessage(),
                    android.widget.Toast.LENGTH_LONG).show();
        }

        ImageView btnNotification, btnBookmark, btnSearch;

        btnNotification = findViewById(R.id.btnNotification);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnSearch = findViewById(R.id.btnSearch);

        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        btnBookmark.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        ImageView btnCalendar = findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        // Fetch Events (will populate bottom sheet automatically)
        fetchEvents();

        // Log Kakao Key Hash (only in logcat, no popup)
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    android.content.pm.PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT);
                android.util.Log.d("KeyHash", keyHash);
            }
        } catch (Exception e) {
            android.util.Log.e("KeyHash", "Failed to get key hash", e);
        }
    }

    private void fetchEvents() {
        com.example.localnow.api.RetrofitClient.getApiService().getEvents()
                .enqueue(new retrofit2.Callback<com.example.localnow.model.EventResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.localnow.model.EventResponse> call,
                            retrofit2.Response<com.example.localnow.model.EventResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            java.util.List<com.example.localnow.model.Event> events = response.body().getData();

                            if (events != null && !events.isEmpty()) {
                                android.util.Log.d("MainActivity", "✅ Fetched " + events.size() + " events");
                                // Store events for when map is ready
                                pendingEvents = events;
                                // Update bottom sheet with real data
                                updateBottomSheetWithEvents(events);
                                // Add markers to map (if map is ready)
                                addMarkersToMap(events);
                                // Schedule notifications
                                scheduleEventNotifications(events);
                            } else {
                                android.util.Log.w("MainActivity", "⚠️ No events found in response");
                                android.widget.Toast.makeText(MainActivity.this, "이벤트 데이터가 없습니다",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            android.util.Log.w("MainActivity", "⚠️ Response unsuccessful: " + response.code());
                            android.widget.Toast.makeText(MainActivity.this, "데이터를 불러오지 못했습니다",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.localnow.model.EventResponse> call,
                            Throwable t) {
                        android.util.Log.e("MainActivity", "❌ Failed to fetch events: " + t.getMessage());
                        android.widget.Toast.makeText(MainActivity.this, "서버 연결 실패: " + t.getMessage(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMarkersToMap(java.util.List<com.example.localnow.model.Event> events) {
        if (kakaoMap == null) {
            android.util.Log.w("MainActivity", "KakaoMap is not ready yet - will retry later");
            return;
        }

        android.util.Log.d("MainActivity", "🗺️ Adding markers for " + events.size() + " events");

        // Use LabelManager to add labels
        com.kakao.vectormap.label.LabelManager labelManager = kakaoMap.getLabelManager();
        com.kakao.vectormap.label.LabelLayer layer = labelManager.getLayer();

        if (layer == null) {
            layer = labelManager.addLayer(com.kakao.vectormap.label.LabelLayerOptions.from("eventLayer"));
        }

        int markerCount = 0;
        for (com.example.localnow.model.Event event : events) {
            double lat = event.getLat();
            double lng = event.getLng();

            // Skip events without valid coordinates
            if (lat == 0.0 || lng == 0.0) {
                android.util.Log.d("MainActivity", "Skipping " + event.getTitle() + " - no coordinates");
                continue;
            }

            try {
                com.kakao.vectormap.LatLng position = com.kakao.vectormap.LatLng.from(lat, lng);
                String labelId = "event_" + event.getId();

                // Create label options with position
                com.kakao.vectormap.label.LabelOptions labelOptions = com.kakao.vectormap.label.LabelOptions
                        .from(labelId, position)
                        .setStyles(R.mipmap.ic_launcher) // Use mipmap PNG directly
                        .setClickable(true);

                layer.addLabel(labelOptions);
                markerCount++;
                android.util.Log.d("MainActivity",
                        "Added marker: " + event.getTitle() + " at (" + lat + ", " + lng + ")");
            } catch (Exception e) {
                android.util.Log.e("MainActivity",
                        "Failed to add marker for " + event.getTitle() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        android.util.Log.d("MainActivity", "Total markers added: " + markerCount + "/" + events.size());
    }

    private void updateBottomSheetWithEvents(java.util.List<com.example.localnow.model.Event> events) {
        androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.viewPager);
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tabLayout);

        java.util.List<com.example.localnow.adapters.BottomSheetAdapter.PageData> dataList = new java.util.ArrayList<>();

        // Create pages from real event data
        for (int i = 0; i < Math.min(events.size(), 5); i++) {
            com.example.localnow.model.Event event = events.get(i);

            com.example.localnow.adapters.BottomSheetAdapter.PageData pageData = new com.example.localnow.adapters.BottomSheetAdapter.PageData(
                    event.getCategory() != null ? event.getCategory() : "행사",
                    event.getLocation() != null ? event.getLocation() : "",
                    event.getTitle(),
                    event.getDate() != null ? event.getDate() : "",
                    R.drawable.ic_marker_yellow,
                    v -> {
                        // Move map camera to event location first
                        if (kakaoMap != null && event.getLat() != 0 && event.getLng() != 0) {
                            com.kakao.vectormap.LatLng position = com.kakao.vectormap.LatLng.from(event.getLat(),
                                    event.getLng());
                            com.kakao.vectormap.camera.CameraUpdate cameraUpdate = com.kakao.vectormap.camera.CameraUpdateFactory
                                    .newCenterPosition(position, 17);
                            kakaoMap.moveCamera(cameraUpdate);
                        }

                        // Then open detail activity
                        Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                        intent.putExtra("id", event.getId());
                        intent.putExtra("title", event.getTitle());
                        intent.putExtra("date", event.getDate());
                        intent.putExtra("location", event.getLocation());
                        intent.putExtra("description", event.getDescription());
                        intent.putExtra("image", event.getImage());
                        startActivity(intent);
                    });

            // Add event info for map navigation
            pageData.setEventInfo(event.getId(), event.getLat(), event.getLng());

            // Add bookmark click handler
            pageData.setBookmarkClickListener((eventId, isBookmarked) -> {
                android.util.Log.d("MainActivity", "Bookmark toggled for event " + eventId + ": " + isBookmarked);

                if (isBookmarked) {
                    // Add bookmark
                    com.example.localnow.api.RetrofitClient.getApiService()
                            .addBookmark(new com.example.localnow.model.BookmarkRequest(eventId))
                            .enqueue(new retrofit2.Callback<Void>() {
                                @Override
                                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        android.widget.Toast.makeText(MainActivity.this, "북마크 추가됨",
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    } else {
                                        android.widget.Toast.makeText(MainActivity.this, "북마크 실패",
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                    android.widget.Toast
                                            .makeText(MainActivity.this, "네트워크 오류", android.widget.Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                } else {
                    // Remove bookmark
                    com.example.localnow.api.RetrofitClient.getApiService()
                            .deleteBookmark(eventId)
                            .enqueue(new retrofit2.Callback<Void>() {
                                @Override
                                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        android.widget.Toast.makeText(MainActivity.this, "북마크 삭제됨",
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    } else {
                                        android.widget.Toast
                                                .makeText(MainActivity.this, "삭제 실패", android.widget.Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                    android.widget.Toast
                                            .makeText(MainActivity.this, "네트워크 오류", android.widget.Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                }
            });

            dataList.add(pageData);
        }

        // If no events, show empty state
        if (dataList.isEmpty()) {
            dataList.add(new com.example.localnow.adapters.BottomSheetAdapter.PageData(
                    "알림",
                    "",
                    "등록된 이벤트가 없습니다",
                    "새로운 이벤트를 기다려주세요",
                    R.drawable.ic_marker_yellow,
                    v -> {
                    }));
        }

        com.example.localnow.adapters.BottomSheetAdapter adapter = new com.example.localnow.adapters.BottomSheetAdapter(
                dataList);
        viewPager.setAdapter(adapter);

        new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Tab configuration
                }).attach();
    }

    private void scheduleEventNotifications(java.util.List<com.example.localnow.model.Event> events) {
        androidx.work.WorkManager workManager = androidx.work.WorkManager.getInstance(this);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        for (com.example.localnow.model.Event event : events) {
            String startStr = event.getStartDate();
            if (startStr == null || startStr.isEmpty())
                continue;

            try {
                java.util.Date startDate = sdf.parse(startStr);
                long startTime = startDate.getTime();
                long currentTime = System.currentTimeMillis();

                // Notification intervals: 3 days, 1 day, 12h, 6h, 1h
                long[] intervals = {
                        3 * 24 * 60 * 60 * 1000L,
                        1 * 24 * 60 * 60 * 1000L,
                        12 * 60 * 60 * 1000L,
                        6 * 60 * 60 * 1000L,
                        1 * 60 * 60 * 1000L
                };

                String[] intervalNames = { "3일 전", "1일 전", "12시간 전", "6시간 전", "1시간 전" };

                for (int i = 0; i < intervals.length; i++) {
                    long triggerTime = startTime - intervals[i];
                    long delay = triggerTime - currentTime;

                    if (delay > 0) {
                        androidx.work.Data data = new androidx.work.Data.Builder()
                                .putString("title", "이벤트 알림: " + event.getTitle())
                                .putString("message", "행사가 " + intervalNames[i] + "에 시작됩니다!")
                                .build();

                        androidx.work.OneTimeWorkRequest request = new androidx.work.OneTimeWorkRequest.Builder(
                                com.example.localnow.workers.NotificationWorker.class)
                                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                                .setInputData(data)
                                .addTag("event_" + event.getId())
                                .build();

                        workManager.enqueue(request);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        android.util.Log.d("MainActivity", "Scheduled notifications for " + events.size() + " events");
    }

}

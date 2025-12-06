package com.example.localnow.model;

import com.google.gson.annotations.SerializedName;

public class BookmarkRequest {
    @SerializedName("event_id")
    private int eventId;

    public BookmarkRequest(int eventId) {
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }
}

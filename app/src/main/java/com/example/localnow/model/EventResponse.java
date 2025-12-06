package com.example.localnow.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<Event> data;

    public String getStatus() {
        return status;
    }

    public int getCount() {
        return count;
    }

    public List<Event> getData() {
        return data;
    }
}

package com.example.localnow.model;

public class Event {
    @com.google.gson.annotations.SerializedName("title")
    private String title;
    @com.google.gson.annotations.SerializedName("date")
    private String date;
    @com.google.gson.annotations.SerializedName("type")
    private String type; // Note: Backend might not return this, handled safely in adapter
    @com.google.gson.annotations.SerializedName("category")
    private String category; // Backend returns "category"
    @com.google.gson.annotations.SerializedName("is_bookmarked")
    private boolean isBookmarked;
    @com.google.gson.annotations.SerializedName("start_date")
    private String startDate;
    @com.google.gson.annotations.SerializedName("end_date")
    private String endDate;

    @com.google.gson.annotations.SerializedName("id")
    private int id;
    @com.google.gson.annotations.SerializedName("lat")
    private double lat;
    @com.google.gson.annotations.SerializedName("lng")
    private double lng;
    @com.google.gson.annotations.SerializedName("image")
    private String image;
    @com.google.gson.annotations.SerializedName("location")
    private String location;

    @com.google.gson.annotations.SerializedName("description")
    private String description;
    @com.google.gson.annotations.SerializedName("source")
    private String source;

    public Event() {
    }

    public Event(String title, String date, String type, boolean isBookmarked) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.isBookmarked = isBookmarked;
        this.startDate = "";
        this.endDate = "";
    }

    public Event(String title, String date, String type, boolean isBookmarked, String startDate, String endDate) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.isBookmarked = isBookmarked;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

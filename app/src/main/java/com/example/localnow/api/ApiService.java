package com.example.localnow.api;

import com.example.localnow.model.BookmarkRequest;
import com.example.localnow.model.Event;
import com.example.localnow.model.GoogleLoginRequest;
import com.example.localnow.model.LoginRequest;
import com.example.localnow.model.RegisterRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("/auth/login")
    Call<Void> login(@Body LoginRequest request);

    @POST("/auth/google")
    Call<Void> googleLogin(@Body GoogleLoginRequest request);

    @GET("/api/events")
    Call<com.example.localnow.model.EventResponse> getEvents();

    @POST("/api/bookmarks/")
    Call<Void> addBookmark(@Body BookmarkRequest request);

    @DELETE("/api/bookmarks/{id}")
    Call<Void> deleteBookmark(@Path("id") int id);

    @GET("/api/bookmarks/")
    Call<com.example.localnow.model.EventResponse> getBookmarks();
}

package com.example.localnow.model;

public class GoogleLoginRequest {
    private String token;

    public GoogleLoginRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

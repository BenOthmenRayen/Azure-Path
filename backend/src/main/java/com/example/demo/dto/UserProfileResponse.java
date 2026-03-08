package com.example.demo.dto;

public class UserProfileResponse {
    private String uid;
    private String email;
    private String role;

    public UserProfileResponse(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
    }

    // getters & setters
}

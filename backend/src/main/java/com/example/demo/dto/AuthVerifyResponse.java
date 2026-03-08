package com.example.demo.dto;


import java.util.Map;

public class AuthVerifyResponse {
    private String uid;
    private String email;
    private String name;
    private String role;
    private Map<String, Object> profile; // optional extra fields

    public AuthVerifyResponse() {}

    public AuthVerifyResponse(String uid, String email, String name, String role, Map<String,Object> profile) {
        this.uid = uid; this.email = email; this.name = name; this.role = role; this.profile = profile;
    }

    // getters/setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Map<String, Object> getProfile() { return profile; }
    public void setProfile(Map<String, Object> profile) { this.profile = profile; }
}

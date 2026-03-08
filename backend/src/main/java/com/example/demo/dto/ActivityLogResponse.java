// src/main/java/com/example/demo/dto/ActivityLogResponse.java
package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ActivityLogResponse {
    private String id;
    private String adminEmail;
    private String action;
    private String targetEmail;
    private String details;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Default constructor
    public ActivityLogResponse() {}

    // Constructor with parameters
    public ActivityLogResponse(String id, String adminEmail, String action,
                               String targetEmail, String details, LocalDateTime timestamp) {
        this.id = id;
        this.adminEmail = adminEmail;
        this.action = action;
        this.targetEmail = targetEmail;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
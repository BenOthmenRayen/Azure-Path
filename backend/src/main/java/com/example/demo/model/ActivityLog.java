// src/main/java/com/example/demo/model/ActivityLog.java
package com.example.demo.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private String id;
    private String adminUid;
    private String adminEmail;
    private String action;
    private String targetUid;
    private String targetEmail;
    private String details;
    private String timestamp; // Store as ISO string for Firebase compatibility

    // Default constructor (required by Firebase)
    public ActivityLog() {
        this.timestamp = LocalDateTime.now().toString();
    }

    // Constructor with parameters
    public ActivityLog(String adminUid, String adminEmail, String action,
                       String targetUid, String targetEmail, String details) {
        this.adminUid = adminUid;
        this.adminEmail = adminEmail;
        this.action = action;
        this.targetUid = targetUid;
        this.targetEmail = targetEmail;
        this.details = details;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
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

    public String getTargetUid() {
        return targetUid;
    }

    public void setTargetUid(String targetUid) {
        this.targetUid = targetUid;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
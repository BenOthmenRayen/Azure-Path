// src/main/java/com/example/demo/dto/UpdateUserRoleRequest.java
package com.example.demo.dto;

public class UpdateUserRoleRequest {
    private String uid;
    private String newRole;

    // Default constructor
    public UpdateUserRoleRequest() {}

    // Constructor with parameters
    public UpdateUserRoleRequest(String uid, String newRole) {
        this.uid = uid;
        this.newRole = newRole;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNewRole() {
        return newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }
}
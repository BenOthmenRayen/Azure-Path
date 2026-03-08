// src/main/java/com/example/demo/controller/UserController.java
package com.example.demo.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String uid = authentication.getName();
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);

            // Get custom claims (including role)
            Map<String, Object> claims = userRecord.getCustomClaims();
            String role = "user";
            if (claims != null && claims.containsKey("role")) {
                role = claims.get("role").toString();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("uid", userRecord.getUid());
            response.put("email", userRecord.getEmail());
            response.put("name", userRecord.getDisplayName());
            response.put("role", role);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
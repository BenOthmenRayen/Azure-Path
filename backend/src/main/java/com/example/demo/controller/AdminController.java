package com.example.demo.controller;

import com.example.demo.service.FirebaseService;
import com.example.demo.service.FirebaseUserService; // see note below
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints to manage users from the dashboard.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final FirebaseService firebaseService;

    public AdminController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        try {
            List<Map<String, Object>> users = firebaseService.listAllUserProfiles();
            return ResponseEntity.ok(users);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");
            String uid = firebaseService.createUserAndSetRole(name, email, password, "ROLE_USER");
            return ResponseEntity.status(201).body(Map.of("uid", uid));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{uid}/role")
    public ResponseEntity<?> updateRole(@PathVariable String uid, @RequestBody Map<String,String> body) {
        try {
            String newRole = body.get("role");
            firebaseService.updateUserRole(uid, newRole);
            return ResponseEntity.ok(Map.of("updated", true));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{uid}")
    public ResponseEntity<?> deleteUser(@PathVariable String uid) {
        try {
            firebaseService.deleteUserByUid(uid);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count")
    public ResponseEntity<?> countUsers() {
        try {
            long c = firebaseService.countUsers();
            return ResponseEntity.ok(Map.of("count", c));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }
}

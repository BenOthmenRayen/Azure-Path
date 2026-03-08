package com.example.demo.controller;

import com.example.demo.service.FirebaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    private final FirebaseService firebaseService;

    public MeController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error","Unauthenticated"));
        }
        String uid = (String) authentication.getPrincipal();
        Map<String, Object> profile = firebaseService.getProfile(uid);
        if (profile == null || profile.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error","Profile not found"));
        }
        profile.remove("password");
        return ResponseEntity.ok(profile);
    }
}

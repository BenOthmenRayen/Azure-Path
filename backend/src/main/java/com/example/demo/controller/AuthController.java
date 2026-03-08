package com.example.demo.controller;

import com.example.demo.service.RecaptchaService;
import com.example.demo.service.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RecaptchaService recaptchaService;
    private final FirebaseService firebaseService;

    public AuthController(RecaptchaService recaptchaService, FirebaseService firebaseService) {
        this.recaptchaService = recaptchaService;
        this.firebaseService = firebaseService;
    }

    /**
     * Login endpoint - validates Firebase ID token + reCAPTCHA
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1) Extract tokens
            String idToken = body.get("idToken");
            String recaptchaToken = body.get("recaptchaToken");

            // 2) Validate input
            if (idToken == null || idToken.isBlank()) {
                response.put("success", false);
                response.put("error", "idToken is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (recaptchaToken == null || recaptchaToken.isBlank()) {
                response.put("success", false);
                response.put("error", "recaptchaToken is required");
                return ResponseEntity.badRequest().body(response);
            }

            // 3) Verify reCAPTCHA first
            String remoteIp = request.getRemoteAddr();
            boolean recaptchaValid = recaptchaService.isValid(recaptchaToken, "login", remoteIp);

            if (!recaptchaValid) {
                log.warn("Login attempt failed reCAPTCHA validation from IP: {}", remoteIp);
                response.put("success", false);
                response.put("error", "reCAPTCHA validation failed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 4) Verify Firebase ID token
            FirebaseToken decodedToken;
            try {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            } catch (FirebaseAuthException e) {
                log.error("Firebase token verification failed: {}", e.getMessage());
                response.put("success", false);
                response.put("error", "Invalid Firebase token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 5) Extract user info
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            log.info("✅ Login successful for user: {} ({})", email, uid);

            // 6) Return success response
            response.put("success", true);
            response.put("uid", uid);
            response.put("email", email);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify endpoint - validates Firebase ID token and returns user info with role
     * This combines the functionality of the old AuthVerifyController
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract Bearer token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("error", "Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String idToken = authHeader.substring(7);

            // Verify Firebase token
            FirebaseToken decodedToken;
            try {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            } catch (FirebaseAuthException e) {
                log.error("Token verification failed: {}", e.getMessage());
                response.put("success", false);
                response.put("error", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extract user info
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            // Get role from Firebase Realtime Database
            String role = null;
            try {
                role = firebaseService.getRoleForUid(uid);
            } catch (Exception e) {
                log.warn("Failed to fetch role for uid {}: {}", uid, e.getMessage());
            }

            // Set default role if not found
            if (role == null || role.isBlank()) {
                role = "ROLE_USER";
            }

            // Return user info with role
            response.put("success", true);
            response.put("uid", uid);
            response.put("email", email);
            response.put("name", name);
            response.put("role", role);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error during token verification", e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Register endpoint - creates user profile in backend database
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String name = body.get("name");
            String email = body.get("email");

            if (name == null || name.isBlank() || email == null || email.isBlank()) {
                response.put("success", false);
                response.put("error", "Name and email are required");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: Save user profile to your database here
            // Example: userRepository.save(new User(name, email));

            log.info("✅ User registered: {} ({})", name, email);

            response.put("success", true);
            response.put("message", "User registered successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during registration", e);
            response.put("success", false);
            response.put("error", "Registration failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
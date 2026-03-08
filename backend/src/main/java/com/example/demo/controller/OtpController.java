package com.example.demo.controller;

import com.example.demo.dto.SendOtpRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class OtpController {

    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;
    private final EmailService emailService;

    public OtpController(OtpService otpService, EmailService emailService) {
        this.otpService = otpService;
        this.emailService = emailService;
    }

    /**
     * Send MFA code endpoint
     * POST /api/auth/send-mfa
     */
    @PostMapping("/send-mfa")
    public ResponseEntity<?> sendMfaCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Generate OTP (use email as key for now, or uid if provided)
            String uid = email; // For simplicity, use email as UID key
            String otp = otpService.generateOtpFor(uid, email);

            // Send email with OTP
            emailService.sendOtpEmail(email, otp, 5);

            log.info("✅ OTP sent to {}", email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification code sent successfully"
            ));

        } catch (IllegalStateException ex) {
            log.warn("Rate limit hit for email: {}", body.get("email"));
            return ResponseEntity.status(429).body(Map.of(
                    "success", false,
                    "error", "Please wait before requesting a new code"
            ));
        } catch (Exception ex) {
            log.error("Error sending OTP: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to send verification code"
            ));
        }
    }

    /**
     * Verify MFA code endpoint
     * POST /api/auth/verify-mfa
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfaCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String code = body.get("code");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            if (code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Code is required"));
            }

            // Use email as UID for verification
            String uid = email;
            boolean isValid = otpService.verifyOtp(uid, code);

            if (isValid) {
                log.info("✅ OTP verified for {}", email);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "verified", true,
                        "message", "Verification successful"
                ));
            } else {
                log.warn("❌ Invalid OTP for {}", email);
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "verified", false,
                        "error", "Invalid or expired code"
                ));
            }

        } catch (Exception ex) {
            log.error("Error verifying OTP: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Verification failed"
            ));
        }
    }
}
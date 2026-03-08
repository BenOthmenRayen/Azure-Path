package com.example.demo.controller;

import com.example.demo.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class RecaptchaController {

    private final RecaptchaService recaptchaService;

    public RecaptchaController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    /**
     * Standalone endpoint to test reCAPTCHA without login / Firebase.
     */
    @PostMapping("/recaptcha")
    public ResponseEntity<Map<String, Object>> verifyRecaptcha(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {

        Map<String, Object> response = new HashMap<>();

        // ✅ guard: body must exist
        if (body == null) {
            response.put("success", false);
            response.put("error", "Missing request body");
            return ResponseEntity.badRequest().body(response);
        }

        String token = body.get("token");
        String action = body.get("action");

        // ✅ guard: token required
        if (token == null || token.isBlank()) {
            response.put("success", false);
            response.put("error", "token is required");
            return ResponseEntity.badRequest().body(response);
        }

        String remoteIp = request.getRemoteAddr();
        boolean ok = recaptchaService.isValid(token, action, remoteIp);

        // ❌ recaptcha invalid
        if (!ok) {
            response.put("success", false);
            response.put("valid", false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // ✅ recaptcha valid
        response.put("success", true);
        response.put("valid", true);

        return ResponseEntity.ok(response);
    }
}

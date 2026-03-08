package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory verification storage.
 * For production prefer Redis (TTL) or a DB table with expiresAt and attempt counters.
 */
@Service
public class VerificationService {

    private static class CodeEntry {
        final String code;
        final Instant expiresAt;
        int attempts = 0;
        CodeEntry(String code, Instant expiresAt) {
            this.code = code; this.expiresAt = expiresAt;
        }
    }

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateSixDigitCode() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    public void saveCodeForEmail(String email, String code, Duration ttl) {
        Instant expires = Instant.now().plus(ttl);
        store.put(email.toLowerCase(), new CodeEntry(code, expires));
    }

    public boolean verifyCode(String email, String code) {
        CodeEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        // optional brute-force prevention
        if (entry.attempts >= 6) {
            store.remove(email.toLowerCase());
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            store.remove(email.toLowerCase());
            return false;
        }
        entry.attempts++;
        boolean ok = entry.code.equals(code);
        if (ok) store.remove(email.toLowerCase());
        return ok;
    }
}

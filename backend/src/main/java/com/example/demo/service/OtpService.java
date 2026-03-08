package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    // In-memory store: uid -> OtpRecord
    private final Map<String, OtpRecord> store = new ConcurrentHashMap<>();

    // Configurable values
    private final int otpLength = 6;
    private final int expiresMinutes = 5;
    private final int maxAttempts = 3;
    private final long resendCooldownSeconds = 60;

    public String generateOtpFor(String uid, String email) {
        // rate limit resend
        OtpRecord existing = store.get(uid);
        if (existing != null && Instant.now().isBefore(existing.createdAt.plusSeconds(resendCooldownSeconds))) {
            throw new IllegalStateException("You can request a new code after cooldown.");
        }

        int num = secureRandom.nextInt(1_000_000); // 0..999999
        String otp = String.format("%06d", num);

        String hashed = encoder.encode(otp);
        OtpRecord rec = new OtpRecord(hashed, Instant.now(), 0);
        store.put(uid, rec);
        return otp; // return plain for emailing; NEVER expose this otherwise
    }

    public boolean verifyOtp(String uid, String providedOtp) {
        OtpRecord rec = store.get(uid);
        if (rec == null) return false;

        // check expiry
        Instant expiresAt = rec.createdAt.plusSeconds(expiresMinutes * 60L);
        if (Instant.now().isAfter(expiresAt)) {
            store.remove(uid);
            return false;
        }

        if (rec.attempts >= maxAttempts) {
            store.remove(uid);
            return false;
        }

        boolean matches = encoder.matches(providedOtp, rec.hashedOtp);
        if (matches) {
            store.remove(uid);
            return true;
        } else {
            rec.attempts++;
            store.put(uid, rec);
            if (rec.attempts >= maxAttempts) {
                store.remove(uid); // invalidate after too many attempts
            }
            return false;
        }
    }

    // For debugging/testing only
    public void clearOtp(String uid) { store.remove(uid); }

    private static class OtpRecord {
        final String hashedOtp;
        final Instant createdAt;
        int attempts;
        OtpRecord(String hashedOtp, Instant createdAt, int attempts) {
            this.hashedOtp = hashedOtp;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }
    }
}

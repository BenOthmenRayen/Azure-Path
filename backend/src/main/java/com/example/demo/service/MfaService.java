package com.example.demo.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.*;

@Service
public class MfaService {

    private final ConcurrentHashMap<String, String> codes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> expiryTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final SecureRandom rnd = new SecureRandom();
    private final long ttlMinutes = 5L;

    /** Generate 6-digit code and store it temporarily */
    public String generateAndStore(String email) {
        int n = rnd.nextInt(1_000_000);
        String code = String.format("%06d", n);
        codes.put(email, code);

        // cancel any previous scheduled removal
        ScheduledFuture<?> prev = expiryTasks.remove(email);
        if (prev != null) prev.cancel(false);

        // schedule removal after TTL
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            codes.remove(email);
            expiryTasks.remove(email);
        }, ttlMinutes, TimeUnit.MINUTES);

        expiryTasks.put(email, future);
        return code;
    }

    /** Verify the code */
    public boolean verify(String email, String code) {
        String stored = codes.get(email);
        if (stored == null) return false;
        boolean ok = stored.equals(code);
        if (ok) {
            codes.remove(email); // one-time use
            ScheduledFuture<?> f = expiryTasks.remove(email);
            if (f != null) f.cancel(false);
        }
        return ok;
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}

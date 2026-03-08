// src/main/java/com/example/demo/service/ActivityLogService.java
package com.example.demo.service;

import com.example.demo.dto.ActivityLogResponse;
import com.example.demo.model.ActivityLog;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityLogService {

    private final DatabaseReference logsRef;

    public ActivityLogService() {
        this.logsRef = FirebaseDatabase.getInstance().getReference("activity_logs");
    }

    /**
     * Log an admin action to Firebase Realtime Database
     */
    public void logAction(String adminUid, String adminEmail, String action,
                          String targetUid, String targetEmail, String details) {
        try {
            ActivityLog log = new ActivityLog(adminUid, adminEmail, action, targetUid, targetEmail, details);

            // Generate unique ID using Firebase push()
            String logId = logsRef.push().getKey();
            if (logId != null) {
                log.setId(logId);
                logsRef.child(logId).setValueAsync(log);
                System.out.println("Activity logged: " + action + " by " + adminEmail);
            }
        } catch (Exception e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get recent activity logs (synchronous version for REST endpoint)
     * Returns up to 'limit' most recent logs
     */
    public List<ActivityLogResponse> getRecentLogs(int limit) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        List<ActivityLogResponse> logs = new ArrayList<>();
        Exception[] error = new Exception[1];

        logsRef.orderByChild("timestamp")
                .limitToLast(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                try {
                                    ActivityLog log = child.getValue(ActivityLog.class);
                                    if (log != null) {
                                        // Parse timestamp string to LocalDateTime
                                        LocalDateTime timestamp = LocalDateTime.parse(log.getTimestamp());

                                        logs.add(new ActivityLogResponse(
                                                child.getKey(),
                                                log.getAdminEmail() != null ? log.getAdminEmail() : "Unknown",
                                                log.getAction() != null ? log.getAction() : "UNKNOWN",
                                                log.getTargetEmail() != null ? log.getTargetEmail() : "Unknown",
                                                log.getDetails() != null ? log.getDetails() : "",
                                                timestamp
                                        ));
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error parsing log entry: " + e.getMessage());
                                }
                            }

                            // Sort newest first (reverse chronological order)
                            logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

                            System.out.println("Retrieved " + logs.size() + " activity logs");
                        } finally {
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        error[0] = databaseError.toException();
                        System.err.println("Firebase error retrieving logs: " + databaseError.getMessage());
                        latch.countDown();
                    }
                });

        // Wait for Firebase callback (with 10 second timeout)
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new Exception("Timeout waiting for Firebase logs");
        }

        if (error[0] != null) {
            throw error[0];
        }

        return logs;
    }
}
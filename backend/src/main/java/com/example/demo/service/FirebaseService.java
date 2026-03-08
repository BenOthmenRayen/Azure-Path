package com.example.demo.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Service helper to interact synchronously with Firebase Auth and Realtime Database.
 */
@Service
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase firebaseDatabase;

    private static final long DB_WRITE_TIMEOUT_SEC = 15;
    private static final long DB_READ_TIMEOUT_SEC = 10;

    public FirebaseService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    // --------------------------------------------------
    // Create user
    // --------------------------------------------------

    public String createUserAndSetRole(String name, String email, String password, String role)
            throws FirebaseAuthException {

        UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(name)
                .setEmailVerified(false);

        UserRecord userRecord = firebaseAuth.createUser(req);
        String uid = userRecord.getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("name", name);
        map.put("email", email);
        map.put("role", role);
        map.put("status", "PENDING_EMAIL_VERIFICATION");
        map.put("createdAt", System.currentTimeMillis());

        DatabaseReference ref = firebaseDatabase.getReference("users").child(uid);

        try {
            ApiFuture<Void> future = ref.updateChildrenAsync(map);
            future.get(DB_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error writing user data to DB for uid={}", uid, e);
            throw new RuntimeException("Error writing user data to DB", e);
        }

        log.info("Created Firebase user uid={} email={} role={}", uid, email, role);
        return uid;
    }

    // --------------------------------------------------
    // Auth helpers
    // --------------------------------------------------

    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(idToken);
    }

    public String verifyIdTokenAndGetUid(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken).getUid();
        } catch (FirebaseAuthException e) {
            log.warn("Invalid Firebase idToken", e);
            return null;
        }
    }

    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        return firebaseAuth.getUserByEmail(email);
    }

    public UserRecord getUserByUid(String uid) throws FirebaseAuthException {
        return firebaseAuth.getUser(uid);
    }

    public String createCustomToken(String uid) throws FirebaseAuthException {
        return firebaseAuth.createCustomToken(uid);
    }

    public String createCustomTokenForEmail(String email) throws FirebaseAuthException {
        return createCustomToken(getUserByEmail(email).getUid());
    }

    // --------------------------------------------------
    // Status & role reads
    // --------------------------------------------------

    public void markUserStatusActiveByEmail(String email) throws FirebaseAuthException {
        UserRecord user = getUserByEmail(email);
        String uid = user.getUid();

        DatabaseReference ref = firebaseDatabase.getReference("users")
                .child(uid)
                .child("status");

        try {
            ref.setValueAsync("ACTIVE")
                    .get(DB_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to mark user ACTIVE", ex);
        }
    }

    public String getRoleForUid(String uid) {
        DatabaseReference ref = firebaseDatabase.getReference("users").child(uid).child("role");
        CountDownLatch latch = new CountDownLatch(1);
        final String[] role = {null};

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) role[0] = String.valueOf(snapshot.getValue());
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(DB_READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return role[0];
    }

    // --------------------------------------------------
    // Profiles
    // --------------------------------------------------

    public Map<String, Object> getProfile(String uid) {
        DatabaseReference ref = firebaseDatabase.getReference("users").child(uid);
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> result = new HashMap<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                snapshot.getChildren().forEach(c -> result.put(c.getKey(), c.getValue()));
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(DB_READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    // --------------------------------------------------
    // Admin helpers
    // --------------------------------------------------

    public List<Map<String, Object>> listAllUserProfiles() {
        DatabaseReference ref = firebaseDatabase.getReference("users");
        CountDownLatch latch = new CountDownLatch(1);
        List<Map<String, Object>> users = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                snapshot.getChildren().forEach(child -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("uid", child.getKey());
                    child.getChildren().forEach(c -> m.put(c.getKey(), c.getValue()));
                    users.add(m);
                });
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(DB_READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return users;
    }

    public void deleteUserByUid(String uid) {
        try {
            firebaseAuth.deleteUser(uid);
            log.info("Firebase Auth user deleted uid={}", uid);
        } catch (FirebaseAuthException e) {
            if (!"USER_NOT_FOUND".equals(e.getErrorCode())) {
                throw new RuntimeException("Failed deleting Firebase Auth user", e);
            }
            log.warn("Auth user already deleted uid={}", uid);
        }

        try {
            firebaseDatabase.getReference("users")
                    .child(uid)
                    .removeValueAsync()
                    .get(DB_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed deleting DB user", e);
        }
    }

    public void updateUserRole(String uid, String newRole) {

        try {
            firebaseDatabase.getReference("users")
                    .child(uid)
                    .child("role")
                    .setValueAsync(newRole)
                    .get(DB_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed updating role in DB", e);
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", newRole.replace("ROLE_", ""));
            firebaseAuth.setCustomUserClaims(uid, claims);
        } catch (FirebaseAuthException e) {
            if ("USER_NOT_FOUND".equals(e.getErrorCode())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User does not exist in Firebase Auth"
                );
            }
            throw new RuntimeException("Failed updating Firebase claims", e);
        }
    }

    public long countUsers() {
        DatabaseReference ref = firebaseDatabase.getReference("users");
        CountDownLatch latch = new CountDownLatch(1);
        final long[] count = {0};

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                count[0] = snapshot.exists() ? snapshot.getChildrenCount() : 0;
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(DB_READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return count[0];
    }
}

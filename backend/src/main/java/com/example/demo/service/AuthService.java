package com.example.demo.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final FirebaseService firebaseService;

    public AuthService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    // Create a user in Firebase Auth + Realtime DB with a role
    public String registerUserAndSetRole(String name, String email, String password, String role)
            throws FirebaseAuthException {
        return firebaseService.createUserAndSetRole(name, email, password, role);
    }

    // Verify ID token
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        return firebaseService.verifyIdToken(idToken);
    }

    // Fetch full user profile
    public Map<String, Object> getProfile(String uid) {
        return firebaseService.getProfile(uid);
    }

    // Fetch role only
    public String getRoleForUid(String uid) {
        return firebaseService.getRoleForUid(uid);
    }
}

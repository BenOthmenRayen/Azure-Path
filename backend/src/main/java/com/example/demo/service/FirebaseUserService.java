package com.example.demo.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FirebaseUserService {

    public UserRecord getUserByUid(String uid) throws Exception {
        return FirebaseAuth.getInstance().getUser(uid);
    }

    // Return Iterable<ExportedUserRecord> because listUsers().getValues() yields ExportedUserRecord
    public Iterable<ExportedUserRecord> getAllUsers() throws Exception {
        return FirebaseAuth.getInstance().listUsers(null).getValues();
    }

    public void updateUserRole(String uid, String newRole) throws Exception {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setCustomClaims(Map.of("role", newRole));

        FirebaseAuth.getInstance().updateUser(request);
    }
}

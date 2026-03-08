package com.example.demo.service;

import com.example.demo.dto.UpdateUserRoleRequest;
import com.example.demo.dto.UserProfileResponse;
import com.google.firebase.auth.ExportedUserRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserManagementService {

    private final FirebaseUserService firebaseUserService;

    public AdminUserManagementService(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
    }

    public List<UserProfileResponse> getAllUsers() throws Exception {
        List<UserProfileResponse> users = new ArrayList<>();

        for (ExportedUserRecord user : firebaseUserService.getAllUsers()) {
            Map<String, Object> claims = user.getCustomClaims();
            String role = (claims != null && claims.get("role") != null)
                    ? claims.get("role").toString()
                    : "user";

            users.add(new UserProfileResponse(
                    user.getUid(),
                    user.getEmail(),
                    role
            ));
        }
        return users;
    }

    public void updateRole(UpdateUserRoleRequest request) throws Exception {
        firebaseUserService.updateUserRole(request.getUid(), request.getNewRole());
    }
}
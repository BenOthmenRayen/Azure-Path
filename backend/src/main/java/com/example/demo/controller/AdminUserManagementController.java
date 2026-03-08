package com.example.demo.controller;

import com.example.demo.dto.UpdateUserRoleRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.service.AdminUserManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserService;

    public AdminUserManagementController(AdminUserManagementService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/")
    public List<UserProfileResponse> getAllUsers() throws Exception {
        return adminUserService.getAllUsers();
    }

    @PostMapping("/update-role")
    public String updateRole(@RequestBody UpdateUserRoleRequest request) throws Exception {
        adminUserService.updateRole(request);
        return "Role updated successfully!";
    }
}
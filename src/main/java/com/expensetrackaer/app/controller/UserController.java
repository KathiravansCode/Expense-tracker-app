package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.ApiResponse;
import com.expensetrackaer.app.entity.dto.ChangePasswordRequest;
import com.expensetrackaer.app.entity.dto.UpdateProfileRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;
import com.expensetrackaer.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/v1/users/profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile() {

        UserResponse response = userService.getProfile();

        return ResponseEntity.ok(
                new ApiResponse(true, "Profile fetched successfully", response)
        );
    }

    // PUT /api/v1/users/profile
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateProfile(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Profile updated successfully", response)
        );
    }

    // PUT /api/v1/users/change-password
    // Requires current password — use /auth/forgot-password if password is forgotten
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Password changed successfully", null)
        );
    }

    // DELETE /api/v1/users/account
    // Permanently deletes the account and all associated data
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse> deleteAccount() {

        userService.deleteAccount();

        return ResponseEntity.ok(
                new ApiResponse(true, "Account deleted successfully", null)
        );
    }
}
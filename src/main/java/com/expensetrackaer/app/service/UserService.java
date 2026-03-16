package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.ChangePasswordRequest;
import com.expensetrackaer.app.entity.dto.UpdateProfileRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;

public interface UserService {

    // Get the logged-in user's profile info
    UserResponse getProfile();

    // Update name and/or email
    UserResponse updateProfile(UpdateProfileRequest request);

    // Change password while logged in — requires current password verification
    void changePassword(ChangePasswordRequest request);

    // Permanently delete the account and all associated data
    void deleteAccount();
}

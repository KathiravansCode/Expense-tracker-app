package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.ChangePasswordRequest;
import com.expensetrackaer.app.entity.dto.UpdateProfileRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    // ── Get Profile ───────────────────────────────────────────────
    @Override
    public UserResponse getProfile() {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }

    // ── Update Profile ────────────────────────────────────────────
    // Allows updating name and email
    // If email is changed, checks no other account already uses that email
    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If email is being changed, check it's not already taken by another user
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessValidationException(
                        "This email is already associated with another account");
            }
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        User updated = userRepository.save(user);

        return mapToResponse(updated);
    }

    // ── Change Password ───────────────────────────────────────────
    // User must be logged in and must provide their current password
    // This is different from forgot-password which uses a reset token
    @Override
    public void changePassword(ChangePasswordRequest request) {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password matches what's stored
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessValidationException("Current password is incorrect");
        }

        // Check new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessValidationException(
                    "New password and confirm password do not match");
        }

        // Prevent setting the same password as current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessValidationException(
                    "New password cannot be the same as your current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Delete Account ────────────────────────────────────────────
    // Permanently deletes the user and all their data
    // Cascades defined in User entity handle related data:
    // transactions, budgets, alerts are all deleted automatically
    // Categories owned by this user are also deleted
    // Global categories (user IS NULL) are NOT affected
    @Override
    public void deleteAccount() {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
    }

    // ── Mapper ────────────────────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
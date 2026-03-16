package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.*;
import com.expensetrackaer.app.entity.model.PasswordResetToken;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.PasswordResetTokenRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.security.JwtUtil;
import com.expensetrackaer.app.service.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordResetTokenRepository resetTokenRepository,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(RegisterUserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessValidationException("User already exists with this email");
        }

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // ✅ No category seeding here anymore.
        // Global default categories are seeded ONCE by DatabaseSeeder at app startup
        // and are shared across all users — no per-user duplication needed.

        return mapToUser(savedUser);
    }

    private UserResponse mapToUser(User savedUser) {
        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail()) // ✅ Fixed: was getPassword() before
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    // ── Login ──────────────────────────────────────────────────────
    @Override
    public LoginResponse login(LoginRequest request) {

        try {
            // Spring Security validates email + password against DB
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BusinessValidationException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate token with email + userId embedded
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // ── Forgot Password ───────────────────────────────────────────
    // Since there's no frontend yet, we return the token directly in
    // the response so you can test in Postman.
    // When frontend is ready: replace the return with an email call.
    @Override
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        // Delete any existing reset token for this user before creating a new one
        resetTokenRepository.deleteByUserId(user.getId());

        // Generate a secure random UUID token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // expires in 15 minutes
                .build();

        resetTokenRepository.save(resetToken);

        // TODO: When frontend is ready, replace this with:
        // emailService.sendResetEmail(user.getEmail(), token);
        // and return: new ApiResponse(true, "Reset link sent to your email", null);

        return new ApiResponse(
                true,
                "Use this token to reset your password (valid for 15 minutes)",
                Map.of("resetToken", token)
        );
    }

    // ── Reset Password ────────────────────────────────────────────
    @Override
    public ApiResponse resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = resetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new BusinessValidationException("Invalid or expired reset token"));

        // Check if already used
        if (resetToken.getIsUsed()) {
            throw new BusinessValidationException("This reset token has already been used");
        }

        // Check if expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessValidationException("Reset token has expired. Please request a new one");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used so it can't be reused
        resetToken.setIsUsed(true);
        resetTokenRepository.save(resetToken);

        return new ApiResponse(true, "Password reset successfully. Please login with your new password", null);
    }
}
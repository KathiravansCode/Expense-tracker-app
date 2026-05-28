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
import com.expensetrackaer.app.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Injected email infrastructure module

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordResetTokenRepository resetTokenRepository,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        return mapToUser(savedUser);
    }

    private UserResponse mapToUser(User savedUser) {
        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
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

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // ── Secure Forgot Password Implementation ───────────────────────────────────────────
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

        // Dispatches email asynchronously to user profile mailbox endpoints safely
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getName());

        // Secure Response: Token omitted from data node block payload architectures entirely
        return new ApiResponse(
                true,
                "If the email address matches an active profile, a secure validation token has been successfully sent.",
                null
        );
    }

    @Override
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new BusinessValidationException("Invalid or expired reset token"));

        if (resetToken.getIsUsed()) {
            throw new BusinessValidationException("This reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessValidationException("Reset token has expired. Please request a new one");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        resetTokenRepository.save(resetToken);

        return new ApiResponse(true, "Password reset successfully. Please login with your new password", null);
    }
}
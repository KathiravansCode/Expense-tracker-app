package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.*;
import com.expensetrackaer.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/v1/auth/login
    // Returns JWT token on successful login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Login successful", response)
        );
    }

    // POST /api/v1/auth/forgot-password
    // Returns reset token directly (no email yet — frontend not built)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        ApiResponse response = authService.forgotPassword(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterUserRequest userRequest){
        UserResponse response=authService.registerUser(userRequest);

        return new ResponseEntity<>(new ApiResponse(true,"User registered Successfully",response), HttpStatus.CREATED);
    }

    // POST /api/v1/auth/reset-password
    // Validates reset token and updates password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        ApiResponse response = authService.resetPassword(request);

        return ResponseEntity.ok(response);
    }
}
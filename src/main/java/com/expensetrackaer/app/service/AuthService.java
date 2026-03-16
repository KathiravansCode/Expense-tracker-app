package com.expensetrackaer.app.service;


import com.expensetrackaer.app.entity.dto.*;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    ApiResponse forgotPassword(ForgotPasswordRequest request);

    ApiResponse resetPassword(ResetPasswordRequest request);

    UserResponse registerUser(RegisterUserRequest userRequest);
}
package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.RegisterUserRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(RegisterUserRequest userRequest);
}

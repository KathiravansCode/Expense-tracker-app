package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.RegisterUserRequest;
import com.expensetrackaer.app.entity.dto.UserResponse;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EmptyStackException;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    @Override
    public UserResponse registerUser(RegisterUserRequest userRequest) {
//        if(userRepository.existsByEmail(userRequest.getEmail())){
//
//        }
        User user=User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .build();

        User savedUser=userRepository.save(user);

        return mapToUser(savedUser);
    }

    private UserResponse mapToUser(User savedUser){
          return UserResponse.builder()
                  .id(savedUser.getId())
                  .name(savedUser.getName())
                  .email(savedUser.getPassword())
                  .createdAt(savedUser.getCreatedAt())
                  .build();
    }
}

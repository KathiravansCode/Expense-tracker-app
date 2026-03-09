package com.expensetrackaer.app.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {
    @NotBlank(message="name is required")
    private String name;

    @NotBlank(message="Email is required")
    @Email(message="Invalid Email format")
    private String email;


    @NotBlank(message="Password is required")
    @Size(max=6,message="Password must be atleast 6 characters")
    private String password;
}

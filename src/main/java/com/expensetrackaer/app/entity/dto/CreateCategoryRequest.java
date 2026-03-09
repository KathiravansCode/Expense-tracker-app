package com.expensetrackaer.app.entity.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 50)
    private String name;
}



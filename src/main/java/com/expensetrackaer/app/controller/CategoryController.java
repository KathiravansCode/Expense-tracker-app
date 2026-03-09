package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.ApiResponse;
import com.expensetrackaer.app.entity.dto.CategoryResponse;
import com.expensetrackaer.app.entity.dto.CreateCategoryRequest;
import com.expensetrackaer.app.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CreateCategoryRequest categoryRequest){
        CategoryResponse response=categoryService.createCategory(categoryRequest);
        ApiResponse apiResponse=new ApiResponse(true,"Category created Successfully",response);
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCategories(){
        List<CategoryResponse> responses=categoryService.getCategories();

        ApiResponse response=new ApiResponse(true,"Categories fetched successfully",responses);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id,@Valid @RequestBody CreateCategoryRequest categoryRequest){
          CategoryResponse categoryResponse = categoryService.updateCategory(id,categoryRequest);
          ApiResponse response=new ApiResponse(true,"Category updated successfully",categoryResponse);

          return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true,"Category Deleted Successfully"));
    }

}

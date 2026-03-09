package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.CategoryResponse;
import com.expensetrackaer.app.entity.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> getCategories();

    CategoryResponse updateCategory(Long id, CreateCategoryRequest request);

    void deleteCategory(Long id);


}

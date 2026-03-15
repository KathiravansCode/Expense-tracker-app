package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryResponse;
import com.expensetrackaer.app.entity.dto.CreateCategoryRequest;
import com.expensetrackaer.app.entity.model.Category;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,TransactionRepository transactionRepository,UserRepository userRepository){
        this.categoryRepository=categoryRepository;
        this.transactionRepository=transactionRepository;
        this.userRepository=userRepository;
    }
    private Long getCurrentUserId() {

        return 1L; // temporary until JWT
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
         Long userId=getCurrentUserId();

        if(categoryRepository.existsByNameAndUserId(request.getName(),userId)){
            throw new BusinessValidationException("Category Already exists for the user");
        }

         User user= userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found"));

         Category category=new Category();
         category.setName(request.getName());
         category.setUser(user);

         Category savedCategory= categoryRepository.save(category);

         return  new CategoryResponse(savedCategory.getId(), savedCategory.getName());

    }

    @Override
    public List<CategoryResponse> getCategories() {

        Long userId=getCurrentUserId();

        return categoryRepository.findById(userId).stream().
                map((category)->new CategoryResponse(category.getId(), category.getName())).
                toList();
    }

    @Override
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {

        Long userId=getCurrentUserId();

        Category category=categoryRepository.findByIdAndUserId(id,userId).
                orElseThrow(()->new ResourceNotFoundException("Category not found"));


        category.setName(request.getName());

        Category updated=categoryRepository.save(category);

        return new CategoryResponse(updated.getId(), updated.getName());

    }

    @Override
    public void deleteCategory(Long id) {

        Long userId=getCurrentUserId();

        Category category=categoryRepository.findByIdAndUserId(id,userId).
                orElseThrow(()->new ResourceNotFoundException("category not found"));
        //check if the category is used in transactions or not.

        if(transactionRepository.existsByCategory_Id(id)){
            throw new BusinessValidationException("Category is associated with transaction");
        }

        categoryRepository.delete(category);


    }
}

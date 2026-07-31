package com.spendsense.category.service;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.DuplicateCategoryException;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        User currentUser = getCurrentUser();

        String categoryName = request.getName().trim();

        boolean exists = categoryRepository
                .existsByNameAndCreatedBy(
                        categoryName,
                        currentUser
                );

        if(exists){
            throw new DuplicateCategoryException("Category already exists");
        }

        String icon = request.getIcon();

        if(icon == null || icon.isBlank()){
            icon = "category";
        }

        String color = request.getColor();

        if(color == null || color.isBlank()){
            color = "#757575";
        }

        Category category = new Category(
                request.getName(),
                icon,
                color,
                false,
                currentUser
        );

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }


    public List<CategoryResponse> getCategories(){

        User currentUser = getCurrentUser();

        List<Category> defaultCategories =
                categoryRepository.findByIsDefaultTrue();


        List<Category> userCategories =
                categoryRepository.findByCreatedBy(currentUser);


        List<Category> allCategories = new ArrayList<>();

        allCategories.addAll(defaultCategories);
        allCategories.addAll(userCategories);


        return allCategories
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getCurrentUser(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    private CategoryResponse mapToResponse(Category category){

        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                category.isDefault()
        );
    }
}

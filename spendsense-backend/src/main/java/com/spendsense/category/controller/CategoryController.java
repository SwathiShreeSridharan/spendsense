package com.spendsense.category.controller;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable UUID groupId , @Valid @RequestBody CreateCategoryRequest request){
        CategoryResponse response =
                categoryService.createCategory(groupId,request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@PathVariable UUID groupId){
        List<CategoryResponse> response =
                categoryService.getCategories(groupId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }
}

package com.spendsense.category.controller;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request){
        CategoryResponse response =
                categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(){
        List<CategoryResponse> response =
                categoryService.getCategories();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }
}

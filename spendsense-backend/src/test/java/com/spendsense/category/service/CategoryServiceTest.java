package com.spendsense.category.service;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.DuplicateCategoryException;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;


    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private CategoryService categoryService;


    private User user;


    @BeforeEach
    void setup(){

        user = new User();

        user.setEmail("swathi@gmail.com");

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null
                        )
                );
    }

    @Test
    void shouldCreateCategorySuccessfully(){

        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        null,
                        null
                );


        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));


        when(categoryRepository
                .existsByNameAndCreatedBy(
                        "Pet Care",
                        user
                ))
                .thenReturn(false);


        Category savedCategory =
                new Category(
                        "Pet Care",
                        "category",
                        "#757575",
                        false,
                        user
                );


        savedCategory.setCategoryId(UUID.randomUUID());

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response =
                categoryService.createCategory(request);

        assertNotNull(response);

        assertEquals(
                "Pet Care",
                response.getName()
        );

        verify(categoryRepository)
                .save(any(Category.class));
    }

    @Test
    void shouldNotCreateDuplicateCategory(){

        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        null,
                        null
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(categoryRepository.existsByNameAndCreatedBy(
                "Pet Care",
                user
        )).thenReturn(true);

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void shouldApplyDefaultIconAndColor(){
        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        "",
                        ""
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(categoryRepository.existsByNameAndCreatedBy(
                "Pet Care",
                user
        )).thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response =
                categoryService.createCategory(request);

        assertEquals(
                "category",
                response.getIcon()
        );

        assertEquals(
                "#757575",
                response.getColor()
        );
    }
}

package com.spendsense.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.service.CategoryService;
import com.spendsense.security.CustomUserDetailsService;
import com.spendsense.security.JwtAuthenticationFilter;
import com.spendsense.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {
        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        null,
                        null
                );
        CategoryResponse response =
                new CategoryResponse(
                        UUID.randomUUID(),
                        "Pet Care",
                        "category",
                        "#757575",
                        false
                );
        when(categoryService.createCategory(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Pet Care"))
                .andExpect(jsonPath("$.default")
                        .value(false));
        verify(categoryService)
                .createCategory(any());
    }

    @Test
    void shouldGetCategoriesSuccessfully() throws Exception {
        CategoryResponse food =
                new CategoryResponse(
                        UUID.randomUUID(),
                        "Food",
                        "restaurant",
                        "#4CAF50",
                        true
                );

        CategoryResponse petCare =
                new CategoryResponse(
                        UUID.randomUUID(),
                        "Pet Care",
                        "category",
                        "#757575",
                        false
                );

        when(categoryService.getCategories())
                .thenReturn(
                        List.of(food, petCare)
                );

        mockMvc.perform(
                        get("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(2))

                .andExpect(jsonPath("$[0].name")
                        .value("Food"))

                .andExpect(jsonPath("$[0].default")
                        .value(true))

                .andExpect(jsonPath("$[1].name")
                        .value("Pet Care"))

                .andExpect(jsonPath("$[1].default")
                        .value(false));
        verify(categoryService)
                .getCategories();
    }
}

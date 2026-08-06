package com.spendsense.expense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
import com.spendsense.expense.dto.UpdateExpenseRequest;
import com.spendsense.expense.service.ExpenseService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ExpenseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    UUID groupId = UUID.randomUUID();

    @Test
    void shouldCreateExpenseSuccessfully() throws Exception {

        UUID categoryId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId
                );

        ExpenseResponse response =
                new ExpenseResponse(
                        UUID.randomUUID(),
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId,
                        "Food",
                        "restaurant",
                        "#4CAF50",
                        true
                );

        when(expenseService.createExpense(eq(groupId), any(CreateExpenseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/groups/{groupId}/expenses",groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.title")
                        .value("Lunch"))

                .andExpect(jsonPath("$.amount")
                        .value(250))

                .andExpect(jsonPath("$.categoryName")
                        .value("Food"));

        verify(expenseService)
                .createExpense(eq(groupId),any(CreateExpenseRequest.class));
    }

    @Test
    void shouldGetExpensesSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();

        ExpenseResponse lunch =
                new ExpenseResponse(
                        UUID.randomUUID(),
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        UUID.randomUUID(),
                        "Food",
                        "restaurant",
                        "#4CAF50",
                        true
                );

        ExpenseResponse movie =
                new ExpenseResponse(
                        UUID.randomUUID(),
                        "Movie",
                        "Weekend",
                        BigDecimal.valueOf(300),
                        LocalDate.now(),
                        UUID.randomUUID(),
                        "Entertainment",
                        "movie",
                        "#9C27B0",
                        true
                );

        when(expenseService.getExpenses(groupId))
                .thenReturn(List.of(lunch, movie));

        mockMvc.perform(
                        get("/api/v1/groups/{groupId}/expenses",groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()")
                        .value(2))

                .andExpect(jsonPath("$[0].title")
                        .value("Lunch"))

                .andExpect(jsonPath("$[1].title")
                        .value("Movie"));

        verify(expenseService)
                .getExpenses(groupId);
    }

    @Test
    void shouldUpdateExpenseSuccessfully() throws Exception {
        UUID expenseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseRequest request =
                new UpdateExpenseRequest(
                        "Updated lunch",
                        "Updated description",
                        new BigDecimal("450.00"),
                        LocalDate.of(2026, 8, 5),
                        categoryId
                );

        ExpenseResponse response =
                new ExpenseResponse(
                        expenseId,
                        "Updated lunch",
                        "Updated description",
                        new BigDecimal("450.00"),
                        LocalDate.of(2026, 8, 5),
                        categoryId,
                        "Food",
                        "restaurant",
                        "#4CAF50",
                        false
                );

        when(expenseService.updateExpense(
                eq(groupId),
                eq(expenseId),
                any(UpdateExpenseRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/groups/{groupId}/expenses/{expenseId}",
                                groupId,
                                expenseId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.expenseId")
                        .value(expenseId.toString()))

                .andExpect(jsonPath("$.title")
                        .value("Updated lunch"))

                .andExpect(jsonPath("$.amount")
                        .value(450.00))

                .andExpect(jsonPath("$.categoryName")
                        .value("Food"));

        verify(expenseService).updateExpense(
                eq(groupId),
                eq(expenseId),
                any(UpdateExpenseRequest.class)
        );
    }

    @Test
    void shouldArchiveExpenseSuccessfully() throws Exception {
        UUID expenseId = UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/groups/{groupId}/expenses/{expenseId}",
                                groupId,
                                expenseId
                        )
                )
                .andExpect(status().isNoContent());

        verify(expenseService).archiveExpense(
                groupId,
                expenseId
        );
    }

}

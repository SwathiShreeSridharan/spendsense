package com.spendsense.expense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        when(expenseService.createExpense(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/expenses")
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
                .createExpense(any());
    }

    @Test
    void shouldGetExpensesSuccessfully() throws Exception {

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

        when(expenseService.getExpenses())
                .thenReturn(List.of(lunch, movie));

        mockMvc.perform(
                        get("/api/v1/expenses")
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
                .getExpenses();
    }

}

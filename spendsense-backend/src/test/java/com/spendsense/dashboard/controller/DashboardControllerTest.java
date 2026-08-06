package com.spendsense.dashboard.controller;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.dashboard.dto.CategorySummaryResponse;
import com.spendsense.dashboard.dto.DashboardSummaryResponse;
import com.spendsense.dashboard.dto.MonthlyExpenseResponse;
import com.spendsense.dashboard.service.DashboardService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnDashboardSummary() throws Exception {

        BudgetResponse activeBudget =
                new BudgetResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Family",
                        new BigDecimal("10000.00"),
                        new BigDecimal("2500.00"),
                        new BigDecimal("7500.00"),
                        new BigDecimal("25.00"),
                        false,
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        UUID.randomUUID(),
                        "Swathi",
                        LocalDateTime.of(2026, 8, 1, 10, 0)
                );

        DashboardSummaryResponse response =
                new DashboardSummaryResponse(
                        BigDecimal.valueOf(500),
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(25000),
                        40L,
                        List.of(activeBudget)
                );

        when(dashboardService.getSummary())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.todayExpense")
                        .value(500))

                .andExpect(jsonPath("$.monthExpense")
                        .value(8000))

                .andExpect(jsonPath("$.totalExpense")
                        .value(25000))

                .andExpect(jsonPath("$.expenseCount")
                        .value(40))

                .andExpect(jsonPath("$.activeBudgets.length()")
                        .value(1))

                .andExpect(jsonPath("$.activeBudgets[0].groupName")
                        .value("Family"))

                .andExpect(jsonPath("$.activeBudgets[0].budgetAmount")
                        .value(10000.00))

                .andExpect(jsonPath("$.activeBudgets[0].spentAmount")
                        .value(2500.00))

                .andExpect(jsonPath("$.activeBudgets[0].remainingAmount")
                        .value(7500.00))

                .andExpect(jsonPath("$.activeBudgets[0].percentageUsed")
                        .value(25.00))

                .andExpect(jsonPath("$.activeBudgets[0].exceeded")
                        .value(false));

        verify(dashboardService)
                .getSummary();
    }

    @Test
    void shouldReturnCategorySummary() throws Exception {
        when(dashboardService.getCategorySummary())
                .thenReturn(
                        List.of(
                                new CategorySummaryResponse(
                                        "Food",
                                        BigDecimal.valueOf(5000)
                                ),
                                new CategorySummaryResponse(
                                        "Travel",
                                        BigDecimal.valueOf(2500)
                                )
                        )
                );

        mockMvc.perform(
                        get("/api/v1/dashboard/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Food"))
                .andExpect(jsonPath("$[0].amount")
                        .value(5000))
                .andExpect(jsonPath("$[1].categoryName")
                        .value("Travel"))
                .andExpect(jsonPath("$[1].amount")
                        .value(2500));

        verify(dashboardService).getCategorySummary();
    }

    @Test
    void shouldReturnMonthlyExpenseSummary() throws Exception {
        when(dashboardService.getMonthlyExpenseSummary())
                .thenReturn(
                        List.of(
                                new MonthlyExpenseResponse(
                                        "Jan",
                                        BigDecimal.valueOf(4000)
                                ),
                                new MonthlyExpenseResponse(
                                        "Feb",
                                        BigDecimal.valueOf(6000)
                                )
                        )
                );

        mockMvc.perform(
                        get("/api/v1/dashboard/monthly")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].month")
                        .value("Jan"))
                .andExpect(jsonPath("$[0].amount")
                        .value(4000))
                .andExpect(jsonPath("$[1].month")
                        .value("Feb"))
                .andExpect(jsonPath("$[1].amount")
                        .value(6000));

        verify(dashboardService).getMonthlyExpenseSummary();
    }
}

package com.spendsense.dashboard.controller;

import com.spendsense.dashboard.dto.DashboardSummaryResponse;
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

        DashboardSummaryResponse response =
                new DashboardSummaryResponse(
                        BigDecimal.valueOf(500),
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(25000),
                        40L
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
                        .value(40));

        verify(dashboardService)
                .getSummary();
    }
}

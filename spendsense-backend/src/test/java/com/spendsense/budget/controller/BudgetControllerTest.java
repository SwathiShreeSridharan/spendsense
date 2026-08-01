package com.spendsense.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.budget.service.BudgetService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule());

    @Test
    void shouldCreateBudgetSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(30000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        BudgetResponse response =
                new BudgetResponse(
                        budgetId,
                        groupId,
                        "Family",
                        BigDecimal.valueOf(30000),
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(30000),
                        BigDecimal.ZERO,
                        false,
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        userId,
                        "Swathi",
                        LocalDateTime.now()
                );

        when(budgetService.createBudget(
                eq(groupId),
                any(CreateBudgetRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/groups/{groupId}/budgets", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetId")
                        .value(budgetId.toString()))
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.groupName")
                        .value("Family"))
                .andExpect(jsonPath("$.budgetAmount")
                        .value(30000))
                .andExpect(jsonPath("$.spentAmount")
                        .value(0))
                .andExpect(jsonPath("$.remainingAmount")
                        .value(30000))
                .andExpect(jsonPath("$.budgetType")
                        .value("MONTHLY"))
                .andExpect(jsonPath("$.exceeded")
                        .value(false));

        verify(budgetService)
                .createBudget(
                        eq(groupId),
                        any(CreateBudgetRequest.class)
                );
    }

    @Test
    void shouldGetGroupBudgetsSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();

        BudgetResponse monthlyBudget =
                createBudgetResponse(
                        UUID.randomUUID(),
                        groupId,
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        BudgetResponse yearlyBudget =
                createBudgetResponse(
                        UUID.randomUUID(),
                        groupId,
                        BudgetType.YEARLY,
                        BigDecimal.valueOf(300000),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31)
                );

        when(budgetService.getGroupBudgets(groupId))
                .thenReturn(
                        List.of(
                                monthlyBudget,
                                yearlyBudget
                        )
                );

        mockMvc.perform(
                        get("/api/v1/groups/{groupId}/budgets", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].budgetType")
                        .value("MONTHLY"))
                .andExpect(jsonPath("$[1].budgetType")
                        .value("YEARLY"));

        verify(budgetService)
                .getGroupBudgets(groupId);
    }

    @Test
    void shouldGetBudgetByIdSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        BudgetResponse response =
                createBudgetResponse(
                        budgetId,
                        groupId,
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        when(budgetService.getBudgetById(
                groupId,
                budgetId
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/groups/{groupId}/budgets/{budgetId}",
                                groupId,
                                budgetId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId")
                        .value(budgetId.toString()))
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.budgetAmount")
                        .value(30000));

        verify(budgetService)
                .getBudgetById(
                        groupId,
                        budgetId
                );
    }

    @Test
    void shouldUpdateBudgetSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        UpdateBudgetRequest request =
                new UpdateBudgetRequest(
                        BigDecimal.valueOf(35000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        BudgetResponse response =
                createBudgetResponse(
                        budgetId,
                        groupId,
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(35000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        when(budgetService.updateBudget(
                eq(groupId),
                eq(budgetId),
                any(UpdateBudgetRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/groups/{groupId}/budgets/{budgetId}",
                                groupId,
                                budgetId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetAmount")
                        .value(35000))
                .andExpect(jsonPath("$.budgetType")
                        .value("MONTHLY"));

        verify(budgetService)
                .updateBudget(
                        eq(groupId),
                        eq(budgetId),
                        any(UpdateBudgetRequest.class)
                );
    }

    @Test
    void shouldArchiveBudgetSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        doNothing()
                .when(budgetService)
                .archiveBudget(
                        groupId,
                        budgetId
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/groups/{groupId}/budgets/{budgetId}",
                                groupId,
                                budgetId
                        )
                )
                .andExpect(status().isNoContent());

        verify(budgetService)
                .archiveBudget(
                        groupId,
                        budgetId
                );
    }

    @Test
    void shouldReturnBadRequestWhenBudgetAmountIsZero()
            throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.ZERO,
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockMvc.perform(
                        post("/api/v1/groups/{groupId}/budgets", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                budgetService,
                org.mockito.Mockito.never()
        ).createBudget(
                any(UUID.class),
                any(CreateBudgetRequest.class)
        );
    }

    private BudgetResponse createBudgetResponse(
            UUID budgetId,
            UUID groupId,
            BudgetType budgetType,
            BigDecimal amount,
            LocalDate startDate,
            LocalDate endDate
    ) {

        return new BudgetResponse(
                budgetId,
                groupId,
                "Family",
                amount,
                BigDecimal.ZERO,
                amount,
                BigDecimal.ZERO,
                false,
                budgetType,
                startDate,
                endDate,
                UUID.randomUUID(),
                "Swathi",
                LocalDateTime.now()
        );
    }
}
package com.spendsense.dashboard.service;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.service.BudgetService;
import com.spendsense.dashboard.dto.CategorySummaryResponse;
import com.spendsense.dashboard.dto.DashboardSummaryResponse;
import com.spendsense.dashboard.dto.MonthlyExpenseResponse;
import com.spendsense.dashboard.projection.MonthlyExpenseProjection;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BudgetService budgetService;

    private DashboardService dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-08-02T00:00:00Z"),
                ZoneId.of("UTC")
        );

        dashboardService = new DashboardService(
                expenseRepository,
                currentUserService,
                budgetService,
                fixedClock
        );
    }

    @Test
    void shouldReturnDashboardSummary() {
        LocalDate today = LocalDate.of(2026, 8, 2);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(expenseRepository.getTodayExpense(user, today))
                .thenReturn(BigDecimal.valueOf(500));

        when(expenseRepository.getMonthExpense(
                user,
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth())
        )).thenReturn(BigDecimal.valueOf(8000));

        when(expenseRepository.getTotalExpense(user))
                .thenReturn(BigDecimal.valueOf(25000));

        when(expenseRepository.countByCreatedBy(user))
                .thenReturn(40L);

        BudgetResponse activeBudget =
                mock(BudgetResponse.class);

        when(budgetService.getActiveBudgetsForUser(
                user,
                today
        )).thenReturn(List.of(activeBudget));

        DashboardSummaryResponse response =
                dashboardService.getSummary();

        assertEquals(
                BigDecimal.valueOf(500),
                response.getTodayExpense()
        );

        assertEquals(
                BigDecimal.valueOf(8000),
                response.getMonthExpense()
        );

        assertEquals(
                BigDecimal.valueOf(25000),
                response.getTotalExpense()
        );

        assertEquals(
                40L,
                response.getExpenseCount()
        );

        assertEquals(
                1,
                response.getActiveBudgets().size()
        );

        assertEquals(
                activeBudget,
                response.getActiveBudgets().get(0)
        );


        verify(currentUserService).getCurrentUser();

        verify(expenseRepository)
                .getTodayExpense(user, today);

        verify(expenseRepository)
                .getMonthExpense(
                        user,
                        today.withDayOfMonth(1),
                        today.withDayOfMonth(today.lengthOfMonth())
                );

        verify(expenseRepository)
                .getTotalExpense(user);

        verify(expenseRepository)
                .countByCreatedBy(user);

        verify(budgetService)
                .getActiveBudgetsForUser(
                        user,
                        today
                );
    }

    @Test
    void shouldReturnCategorySummary() {
        List<CategorySummaryResponse> repositoryResponse =
                List.of(
                        new CategorySummaryResponse(
                                "Food",
                                BigDecimal.valueOf(5000)
                        ),
                        new CategorySummaryResponse(
                                "Travel",
                                BigDecimal.valueOf(2500)
                        )
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(expenseRepository.getCategorySummary(user))
                .thenReturn(repositoryResponse);

        List<CategorySummaryResponse> response =
                dashboardService.getCategorySummary();

        assertEquals(2, response.size());

        assertEquals(
                "Food",
                response.get(0).getCategoryName()
        );

        assertEquals(
                BigDecimal.valueOf(5000),
                response.get(0).getAmount()
        );

        assertEquals(
                "Travel",
                response.get(1).getCategoryName()
        );

        verify(currentUserService).getCurrentUser();
        verify(expenseRepository).getCategorySummary(user);
    }

    @Test
    void shouldReturnMonthlyExpenseSummary() {
        int currentYear = 2026;

        MonthlyExpenseProjection january =
                mock(MonthlyExpenseProjection.class);

        MonthlyExpenseProjection february =
                mock(MonthlyExpenseProjection.class);

        when(january.getMonth()).thenReturn("Jan ");
        when(january.getAmount())
                .thenReturn(BigDecimal.valueOf(4000));

        when(february.getMonth()).thenReturn("Feb ");
        when(february.getAmount())
                .thenReturn(BigDecimal.valueOf(6000));

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(expenseRepository.getMonthlyExpenseSummary(
                user.getUserId(),
                currentYear
        )).thenReturn(List.of(january, february));

        List<MonthlyExpenseResponse> response =
                dashboardService.getMonthlyExpenseSummary();

        assertEquals(2, response.size());

        assertEquals("Jan", response.get(0).getMonth());
        assertEquals(
                BigDecimal.valueOf(4000),
                response.get(0).getAmount()
        );

        assertEquals("Feb", response.get(1).getMonth());
        assertEquals(
                BigDecimal.valueOf(6000),
                response.get(1).getAmount()
        );

        verify(currentUserService).getCurrentUser();

        verify(expenseRepository).getMonthlyExpenseSummary(
                user.getUserId(),
                currentYear
        );
    }
}

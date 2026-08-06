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
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final CurrentUserService currentUserService;
    private final Clock clock;
    private final BudgetService budgetService;

    public DashboardService(
            ExpenseRepository expenseRepository,
            CurrentUserService currentUserService,
            BudgetService budgetService,
            Clock clock
    ) {
        this.expenseRepository = expenseRepository;
        this.currentUserService = currentUserService;
        this.budgetService = budgetService;
        this.clock = clock;
    }

    public DashboardSummaryResponse getSummary() {

        User currentUser = currentUserService.getCurrentUser();

        LocalDate today = LocalDate.now(clock);

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );

        BigDecimal todayExpense =
                expenseRepository.getTodayExpense(
                        currentUser,
                        today
                );

        BigDecimal monthExpense =
                expenseRepository.getMonthExpense(
                        currentUser,
                        startDate,
                        endDate
                );

        BigDecimal totalExpense =
                expenseRepository.getTotalExpense(
                        currentUser
                );

        long expenseCount =
                expenseRepository.countByCreatedBy(
                        currentUser
                );

        List<BudgetResponse> activeBudgets =
                budgetService.getActiveBudgetsForUser(
                        currentUser,
                        today
                );

        return new DashboardSummaryResponse(
                todayExpense,
                monthExpense,
                totalExpense,
                expenseCount,
                activeBudgets
        );
    }

    public List<CategorySummaryResponse> getCategorySummary(){

        User currentUser = currentUserService.getCurrentUser();

        return expenseRepository.getCategorySummary(currentUser);
    }


    public List<MonthlyExpenseResponse> getMonthlyExpenseSummary() {

        User currentUser = currentUserService.getCurrentUser();

        int year = LocalDate.now(clock).getYear();

        return expenseRepository
                .getMonthlyExpenseSummary(
                        currentUser.getUserId(),
                        year
                )
                .stream()
                .map(this::mapToMonthlyResponse)
                .toList();
    }

    private MonthlyExpenseResponse mapToMonthlyResponse(
            MonthlyExpenseProjection projection
    ) {

        return new MonthlyExpenseResponse(
                projection.getMonth().trim(),
                projection.getAmount()
        );
    }
}

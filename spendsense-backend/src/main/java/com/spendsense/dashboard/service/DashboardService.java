package com.spendsense.dashboard.service;

import com.spendsense.dashboard.dto.CategorySummaryResponse;
import com.spendsense.dashboard.dto.DashboardSummaryResponse;
import com.spendsense.dashboard.dto.MonthlyExpenseResponse;
import com.spendsense.dashboard.projection.MonthlyExpenseProjection;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public DashboardService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryResponse getSummary() {

        User currentUser = getCurrentUser();

        LocalDate today = LocalDate.now();

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

        return new DashboardSummaryResponse(
                todayExpense,
                monthExpense,
                totalExpense,
                expenseCount
        );
    }

    public List<CategorySummaryResponse> getCategorySummary(){

        User currentUser = getCurrentUser();

        return expenseRepository.getCategorySummary(currentUser);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    public List<MonthlyExpenseResponse> getMonthlyExpenseSummary() {

        User currentUser = getCurrentUser();

        int year = LocalDate.now().getYear();

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

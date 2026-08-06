package com.spendsense.dashboard.dto;

import com.spendsense.budget.dto.BudgetResponse;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal todayExpense;
    private BigDecimal monthExpense;
    private BigDecimal totalExpense;
    private long expenseCount;
    private List<BudgetResponse> activeBudgets;

    public DashboardSummaryResponse() {
        this.activeBudgets = List.of();
    }

    public DashboardSummaryResponse(
            BigDecimal todayExpense,
            BigDecimal monthExpense,
            BigDecimal totalExpense,
            long expenseCount
    ) {
        this(
                todayExpense,
                monthExpense,
                totalExpense,
                expenseCount,
                List.of()
        );
    }

    public DashboardSummaryResponse(
            BigDecimal todayExpense,
            BigDecimal monthExpense,
            BigDecimal totalExpense,
            long expenseCount,
            List<BudgetResponse> activeBudgets
    ) {
        this.todayExpense = todayExpense;
        this.monthExpense = monthExpense;
        this.totalExpense = totalExpense;
        this.expenseCount = expenseCount;
        this.activeBudgets = activeBudgets;
    }

    public BigDecimal getTodayExpense() {
        return todayExpense;
    }

    public void setTodayExpense(BigDecimal todayExpense) {
        this.todayExpense = todayExpense;
    }

    public BigDecimal getMonthExpense() {
        return monthExpense;
    }

    public void setMonthExpense(BigDecimal monthExpense) {
        this.monthExpense = monthExpense;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }

    public List<BudgetResponse> getActiveBudgets() {
        return activeBudgets;
    }

    public void setActiveBudgets(
            List<BudgetResponse> activeBudgets
    ) {
        this.activeBudgets = activeBudgets;
    }
}

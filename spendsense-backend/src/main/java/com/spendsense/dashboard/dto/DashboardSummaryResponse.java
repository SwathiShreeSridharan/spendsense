package com.spendsense.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {
    private BigDecimal todayExpense;
    private BigDecimal monthExpense;
    private BigDecimal totalExpense;
    private long expenseCount;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(
            BigDecimal todayExpense,
            BigDecimal monthExpense,
            BigDecimal totalExpense,
            long expenseCount
    ) {
        this.todayExpense = todayExpense;
        this.monthExpense = monthExpense;
        this.totalExpense = totalExpense;
        this.expenseCount = expenseCount;
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
}

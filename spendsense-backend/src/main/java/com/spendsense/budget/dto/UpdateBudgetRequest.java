package com.spendsense.budget.dto;

import com.spendsense.budget.entity.BudgetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateBudgetRequest {
    @NotNull(message = "Budget amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Budget amount must be greater than zero"
    )
    private BigDecimal amount;

    @NotNull(message = "Budget type is required")
    private BudgetType budgetType;

    @NotNull(message = "Budget start date is required")
    private LocalDate startDate;

    @NotNull(message = "Budget end date is required")
    private LocalDate endDate;

    public UpdateBudgetRequest() {
    }

    public UpdateBudgetRequest(
            BigDecimal amount,
            BudgetType budgetType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.amount = amount;
        this.budgetType = budgetType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BudgetType getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(BudgetType budgetType) {
        this.budgetType = budgetType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

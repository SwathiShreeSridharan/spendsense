package com.spendsense.budget.dto;

import com.spendsense.budget.entity.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetResponse {

    private UUID budgetId;

    private UUID groupId;

    private String groupName;

    private BigDecimal budgetAmount;

    private BigDecimal spentAmount;

    private BigDecimal remainingAmount;

    private BigDecimal percentageUsed;

    private boolean exceeded;

    private BudgetType budgetType;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID createdById;

    private String createdByName;

    private LocalDateTime createdAt;

    public BudgetResponse() {
    }

    public BudgetResponse(
            UUID budgetId,
            UUID groupId,
            String groupName,
            BigDecimal budgetAmount,
            BigDecimal spentAmount,
            BigDecimal remainingAmount,
            BigDecimal percentageUsed,
            boolean exceeded,
            BudgetType budgetType,
            LocalDate startDate,
            LocalDate endDate,
            UUID createdById,
            String createdByName,
            LocalDateTime createdAt
    ) {
        this.budgetId = budgetId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.percentageUsed = percentageUsed;
        this.exceeded = exceeded;
        this.budgetType = budgetType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }

    public UUID getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(UUID budgetId) {
        this.budgetId = budgetId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isExceeded() {
        return exceeded;
    }

    public void setExceeded(boolean exceeded) {
        this.exceeded = exceeded;
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

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.spendsense.expense.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseResponse {
    private UUID expenseId;

    private String title;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private UUID categoryId;

    private String categoryName;

    private String categoryIcon;

    private String categoryColor;

    private boolean isDefaultCategory;

    public ExpenseResponse() {
    }

    public ExpenseResponse(
            UUID expenseId,
            String title,
            String description,
            BigDecimal amount,
            LocalDate expenseDate,
            UUID categoryId,
            String categoryName,
            String categoryIcon,
            String categoryColor,
            boolean isDefaultCategory
    ) {
        this.expenseId = expenseId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.isDefaultCategory = isDefaultCategory;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(UUID expenseId) {
        this.expenseId = expenseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public boolean isDefaultCategory() {
        return isDefaultCategory;
    }

    public void setDefaultCategory(boolean defaultCategory) {
        isDefaultCategory = defaultCategory;
    }
}

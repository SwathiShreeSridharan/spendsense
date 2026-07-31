package com.spendsense.dashboard.dto;

import java.math.BigDecimal;

public class CategorySummaryResponse {

    private String categoryName;
    private BigDecimal amount;

    public CategorySummaryResponse() {
    }

    public CategorySummaryResponse(
            String categoryName,
            BigDecimal amount
    ) {
        this.categoryName = categoryName;
        this.amount = amount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

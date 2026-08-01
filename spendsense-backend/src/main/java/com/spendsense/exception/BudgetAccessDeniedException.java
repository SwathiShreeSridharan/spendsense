package com.spendsense.exception;

public class BudgetAccessDeniedException extends RuntimeException {
    public BudgetAccessDeniedException(String message) {
        super(message);
    }
}

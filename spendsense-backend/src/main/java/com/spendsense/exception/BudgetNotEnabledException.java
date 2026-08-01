package com.spendsense.exception;

public class BudgetNotEnabledException extends RuntimeException {
    public BudgetNotEnabledException(String message) {
        super(message);
    }
}

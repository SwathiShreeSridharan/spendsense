package com.spendsense.expense.controller;

import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
import com.spendsense.expense.dto.UpdateExpenseRequest;
import com.spendsense.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@PathVariable UUID  groupId, @Valid @RequestBody CreateExpenseRequest request){
        ExpenseResponse response = expenseService.createExpense(groupId,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable UUID groupId){
        List<ExpenseResponse> response = expenseService.getExpenses(groupId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {
        ExpenseResponse response =
                expenseService.updateExpense(
                        groupId,
                        expenseId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> archiveExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId
    ) {
        expenseService.archiveExpense(
                groupId,
                expenseId
        );

        return ResponseEntity.noContent().build();
    }
}

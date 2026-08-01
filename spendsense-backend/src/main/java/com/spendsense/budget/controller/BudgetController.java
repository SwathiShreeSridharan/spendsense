package com.spendsense.budget.controller;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateBudgetRequest request
    ) {

        BudgetResponse response =
                budgetService.createBudget(
                        groupId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getGroupBudgets(
            @PathVariable UUID groupId
    ) {

        List<BudgetResponse> response =
                budgetService.getGroupBudgets(groupId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable UUID groupId,
            @PathVariable UUID budgetId
    ) {

        BudgetResponse response =
                budgetService.getBudgetById(
                        groupId,
                        budgetId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable UUID groupId,
            @PathVariable UUID budgetId,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {

        BudgetResponse response =
                budgetService.updateBudget(
                        groupId,
                        budgetId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> archiveBudget(
            @PathVariable UUID groupId,
            @PathVariable UUID budgetId
    ) {

        budgetService.archiveBudget(
                groupId,
                budgetId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}

package com.spendsense.dashboard.controller;

import com.spendsense.dashboard.dto.CategorySummaryResponse;
import com.spendsense.dashboard.dto.DashboardSummaryResponse;
import com.spendsense.dashboard.dto.MonthlyExpenseResponse;
import com.spendsense.dashboard.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {

        DashboardSummaryResponse response =
                dashboardService.getSummary();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary() {

        return ResponseEntity.ok(
                dashboardService.getCategorySummary()
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyExpenseResponse>>
    getMonthlyExpenseSummary() {

        return ResponseEntity.ok(
                dashboardService.getMonthlyExpenseSummary()
        );
    }
}

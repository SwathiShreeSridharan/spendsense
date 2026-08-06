package com.spendsense.budget.service;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.entity.Budget;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.budget.repository.BudgetRepository;
import com.spendsense.exception.*;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.group.entity.Group;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;
    private final GroupAccessService groupAccessService;
    private final ExpenseRepository expenseRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CurrentUserService currentUserService,
            GroupAccessService groupAccessService,
            ExpenseRepository expenseRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
        this.groupAccessService = groupAccessService;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public BudgetResponse createBudget(
            UUID groupId,
            CreateBudgetRequest request
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireOwnerOrAdmin(
                        groupId,
                        currentUser
                );

        if (group.getSettings() == null
                || !group.getSettings().isBudgetEnabled()) {

            throw new BudgetNotEnabledException(
                    "Budget is not enabled for this group"
            );
        }

        validateBudgetPeriod(
                request.getBudgetType(),
                request.getStartDate(),
                request.getEndDate()
        );

        boolean overlappingBudgetExists =
                budgetRepository.existsOverlappingBudget(
                        group,
                        request.getStartDate(),
                        request.getEndDate()
                );

        if (overlappingBudgetExists) {
            throw new BudgetAlreadyExistsException(
                    "A budget already exists for the selected period"
            );
        }

        Budget budget = new Budget(
                group,
                request.getAmount(),
                request.getBudgetType(),
                request.getStartDate(),
                request.getEndDate(),
                currentUser
        );

        Budget savedBudget =
                budgetRepository.save(budget);

        return mapToResponse(savedBudget);
    }

    public List<BudgetResponse> getGroupBudgets(UUID groupId) {
        User currentUser = currentUserService.getCurrentUser();

        Group group = groupAccessService.requireMember(
                groupId,
                currentUser
        );

        return budgetRepository
                .findByGroupAndArchivedFalseOrderByStartDateDesc(group)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(
            UUID groupId,
            UUID budgetId
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Group group = groupAccessService.requireMember(
                groupId,
                currentUser
        );

        Budget budget = budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budgetId,
                        group
                )
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        return mapToResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(
            UUID groupId,
            UUID budgetId,
            UpdateBudgetRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Group group = groupAccessService.requireOwnerOrAdmin(
                groupId,
                currentUser
        );

        Budget budget = budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budgetId,
                        group
                )
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        validateBudgetPeriod(
                request.getBudgetType(),
                request.getStartDate(),
                request.getEndDate()
        );

        boolean overlapExists =
                budgetRepository
                        .existsOverlappingBudgetExcludingCurrent(
                                group,
                                budgetId,
                                request.getStartDate(),
                                request.getEndDate()
                        );

        if (overlapExists) {
            throw new BudgetAlreadyExistsException(
                    "A budget already exists for the selected period"
            );
        }

        budget.setAmount(request.getAmount());
        budget.setBudgetType(request.getBudgetType());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());

        Budget updatedBudget = budgetRepository.save(budget);

        return mapToResponse(updatedBudget);
    }

    @Transactional
    public void archiveBudget(
            UUID groupId,
            UUID budgetId
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Group group = groupAccessService.requireOwnerOrAdmin(
                groupId,
                currentUser
        );

        Budget budget = budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budgetId,
                        group
                )
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        budget.setArchived(true);

        budgetRepository.save(budget);
    }


    private void validateBudgetPeriod(
            BudgetType budgetType,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate.isAfter(endDate)) {
            throw new InvalidBudgetPeriodException(
                    "Start date cannot be after end date"
            );
        }

        switch (budgetType) {

            case MONTHLY ->
                    validateMonthlyBudget(
                            startDate,
                            endDate
                    );

            case QUARTERLY ->
                    validateQuarterlyBudget(
                            startDate,
                            endDate
                    );

            case YEARLY ->
                    validateYearlyBudget(
                            startDate,
                            endDate
                    );

            case CUSTOM -> {
                // Any valid date range is allowed.
            }
        }
    }

    private void validateYearlyBudget(LocalDate startDate, LocalDate endDate) {

        LocalDate expectedStartDate =
                LocalDate.of(
                        startDate.getYear(),
                        1,
                        1
                );

        LocalDate expectedEndDate =
                LocalDate.of(
                        startDate.getYear(),
                        12,
                        31
                );

        if (!startDate.equals(expectedStartDate)
                || !endDate.equals(expectedEndDate)) {

            throw new InvalidBudgetPeriodException(
                    "Yearly budget must cover one complete calendar year"
            );
        }
    }

    private void validateQuarterlyBudget(LocalDate startDate, LocalDate endDate) {

        int startMonth = startDate.getMonthValue();

        boolean validQuarterStart =
                startMonth == 1
                        || startMonth == 4
                        || startMonth == 7
                        || startMonth == 10;

        if (!validQuarterStart
                || startDate.getDayOfMonth() != 1) {

            throw new InvalidBudgetPeriodException(
                    "Quarterly budget must start on the first day of a quarter"
            );
        }

        LocalDate expectedEndDate =
                startDate
                        .plusMonths(3)
                        .minusDays(1);

        if (!endDate.equals(expectedEndDate)) {
            throw new InvalidBudgetPeriodException(
                    "Quarterly budget must cover one complete quarter"
            );
        }
    }

    private void validateMonthlyBudget(LocalDate startDate, LocalDate endDate) {
        LocalDate expectedStartDate =
                startDate.withDayOfMonth(1);

        LocalDate expectedEndDate =
                startDate.withDayOfMonth(
                        startDate.lengthOfMonth()
                );

        if (!startDate.equals(expectedStartDate)
                || !endDate.equals(expectedEndDate)) {

            throw new InvalidBudgetPeriodException(
                    "Monthly budget must cover one complete month"
            );
        }
    }


    private BudgetResponse mapToResponse(Budget budget) {

        BigDecimal spentAmount =
                expenseRepository.getTotalExpenseForBudgetPeriod(
                        budget.getGroup(),
                        budget.getStartDate(),
                        budget.getEndDate()
                );

        if (spentAmount == null) {
            spentAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount =
                budget.getAmount().subtract(spentAmount);

        BigDecimal percentageUsed =
                spentAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                budget.getAmount(),
                                2,
                                RoundingMode.HALF_UP
                        );

        boolean exceeded =
                spentAmount.compareTo(budget.getAmount()) > 0;

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getGroup().getGroupId(),
                budget.getGroup().getName(),
                budget.getAmount(),
                spentAmount,
                remainingAmount,
                percentageUsed,
                exceeded,
                budget.getBudgetType(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getCreatedBy().getUserId(),
                budget.getCreatedBy().getName(),
                budget.getCreatedAt()
        );
    }

    public List<BudgetResponse> getActiveBudgetsForUser(
            User user,
            LocalDate date
    ) {
        return budgetRepository
                .findActiveBudgetsForUser(user, date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}

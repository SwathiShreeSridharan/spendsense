package com.spendsense.budget.service;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.entity.Budget;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.budget.repository.BudgetRepository;
import com.spendsense.exception.*;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse createBudget(
            UUID groupId,
            CreateBudgetRequest request
    ) {
        User currentUser = getCurrentUser();

        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException("Group not found")
                );

        GroupMember membership = groupMemberRepository
                .findByGroupAndUser(group, currentUser)
                .orElseThrow(
                        () -> new GroupNotFoundException("Group not found")
                );

        if (membership.getRole() != GroupRole.OWNER) {
            throw new BudgetAccessDeniedException(
                    "Only the group owner can create a budget"
            );
        }

        if (group.getSettings() == null
                || !group.getSettings().isBudgetEnabled()) {

            throw new BudgetNotEnabledException(
                    "Budget is not enabled for this group"
            );
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidBudgetPeriodException(
                    "Start date cannot be after end date"
            );
        }

        validateBudgetPeriod(request.getBudgetType(),
                request.getStartDate(),
                request.getEndDate());

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

        User currentUser = getCurrentUser();

        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        groupMemberRepository
                .findByGroupAndUser(group, currentUser)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        List<Budget> budgets =
                budgetRepository
                        .findByGroupAndArchivedFalseOrderByStartDateDesc(
                                group
                        );

        return budgets
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(
            UUID groupId,
            UUID budgetId
    ) {

        User currentUser = getCurrentUser();

        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                )
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        Budget budget = budgetRepository
                .findByBudgetIdAndArchivedFalse(budgetId)
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        if (!budget.getGroup()
                .getGroupId()
                .equals(groupId)) {

            throw new BudgetNotFoundException(
                    "Budget not found"
            );
        }

        return mapToResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(
            UUID groupId,
            UUID budgetId,
            UpdateBudgetRequest request
    ) {

        User currentUser = getCurrentUser();

        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        GroupMember membership = groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                )
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        if (membership.getRole() != GroupRole.OWNER) {
            throw new BudgetAccessDeniedException(
                    "Only the group owner can update a budget"
            );
        }

        Budget budget = budgetRepository
                .findByBudgetIdAndArchivedFalse(budgetId)
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        if (!budget.getGroup()
                .getGroupId()
                .equals(groupId)) {

            throw new BudgetNotFoundException(
                    "Budget not found"
            );
        }

        validateBudgetPeriod(request.getBudgetType(),request.getStartDate(),request.getEndDate());

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

        Budget updatedBudget =
                budgetRepository.save(budget);

        return mapToResponse(updatedBudget);
    }

    @Transactional
    public void archiveBudget(
            UUID groupId,
            UUID budgetId
    ) {

        User currentUser = getCurrentUser();

        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        GroupMember membership = groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                )
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        if (membership.getRole() != GroupRole.OWNER) {
            throw new BudgetAccessDeniedException(
                    "Only the group owner can archive a budget"
            );
        }

        Budget budget = budgetRepository
                .findByBudgetIdAndArchivedFalse(budgetId)
                .orElseThrow(
                        () -> new BudgetNotFoundException(
                                "Budget not found"
                        )
                );

        if (!budget.getGroup()
                .getGroupId()
                .equals(groupId)) {

            throw new BudgetNotFoundException(
                    "Budget not found"
            );
        }

        budget.setArchived(true);

        budgetRepository.save(budget);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
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

        BigDecimal spentAmount = BigDecimal.ZERO;

        BigDecimal remainingAmount =
                budget.getAmount().subtract(spentAmount);

        BigDecimal percentageUsed = BigDecimal.ZERO;

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
}

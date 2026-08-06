package com.spendsense.expense.service;

import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.CategoryNotFoundException;
import com.spendsense.exception.ExpenseNotFoundException;
import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
import com.spendsense.expense.dto.UpdateExpenseRequest;
import com.spendsense.expense.entity.Expense;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.group.entity.Group;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final GroupAccessService groupAccessService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService,
            GroupAccessService groupAccessService
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.groupAccessService = groupAccessService;
    }

    @Transactional
    public ExpenseResponse createExpense(
            UUID groupId,
            CreateExpenseRequest request
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        Category category = getCategory(
                request.getCategoryId(),
                group
        );

        Expense expense = new Expense(
                request.getTitle(),
                request.getDescription(),
                request.getAmount(),
                request.getExpenseDate(),
                group,
                category,
                currentUser,
                currentUser
        );

        Expense savedExpense =
                expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(
            UUID groupId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        return expenseRepository
                .findByGroupAndArchivedFalseOrderByExpenseDateDescCreatedAtDesc(
                        group
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse updateExpense(
            UUID groupId,
            UUID expenseId,
            UpdateExpenseRequest request
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        Expense expense = expenseRepository
                .findByExpenseIdAndGroupAndArchivedFalse(
                        expenseId,
                        group
                )
                .orElseThrow(
                        () -> new ExpenseNotFoundException(
                                "Expense not found"
                        )
                );

        requireExpenseModificationAccess(
                groupId,
                currentUser,
                expense
        );

        Category category = getCategory(
                request.getCategoryId(),
                group
        );

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);

        Expense updatedExpense =
                expenseRepository.save(expense);

        return mapToResponse(updatedExpense);
    }

    @Transactional
    public void archiveExpense(
            UUID groupId,
            UUID expenseId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        Expense expense = expenseRepository
                .findByExpenseIdAndGroupAndArchivedFalse(
                        expenseId,
                        group
                )
                .orElseThrow(
                        () -> new ExpenseNotFoundException(
                                "Expense not found"
                        )
                );

        requireExpenseModificationAccess(
                groupId,
                currentUser,
                expense
        );

        expense.setArchived(true);

        expenseRepository.save(expense);
    }

    private void requireExpenseModificationAccess(
            UUID groupId,
            User currentUser,
            Expense expense
    ) {
        boolean isCreator = Objects.equals(
                expense.getCreatedBy().getUserId(),
                currentUser.getUserId()
        );

        if (isCreator) {
            return;
        }

        groupAccessService.requireOwnerOrAdmin(
                groupId,
                currentUser
        );
    }

    private Category getCategory(
            UUID categoryId,
            Group group
    ) {
        return categoryRepository
                .findByCategoryIdAndGroup(
                        categoryId,
                        group
                )
                .orElseThrow(
                        () -> new CategoryNotFoundException(
                                "Category not found"
                        )
                );
    }

    private ExpenseResponse mapToResponse(
            Expense expense
    ) {
        Category category = expense.getCategory();

        return new ExpenseResponse(
                expense.getExpenseId(),
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                category.getCategoryId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                category.isDefault()
        );
    }
}
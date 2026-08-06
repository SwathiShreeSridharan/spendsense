package com.spendsense.expense.service;

import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.CategoryNotFoundException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
import com.spendsense.expense.entity.Expense;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.group.entity.Group;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private GroupAccessService groupAccessService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService, GroupAccessService groupAccessService
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.groupAccessService = groupAccessService;
    }

    public ExpenseResponse createExpense(UUID groupId, CreateExpenseRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        Category category = getCategory(request.getCategoryId(), group);

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

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    public List<ExpenseResponse> getExpenses(UUID groupId){
        User currentUser = currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        List<Expense> expenses =
                expenseRepository.findByGroupOrderByExpenseDateDescCreatedAtDesc(group);

        return expenses.stream().map(this::mapToResponse).toList();
    }


    private ExpenseResponse mapToResponse(Expense expense) {

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

    public Category getCategory(UUID categoryId, Group group){

        return categoryRepository
                .findByCategoryIdAndGroup(categoryId, group)
                .orElseThrow(()->
                         new CategoryNotFoundException("Category not found"));
    }
}

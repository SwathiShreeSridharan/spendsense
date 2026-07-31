package com.spendsense.expense.service;

import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.CategoryNotFoundException;
import com.spendsense.expense.dto.CreateExpenseRequest;
import com.spendsense.expense.dto.ExpenseResponse;
import com.spendsense.expense.entity.Expense;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public ExpenseResponse createExpense(CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        Category category = getCategory(request.getCategoryId());

        Expense expense = new Expense(
                request.getTitle(),
                request.getDescription(),
                request.getAmount(),
                request.getExpenseDate(),
                category,
                currentUser,
                currentUser
        );

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    public List<ExpenseResponse> getExpenses(){
        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByCreatedBy(currentUser);

        return expenses.stream().map(this::mapToResponse).toList();
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

    public Category getCategory(UUID categoryId){

        return categoryRepository
                .findById(categoryId)
                .orElseThrow(()->
                         new CategoryNotFoundException("Category not found"));
    }
}

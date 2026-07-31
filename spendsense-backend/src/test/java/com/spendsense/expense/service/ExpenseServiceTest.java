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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setup() {

        user = new User();
        user.setEmail("test@gmail.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@gmail.com",
                        null
                )
        );
    }

    @Test
    void shouldCreateExpenseSuccessfully() {

        UUID categoryId = UUID.randomUUID();

        Category category = new Category(
                "Food",
                "restaurant",
                "#4CAF50",
                true,
                user
        );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId
                );

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(expenseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response =
                expenseService.createExpense(request);

        assertEquals("Lunch", response.getTitle());
        assertEquals("Food", response.getCategoryName());

        ArgumentCaptor<Expense> captor =
                ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository)
                .save(captor.capture());

        Expense savedExpense = captor.getValue();

        assertEquals("Lunch", savedExpense.getTitle());
        assertEquals(category, savedExpense.getCategory());
    }

    @Test
    void shouldThrowCategoryNotFoundException() {

        UUID categoryId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId
                );

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> expenseService.createExpense(request)
        );

        verify(expenseRepository, never())
                .save(any());
    }

    @Test
    void shouldGetExpensesSuccessfully() {

        Category category = new Category(
                "Food",
                "restaurant",
                "#4CAF50",
                true,
                user
        );

        Expense expense = new Expense(
                "Lunch",
                "Office Lunch",
                BigDecimal.valueOf(250),
                LocalDate.now(),
                category,
                user,
                user
        );

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(expenseRepository.findByCreatedBy(user))
                .thenReturn(List.of(expense));

        List<ExpenseResponse> responses =
                expenseService.getExpenses();

        assertEquals(1, responses.size());
        assertEquals("Lunch", responses.getFirst().getTitle());

        verify(expenseRepository)
                .findByCreatedBy(user);
    }
}

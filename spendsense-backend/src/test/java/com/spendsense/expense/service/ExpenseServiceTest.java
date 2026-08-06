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
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import net.bytebuddy.asm.MemberSubstitution;
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
    private CurrentUserService currentUserService;

    @Mock
    private GroupAccessService groupAccessService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Group group;
    private UUID groupId;

    @BeforeEach
    void setup() {
        groupId = UUID.randomUUID();

        group = new Group();
        group.setGroupId(groupId);
        group.setName("Family");


        user = new User();
        user.setEmail("test@gmail.com");

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(groupAccessService.requireMember(groupId, user))
                .thenReturn(group);
    }

    @Test
    void shouldCreateExpenseSuccessfully() {

        UUID categoryId = UUID.randomUUID();

        Category category = new Category(
                "Food",
                "restaurant",
                "#4CAF50",
                true,
                group,
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


        when(categoryRepository.findByCategoryIdAndGroup(categoryId,group))
                .thenReturn(Optional.of(category));

        when(expenseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response =
                expenseService.createExpense(groupId,request);

        assertEquals("Lunch", response.getTitle());
        assertEquals("Food", response.getCategoryName());

        ArgumentCaptor<Expense> captor =
                ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository)
                .save(captor.capture());

        Expense savedExpense = captor.getValue();

        assertEquals("Lunch", savedExpense.getTitle());
        assertEquals(group, savedExpense.getGroup());
        assertEquals(category, savedExpense.getCategory());
        assertEquals(user, savedExpense.getCreatedBy());
        assertEquals(user, savedExpense.getPaidBy());
    }

    @Test
    void shouldRejectCategoryThatDoesNotBelongToGroup() {

        UUID categoryId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId
                );

        when(categoryRepository.findByCategoryIdAndGroup(categoryId,group))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> expenseService.createExpense(groupId,request)
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
                group,
                user
        );

        Expense expense = new Expense(
                "Lunch",
                "Office Lunch",
                BigDecimal.valueOf(250),
                LocalDate.now(),
                group,
                category,
                user,
                user
        );

        when(expenseRepository.findByGroupOrderByExpenseDateDescCreatedAtDesc(group))
                .thenReturn(List.of(expense));

        List<ExpenseResponse> responses =
                expenseService.getExpenses(groupId);

        assertEquals(1, responses.size());
        assertEquals("Lunch", responses.getFirst().getTitle());

        verify(expenseRepository)
                .findByGroupOrderByExpenseDateDescCreatedAtDesc(group);
    }

    @Test
    void shouldRejectExpenseCreationWhenUserIsNotGroupMember() {

        UUID categoryId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        "Office Lunch",
                        BigDecimal.valueOf(250),
                        LocalDate.now(),
                        categoryId
                );

        reset(groupAccessService);

        when(groupAccessService.requireMember(groupId, user))
                .thenThrow(
                        new GroupNotFoundException(
                                "Group not found"
                        )
                );

        assertThrows(
                GroupNotFoundException.class,
                () -> expenseService.createExpense(
                        groupId,
                        request
                )
        );

        verify(categoryRepository, never())
                .findByCategoryIdAndGroup(
                        any(UUID.class),
                        any(Group.class)
                );

        verify(expenseRepository, never())
                .save(any());
    }
}

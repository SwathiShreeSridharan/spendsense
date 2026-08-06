package com.spendsense.expense.repository;

import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.expense.entity.Expense;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSumOnlyExpensesForGroupAndBudgetPeriod() {
        User user = userRepository.save(
                new User(
                        "Expense Test User",
                        "expense-" + UUID.randomUUID() + "@example.com",
                        "9876543210",
                        "hashed-password"
                )
        );

        Group budgetGroup = createGroup("Budget Group", user);
        Group otherGroup = createGroup("Other Group", user);

        Category budgetCategory =
                categoryRepository.save(
                        new Category(
                                "Food",
                                "food",
                                "#FF0000",
                                false,
                                budgetGroup,
                                user
                        )
                );

        Category otherCategory =
                categoryRepository.save(
                        new Category(
                                "Travel",
                                "travel",
                                "#0000FF",
                                false,
                                otherGroup,
                                user
                        )
                );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        saveExpense(
                "Start-date expense",
                new BigDecimal("100.00"),
                startDate,
                budgetGroup,
                budgetCategory,
                user
        );

        saveExpense(
                "End-date expense",
                new BigDecimal("50.25"),
                endDate,
                budgetGroup,
                budgetCategory,
                user
        );

        saveExpense(
                "Outside-period expense",
                new BigDecimal("999.00"),
                endDate.plusDays(1),
                budgetGroup,
                budgetCategory,
                user
        );

        saveExpense(
                "Other-group expense",
                new BigDecimal("500.00"),
                startDate.plusDays(1),
                otherGroup,
                otherCategory,
                user
        );

        BigDecimal total =
                expenseRepository.getTotalExpenseForBudgetPeriod(
                        budgetGroup,
                        startDate,
                        endDate
                );

        assertEquals(
                0,
                new BigDecimal("150.25").compareTo(total)
        );
    }

    private Group createGroup(String name, User user) {
        Group group = new Group();
        group.setName(name);
        group.setDescription("Repository test group");
        group.setGroupType(GroupType.FAMILY);
        group.setColor("#2196F3");
        group.setIcon("home");
        group.setArchived(false);
        group.setCreatedBy(user);

        return groupRepository.save(group);
    }

    private void saveExpense(
            String title,
            BigDecimal amount,
            LocalDate date,
            Group group,
            Category category,
            User user
    ) {
        expenseRepository.save(
                new Expense(
                        title,
                        "Repository integration test",
                        amount,
                        date,
                        group,
                        category,
                        user,
                        user
                )
        );
    }
}
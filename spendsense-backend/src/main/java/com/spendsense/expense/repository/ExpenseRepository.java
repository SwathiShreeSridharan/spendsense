package com.spendsense.expense.repository;

import com.spendsense.dashboard.dto.CategorySummaryResponse;
import com.spendsense.dashboard.projection.MonthlyExpenseProjection;
import com.spendsense.expense.entity.Expense;
import com.spendsense.group.entity.Group;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByGroupOrderByExpenseDateDescCreatedAtDesc(
            Group group
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.createdBy = :user
            AND e.archived = false
            """)
    BigDecimal getTotalExpense(
            @Param("user") User user
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.createdBy = :user
            AND e.expenseDate = :today
            AND e.archived = false
            """)
    BigDecimal getTodayExpense(
            @Param("user") User user,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.createdBy = :user
            AND e.archived = false
            AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getMonthExpense(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT new com.spendsense.dashboard.dto.CategorySummaryResponse(
                e.category.name,
                COALESCE(SUM(e.amount),0)
        )
        FROM Expense e
        WHERE e.createdBy = :user
        AND e.archived = false
        GROUP BY e.category.name
        ORDER BY SUM(e.amount) DESC
        """)
    List<CategorySummaryResponse> getCategorySummary(
            @Param("user") User user
    );

    @Query(value = """
        SELECT
            TO_CHAR(e.expense_date, 'Mon') AS month,
            COALESCE(SUM(e.amount),0) AS amount
        FROM expenses e
        WHERE e.created_by = :userId
        AND e.archived = false
        AND EXTRACT(YEAR FROM e.expense_date) = :year
        GROUP BY
            EXTRACT(MONTH FROM e.expense_date),
            TO_CHAR(e.expense_date,'Mon')
        ORDER BY
            EXTRACT(MONTH FROM e.expense_date)
        """,
            nativeQuery = true)
    List<MonthlyExpenseProjection> getMonthlyExpenseSummary(
            @Param("userId") UUID userId,
            @Param("year") int year
    );

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.group = :group
          AND e.expenseDate BETWEEN :startDate AND :endDate
          AND e.archived = false
        """)
    BigDecimal getTotalExpenseForBudgetPeriod(
            @Param("group") Group group,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Expense> findByExpenseIdAndGroup(
            UUID expenseId,
            Group group
    );

    List<Expense>
    findByGroupAndArchivedFalseOrderByExpenseDateDescCreatedAtDesc(
            Group group
    );

    long countByCreatedByAndArchivedFalse(
            User createdBy
    );

    Optional<Expense>
    findByExpenseIdAndGroupAndArchivedFalse(
            UUID expenseId,
            Group group
    );
}

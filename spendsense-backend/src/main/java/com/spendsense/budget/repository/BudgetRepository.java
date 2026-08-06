package com.spendsense.budget.repository;

import com.spendsense.budget.entity.Budget;
import com.spendsense.group.entity.Group;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {


    Optional<Budget> findByBudgetIdAndGroupAndArchivedFalse(
            UUID budgetId,
            Group group
    );

    List<Budget> findByGroupAndArchivedFalseOrderByStartDateDesc(
            Group group
    );

    Optional<Budget> findByGroupAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndArchivedFalse(
            Group group,
            LocalDate dateForStartCheck,
            LocalDate dateForEndCheck
    );

    @Query("""
            SELECT COUNT(b) > 0
            FROM Budget b
            WHERE b.group = :group
              AND b.archived = false
              AND b.startDate <= :endDate
              AND b.endDate >= :startDate
            """)
    boolean existsOverlappingBudget(
            @Param("group") Group group,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(b) > 0
        FROM Budget b
        WHERE b.group = :group
          AND b.archived = false
          AND b.budgetId <> :budgetId
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
        """)
    boolean existsOverlappingBudgetExcludingCurrent(
            @Param("group") Group group,
            @Param("budgetId") UUID budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.group = :group
      AND e.expenseDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumByGroupAndDateRange(
            @Param("group") Group group,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT b
        FROM Budget b
        WHERE b.archived = false
          AND b.group.archived = false
          AND b.startDate <= :date
          AND b.endDate >= :date
          AND b.group IN (
              SELECT gm.group
              FROM GroupMember gm
              WHERE gm.user = :user
          )
        ORDER BY b.endDate ASC, b.group.name ASC
        """)
    List<Budget> findActiveBudgetsForUser(
            @Param("user") User user,
            @Param("date") LocalDate date
    );
}

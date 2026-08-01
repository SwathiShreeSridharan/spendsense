package com.spendsense.budget.repository;

import com.spendsense.budget.entity.Budget;
import com.spendsense.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByBudgetIdAndArchivedFalse(
            UUID budgetId
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
}

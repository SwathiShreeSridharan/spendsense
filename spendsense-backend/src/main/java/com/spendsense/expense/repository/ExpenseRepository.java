package com.spendsense.expense.repository;

import com.spendsense.expense.entity.Expense;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ser.impl.UnknownSerializer;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByCreatedBy(User createdBy);
}

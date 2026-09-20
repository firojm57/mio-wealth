package com.greenboard.investman.repository.expense;

import com.greenboard.investman.model.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByUser_UserId(String userId);
    List<Expense> findByUser_Id(String id);
}

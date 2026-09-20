package com.greenboard.investman.repository.expense;

import com.greenboard.investman.model.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
}

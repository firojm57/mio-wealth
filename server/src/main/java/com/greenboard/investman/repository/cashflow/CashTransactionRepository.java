package com.greenboard.investman.repository.cashflow;

import com.greenboard.investman.model.cashflow.CashTransaction;
import com.greenboard.investman.model.cashflow.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, String>, JpaSpecificationExecutor<CashTransaction> {

    Page<CashTransaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    @Query("SELECT t.transactionType, SUM(t.amount) " +
           "FROM CashTransaction t " +
           "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionType")
    List<Object[]> sumAmountByTransactionTypeBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t.transactionType, SUM(t.amount) " +
           "FROM CashTransaction t " +
           "GROUP BY t.transactionType")
    List<Object[]> sumAmountByTransactionType();

    @Query("SELECT FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM') as monthStr, t.transactionType, SUM(t.amount) " +
           "FROM CashTransaction t " +
           "WHERE t.transactionDate >= :startDate " +
           "GROUP BY FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM'), t.transactionType " +
           "ORDER BY monthStr ASC")
    List<Object[]> sumAmountMonthlyTrendsSince(@Param("startDate") LocalDate startDate);
}

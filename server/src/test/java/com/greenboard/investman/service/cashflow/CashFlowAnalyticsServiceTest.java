package com.greenboard.investman.service.cashflow;

import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.repository.cashflow.CashTransactionRepository;
import com.greenboard.investman.service.cashflow.impl.CashFlowAnalyticsServiceImpl;
import com.greenboard.investman.vo.cashflow.CashFlowSummaryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashFlowAnalyticsServiceTest {

    @Mock
    private CashTransactionRepository transactionRepository;

    private CashFlowAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new CashFlowAnalyticsServiceImpl(transactionRepository);
    }

    @Test
    void testGetSummaryCalculatesNetCashFlowAndExcludesTransfers() {
        List<Object[]> rows = List.of(
                new Object[]{TransactionType.INCOME, new BigDecimal("100000.00")},
                new Object[]{TransactionType.EXPENSE, new BigDecimal("40000.00")},
                new Object[]{TransactionType.TRANSFER, new BigDecimal("15000.00")}
        );

        when(transactionRepository.sumAmountByTransactionTypeBetweenDates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(rows);

        CashFlowSummaryVO summary = service.getSummary(2026, 4);

        assertEquals(new BigDecimal("100000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("40000.00"), summary.getTotalExpense());
        // Net Cash Flow = Income - Expense = 60,000 (Transfers excluded!)
        assertEquals(new BigDecimal("60000.00"), summary.getNetCashFlow());
        // Savings rate = (60000 / 100000) * 100 = 60.0%
        assertEquals(60.0, summary.getSavingsRate());
    }

    @Test
    void testGetSummaryAllTimeWhenYearAndMonthNull() {
        List<Object[]> rows = List.of(
                new Object[]{TransactionType.INCOME, new BigDecimal("150000.00")},
                new Object[]{TransactionType.EXPENSE, new BigDecimal("50000.00")}
        );

        when(transactionRepository.sumAmountByTransactionType()).thenReturn(rows);

        CashFlowSummaryVO summary = service.getSummary(null, null);

        assertEquals(new BigDecimal("150000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("50000.00"), summary.getTotalExpense());
        assertEquals(new BigDecimal("100000.00"), summary.getNetCashFlow());
        assertEquals(66.67, summary.getSavingsRate());
    }
}

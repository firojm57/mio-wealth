package com.greenboard.investman.service.cashflow.impl;

import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.repository.cashflow.CashTransactionRepository;
import com.greenboard.investman.service.cashflow.CashFlowAnalyticsService;
import com.greenboard.investman.vo.cashflow.CashFlowSummaryVO;
import com.greenboard.investman.vo.cashflow.CashFlowTrendVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CashFlowAnalyticsServiceImpl implements CashFlowAnalyticsService {

    private final CashTransactionRepository transactionRepository;

    public CashFlowAnalyticsServiceImpl(CashTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public CashFlowSummaryVO getSummary(Integer year, Integer month) {
        List<Object[]> results;
        if (year != null && month != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            results = transactionRepository.sumAmountByTransactionTypeBetweenDates(startDate, endDate);
        } else if (year != null) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            results = transactionRepository.sumAmountByTransactionTypeBetweenDates(startDate, endDate);
        } else {
            results = transactionRepository.sumAmountByTransactionType();
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalTransfer = BigDecimal.ZERO;

        for (Object[] row : results) {
            TransactionType type = (TransactionType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (amount == null) continue;

            if (type == TransactionType.INCOME) {
                totalIncome = totalIncome.add(amount);
            } else if (type == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(amount);
            } else if (type == TransactionType.TRANSFER) {
                totalTransfer = totalTransfer.add(amount);
            }
        }

        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);
        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netCashFlow.multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return CashFlowSummaryVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netCashFlow(netCashFlow)
                .savingsRate(savingsRate)
                .totalTransferInflow(totalTransfer)
                .totalTransferOutflow(totalTransfer)
                .build();
    }

    @Override
    public List<CashFlowTrendVO> getTrends(int months) {
        int count = (months <= 0) ? 6 : months;
        LocalDate startDate = LocalDate.now().minusMonths(count - 1).withDayOfMonth(1);

        List<Object[]> rawTrends = transactionRepository.sumAmountMonthlyTrendsSince(startDate);

        Map<String, BigDecimal[]> monthlyData = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 0; i < count; i++) {
            String monthKey = cursor.format(formatter);
            monthlyData.put(monthKey, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            cursor = cursor.plusMonths(1);
        }

        for (Object[] row : rawTrends) {
            String monthStr = (String) row[0];
            TransactionType type = (TransactionType) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            if (monthStr != null && monthlyData.containsKey(monthStr) && amount != null) {
                BigDecimal[] values = monthlyData.get(monthStr);
                if (type == TransactionType.INCOME) {
                    values[0] = values[0].add(amount);
                } else if (type == TransactionType.EXPENSE) {
                    values[1] = values[1].add(amount);
                }
            }
        }

        List<CashFlowTrendVO> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : monthlyData.entrySet()) {
            BigDecimal inc = entry.getValue()[0];
            BigDecimal exp = entry.getValue()[1];
            BigDecimal net = inc.subtract(exp);

            result.add(CashFlowTrendVO.builder()
                    .month(entry.getKey())
                    .income(inc)
                    .expense(exp)
                    .netCashFlow(net)
                    .build());
        }

        return result;
    }
}

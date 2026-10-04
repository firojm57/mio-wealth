package com.greenboard.investman.service.cashflow;

import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.vo.cashflow.CashTransactionRequestVO;
import com.greenboard.investman.vo.cashflow.CashTransactionResponseVO;
import com.greenboard.investman.vo.common.PageResponseVO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CashTransactionService {
    CashTransactionResponseVO createTransaction(CashTransactionRequestVO request);
    CashTransactionResponseVO updateTransaction(String id, CashTransactionRequestVO request);
    void deleteTransaction(String id);
    CashTransactionResponseVO getTransactionById(String id);
    PageResponseVO<CashTransactionResponseVO> getTransactions(
            TransactionType type,
            String categoryCode,
            LocalDate fromDate,
            LocalDate toDate,
            String search,
            Pageable pageable
    );
}

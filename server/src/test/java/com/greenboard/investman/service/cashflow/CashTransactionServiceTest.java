package com.greenboard.investman.service.cashflow;

import com.greenboard.investman.model.cashflow.CashTransaction;
import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.model.cashflow.TransferKind;
import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.repository.cashflow.CashTransactionRepository;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.service.cashflow.impl.CashTransactionServiceImpl;
import com.greenboard.investman.vo.cashflow.CashTransactionRequestVO;
import com.greenboard.investman.vo.cashflow.CashTransactionResponseVO;
import com.greenboard.investman.vo.cashflow.TransferDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashTransactionServiceTest {

    @Mock
    private CashTransactionRepository transactionRepository;

    @Mock
    private FinancialCategoryRepository categoryRepository;

    private CashTransactionService service;

    @BeforeEach
    void setUp() {
        service = new CashTransactionServiceImpl(transactionRepository, categoryRepository);
    }

    @Test
    void testCreateIncomeTransactionWithPeriod() {
        FinancialCategory category = FinancialCategory.builder()
                .code("SALARY")
                .name("Salary")
                .domain(DomainType.INCOME)
                .build();

        when(categoryRepository.findById("SALARY")).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(inv -> {
            CashTransaction t = inv.getArgument(0);
            t.setId("txn-1");
            return t;
        });

        CashTransactionRequestVO request = CashTransactionRequestVO.builder()
                .transactionType(TransactionType.INCOME)
                .title("April Salary")
                .amount(new BigDecimal("150000.00"))
                .transactionDate(LocalDate.of(2026, 4, 30))
                .periodStart(LocalDate.of(2026, 4, 1))
                .periodEnd(LocalDate.of(2026, 4, 30))
                .categoryCode("SALARY")
                .build();

        CashTransactionResponseVO response = service.createTransaction(request);

        assertNotNull(response);
        assertEquals("txn-1", response.getId());
        assertEquals(TransactionType.INCOME, response.getTransactionType());
        assertEquals(new BigDecimal("150000.00"), response.getAmount());
        assertEquals(LocalDate.of(2026, 4, 1), response.getPeriodStart());
    }

    @Test
    void testCreateTransferTransactionWithDetails() {
        FinancialCategory category = FinancialCategory.builder()
                .code("FAMILY_TRANSFER")
                .name("Family Transfer")
                .domain(DomainType.TRANSFER)
                .build();

        when(categoryRepository.findById("FAMILY_TRANSFER")).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(inv -> {
            CashTransaction t = inv.getArgument(0);
            t.setId("txn-2");
            return t;
        });

        CashTransactionRequestVO request = CashTransactionRequestVO.builder()
                .transactionType(TransactionType.TRANSFER)
                .title("Money sent to brother")
                .amount(new BigDecimal("10000.00"))
                .transactionDate(LocalDate.of(2026, 4, 15))
                .categoryCode("FAMILY_TRANSFER")
                .transferDetail(TransferDetailVO.builder()
                        .source("HDFC Bank")
                        .destination("Brother ICICI")
                        .transferKind(TransferKind.FAMILY)
                        .build())
                .build();

        CashTransactionResponseVO response = service.createTransaction(request);

        assertNotNull(response);
        assertEquals("txn-2", response.getId());
        assertEquals(TransactionType.TRANSFER, response.getTransactionType());
        assertNotNull(response.getTransferDetail());
        assertEquals("Brother ICICI", response.getTransferDetail().getDestination());
    }
}

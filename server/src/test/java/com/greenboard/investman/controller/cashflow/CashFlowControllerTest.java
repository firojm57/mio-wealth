package com.greenboard.investman.controller.cashflow;

import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.service.cashflow.CashFlowAnalyticsService;
import com.greenboard.investman.service.cashflow.CashTransactionService;
import com.greenboard.investman.vo.cashflow.CashFlowSummaryVO;
import com.greenboard.investman.vo.cashflow.CashTransactionResponseVO;
import com.greenboard.investman.vo.common.PageResponseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CashFlowControllerTest {

    @Mock
    private CashTransactionService transactionService;

    @Mock
    private CashFlowAnalyticsService analyticsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CashFlowController controller = new CashFlowController(transactionService, analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetSummaryReturns200() throws Exception {
        CashFlowSummaryVO summaryVO = CashFlowSummaryVO.builder()
                .totalIncome(new BigDecimal("50000.00"))
                .totalExpense(new BigDecimal("20000.00"))
                .netCashFlow(new BigDecimal("30000.00"))
                .savingsRate(60.0)
                .build();

        when(analyticsService.getSummary(eq(2026), eq(4))).thenReturn(summaryVO);

        mockMvc.perform(get("/api/v1/cash-flow/summary?year=2026&month=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(50000.00))
                .andExpect(jsonPath("$.netCashFlow").value(30000.00))
                .andExpect(jsonPath("$.savingsRate").value(60.0));
    }

    @Test
    void testGetTransactionsReturnsPaginated200() throws Exception {
        CashTransactionResponseVO item = CashTransactionResponseVO.builder()
                .id("txn-1")
                .transactionType(TransactionType.INCOME)
                .title("Salary")
                .amount(new BigDecimal("50000.00"))
                .transactionDate(LocalDate.of(2026, 4, 30))
                .categoryCode("SALARY")
                .build();

        PageResponseVO<CashTransactionResponseVO> page = PageResponseVO.<CashTransactionResponseVO>builder()
                .content(List.of(item))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(transactionService.getTransactions(eq(TransactionType.INCOME), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cash-flow/transactions?type=INCOME&page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("txn-1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}

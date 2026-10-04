package com.greenboard.investman.controller.cashflow;

import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.service.cashflow.CashFlowAnalyticsService;
import com.greenboard.investman.service.cashflow.CashTransactionService;
import com.greenboard.investman.vo.cashflow.CashFlowSummaryVO;
import com.greenboard.investman.vo.cashflow.CashFlowTrendVO;
import com.greenboard.investman.vo.cashflow.CashTransactionRequestVO;
import com.greenboard.investman.vo.cashflow.CashTransactionResponseVO;
import com.greenboard.investman.vo.common.PageResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-flow")
@Tag(name = "Cash Flow", description = "Cash flow management: Income, Expenses, Transfers, Metrics, and Trends")
public class CashFlowController {

    private final CashTransactionService transactionService;
    private final CashFlowAnalyticsService analyticsService;

    public CashFlowController(CashTransactionService transactionService,
                              CashFlowAnalyticsService analyticsService) {
        this.transactionService = transactionService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/transactions")
    @Operation(summary = "Create Transaction", description = "Records a new cash transaction (INCOME, EXPENSE, or TRANSFER)")
    public ResponseEntity<CashTransactionResponseVO> createTransaction(
            @Valid @RequestBody CashTransactionRequestVO request) {
        CashTransactionResponseVO response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get Paginated Transactions", description = "Retrieves transactions filtered by type, category, and date range")
    public ResponseEntity<PageResponseVO<CashTransactionResponseVO>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Direction.DESC) Pageable pageable) {

        PageResponseVO<CashTransactionResponseVO> page = transactionService.getTransactions(
                type, category, from, to, search, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get Transaction By ID", description = "Retrieves a single cash transaction with its details")
    public ResponseEntity<CashTransactionResponseVO> getTransactionById(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/transactions/{id}")
    @Operation(summary = "Update Transaction", description = "Updates an existing cash transaction")
    public ResponseEntity<CashTransactionResponseVO> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody CashTransactionRequestVO request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/transactions/{id}")
    @Operation(summary = "Delete Transaction", description = "Deletes a cash transaction and cascades to details")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Monthly Summary", description = "Returns aggregated cash flow metrics (Income, Expense, Net, Savings Rate)")
    public ResponseEntity<CashFlowSummaryVO> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(analyticsService.getSummary(year, month));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get Monthly Trends", description = "Returns historical monthly aggregated Income vs Expense trends")
    public ResponseEntity<List<CashFlowTrendVO>> getTrends(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getTrends(months));
    }
}

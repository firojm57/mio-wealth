package com.greenboard.investman.vo.cashflow;

import com.greenboard.investman.model.cashflow.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashTransactionResponseVO {

    private String id;
    private TransactionType transactionType;
    private String title;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String categoryCode;
    private String categoryName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String remarks;
    private String tags;
    private TransferDetailVO transferDetail;
    private TaxProfileVO taxProfile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.greenboard.investman.vo.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentVO {
    private String id;
    private String symbol;
    private String name;
    private String domain; // "INVESTMENT"
    private String categoryCode;
    private String categoryName;
    private double amount;
    private int quantity;
    private double unitPrice;
    private double returnRate;
    private String tags;
    private String action;
    private LocalDateTime investmentDate;
}

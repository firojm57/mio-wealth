package com.greenboard.investman.vo.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentSummaryVO {
    private double totalInvested;
    private double currentPortfolioValue;
    private double unrealizedProfitLoss;
    private double unrealizedProfitLossPercentage;
    private double realizedProfitLoss;
    private double realizedProfitLossPercentage;
    private double profitLossPercentage;
    private int assetClassesCount;
    private int totalHoldingsCount;
    private int activeHoldingsCount;
    private int soldHoldingsCount;
}

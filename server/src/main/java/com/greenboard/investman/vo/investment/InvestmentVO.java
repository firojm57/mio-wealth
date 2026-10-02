package com.greenboard.investman.vo.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String name;
    private String domain; // "INVESTMENT"
    private String categoryCode;
    private String categoryName;
    private double buyingPrice;
    private int quantity;
    private double unitPrice;
    private LocalDateTime investmentDate;

    @JsonProperty("isSold")
    private boolean isSold;

    @JsonProperty("isSold")
    public boolean isSold() {
        return this.isSold;
    }

    @JsonProperty("sold")
    public boolean getSold() {
        return this.isSold;
    }

    private Double sellingPrice;
    private LocalDateTime soldDate;
    private Double profitLoss;
    private Double returnPercentage;
    private Double percentChange;
    private Double currentPercentageChange;
    private Double currentValue;
    private Double unrealizedProfitLoss;
    private Double allocationPercentage;
    private String remarks;
    private String tags;

    public double getAmount() {
        return this.buyingPrice;
    }

    public String getSymbol() {
        return this.categoryCode != null ? this.categoryCode : this.name;
    }
}

package com.greenboard.investman.vo.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class InvestmentRequestVO {

    @NotBlank(message = "symbol is required")
    private String symbol;

    @NotBlank(message = "assetName is required")
    private String assetName;

    @NotBlank(message = "categoryCode is required")
    private String categoryCode;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Double amount;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;

    private Double unitPrice;
    private String remarks;
    private String tags;
    private String action;
}

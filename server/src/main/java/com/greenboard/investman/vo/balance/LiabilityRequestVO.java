package com.greenboard.investman.vo.balance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class LiabilityRequestVO {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "categoryCode is required")
    private String categoryCode;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Double amount;

    @NotNull(message = "interestRate is required")
    @PositiveOrZero(message = "interestRate must be zero or positive")
    private Double interestRate;

    private String remarks;
    private String tags;
}

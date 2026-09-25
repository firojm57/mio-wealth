package com.greenboard.investman.vo.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.greenboard.investman.config.FlexibleLocalDateTimeDeserializer;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentRequestVO {

    @NotBlank(message = "assetName is required")
    private String assetName;

    @NotBlank(message = "categoryCode is required")
    private String categoryCode;

    @NotNull(message = "buyingPrice is required")
    @Positive(message = "buyingPrice must be positive")
    private Double buyingPrice;

    private Integer quantity;
    private Double unitPrice;

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime investmentDate;

    private Boolean isSold;
    private Double sellingPrice;

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime soldDate;

    private String remarks;
    private String tags;
    private Double currentPercentageChange;
}

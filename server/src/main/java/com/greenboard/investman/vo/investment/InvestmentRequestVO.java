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

    @com.fasterxml.jackson.annotation.JsonProperty("isSold")
    private Boolean isSold;

    @com.fasterxml.jackson.annotation.JsonProperty("sold")
    public void setSold(Boolean sold) {
        if (this.isSold == null) {
            this.isSold = sold;
        }
    }

    private Double sellingPrice;

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime soldDate;

    private String remarks;
    private String tags;
    private Double percentChange;
    private Double currentPercentageChange;

    public Double getEffectivePercentChange() {
        if (this.percentChange != null && this.percentChange != 0.0) {
            return this.percentChange;
        }
        if (this.currentPercentageChange != null && this.currentPercentageChange != 0.0) {
            return this.currentPercentageChange;
        }
        if (this.percentChange != null) {
            return this.percentChange;
        }
        if (this.currentPercentageChange != null) {
            return this.currentPercentageChange;
        }
        return 0.0;
    }
}

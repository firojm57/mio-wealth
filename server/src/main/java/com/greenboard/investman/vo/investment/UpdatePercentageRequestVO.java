package com.greenboard.investman.vo.investment;

import jakarta.validation.constraints.NotNull;
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
public class UpdatePercentageRequestVO {

    private Double percentChange;
    private Double currentPercentageChange;

    public Double getEffectivePercentChange() {
        if (this.percentChange != null) return this.percentChange;
        if (this.currentPercentageChange != null) return this.currentPercentageChange;
        return 0.0;
    }
}

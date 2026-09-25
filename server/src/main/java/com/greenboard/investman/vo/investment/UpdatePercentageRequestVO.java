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

    @NotNull(message = "currentPercentageChange is required")
    private Double currentPercentageChange;
}

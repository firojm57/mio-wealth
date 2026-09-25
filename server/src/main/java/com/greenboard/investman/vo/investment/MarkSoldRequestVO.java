package com.greenboard.investman.vo.investment;

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
public class MarkSoldRequestVO {

    @NotNull(message = "sellingPrice is required")
    @Positive(message = "sellingPrice must be greater than zero")
    private Double sellingPrice;

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime soldDate;
}

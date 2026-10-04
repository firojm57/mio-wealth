package com.greenboard.investman.vo.cashflow;

import com.greenboard.investman.model.cashflow.TaxHead;
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
public class TaxProfileVO {
    private String financialYear;
    private TaxHead taxHead;
    private boolean isTaxable;
}

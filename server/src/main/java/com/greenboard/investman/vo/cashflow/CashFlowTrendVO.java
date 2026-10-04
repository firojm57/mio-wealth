package com.greenboard.investman.vo.cashflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashFlowTrendVO {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal netCashFlow;
}

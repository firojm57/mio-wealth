package com.greenboard.investman.vo.investment;

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
public class InvestmentVO {
    private Long id;
    private String name;
    private String symbol;
    private String allocation;
    private String shares;
    private String price;
    private String value;
    private String returnRate;
    private boolean up;
}

package com.greenboard.investman.vo.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSummaryVO {
    private double totalAssets;
    private double totalLiabilities;
    private double netWorth;
    private double equityRatio;
    private List<AssetItemVO> assets;
    private List<LiabilityItemVO> liabilities;
}

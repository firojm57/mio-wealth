package com.greenboard.investman.service.balance.impl;

import com.greenboard.investman.service.balance.AssetService;
import com.greenboard.investman.service.balance.LiabilityService;
import com.greenboard.investman.service.balance.PortfolioService;
import com.greenboard.investman.vo.balance.AssetItemVO;
import com.greenboard.investman.vo.balance.BalanceMetricsVO;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;
import com.greenboard.investman.vo.balance.LiabilityItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final AssetService assetService;
    private final LiabilityService liabilityService;

    public PortfolioServiceImpl(AssetService assetService, LiabilityService liabilityService) {
        this.assetService = assetService;
        this.liabilityService = liabilityService;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceMetricsVO getSummaryMetrics() {
        double totalAssets = assetService.calculateTotalAssets();
        double totalLiabilities = liabilityService.calculateTotalLiabilities();
        double netWorth = totalAssets - totalLiabilities;
        double equityRatio = totalAssets > 0 ? (netWorth / totalAssets) * 100.0 : 0.0;

        log.debug("Computed balance metrics: Assets={}, Liabilities={}, NetWorth={}",
                totalAssets, totalLiabilities, netWorth);

        return BalanceMetricsVO.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netWorth(netWorth)
                .equityRatio(equityRatio)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSummaryVO getFullBalanceSummary() {
        List<AssetItemVO> assets = assetService.getAssets();
        List<LiabilityItemVO> liabilities = liabilityService.getLiabilities();

        double totalAssets = assets.stream().mapToDouble(AssetItemVO::getAmount).sum();
        double totalLiabilities = liabilities.stream().mapToDouble(LiabilityItemVO::getAmount).sum();
        double netWorth = totalAssets - totalLiabilities;
        double equityRatio = totalAssets > 0 ? (netWorth / totalAssets) * 100.0 : 0.0;

        log.debug("Computed full balance summary: Assets count={}, Liabilities count={}",
                assets.size(), liabilities.size());

        return BalanceSummaryVO.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netWorth(netWorth)
                .equityRatio(equityRatio)
                .assets(assets)
                .liabilities(liabilities)
                .build();
    }
}

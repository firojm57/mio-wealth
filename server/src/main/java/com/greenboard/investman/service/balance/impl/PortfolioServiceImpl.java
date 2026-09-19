package com.greenboard.investman.service.balance.impl;

import com.greenboard.investman.service.balance.PortfolioService;
import com.greenboard.investman.vo.balance.AssetItemVO;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;
import com.greenboard.investman.vo.balance.LiabilityItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    @Override
    public BalanceSummaryVO getBalanceSummary(String userId) {
        List<AssetItemVO> assets = Arrays.asList(
                AssetItemVO.builder().name("Cash & Savings").category("Liquid").value(120150.0).change("+1.2%").icon("icon-cashflow").build(),
                AssetItemVO.builder().name("Brokerage & Stocks").category("Investment").value(452300.0).change("+6.4%").icon("icon-investments").build(),
                AssetItemVO.builder().name("Real Estate Portfolio").category("Property").value(1223200.0).change("+0.8%").icon("icon-home").build()
        );

        List<LiabilityItemVO> liabilities = Arrays.asList(
                LiabilityItemVO.builder().name("Home Mortgage").category("Secured Debt").value(512140.0).rate("3.85%").icon("icon-home").build(),
                LiabilityItemVO.builder().name("Student & Car Loans").category("Unsecured").value(31200.0).rate("4.5%").icon("icon-liabilities").build(),
                LiabilityItemVO.builder().name("Credit Cards Balance").category("Revolving").value(4000.0).rate("14.99%").icon("icon-expenses").build()
        );

        double totalAssets = assets.stream().mapToDouble(AssetItemVO::getValue).sum();
        double totalLiabilities = liabilities.stream().mapToDouble(LiabilityItemVO::getValue).sum();
        double netWorth = totalAssets - totalLiabilities;
        double equityRatio = totalAssets > 0 ? (netWorth / totalAssets) * 100 : 0;

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

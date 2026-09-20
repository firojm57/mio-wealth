package com.greenboard.investman.service.balance;

import com.greenboard.investman.vo.balance.BalanceMetricsVO;
import com.greenboard.investman.vo.balance.BalanceSummaryVO;

public interface PortfolioService {
    BalanceMetricsVO getSummaryMetrics();
    BalanceSummaryVO getFullBalanceSummary();
}

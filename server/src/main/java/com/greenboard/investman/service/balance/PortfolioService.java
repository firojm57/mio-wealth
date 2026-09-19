package com.greenboard.investman.service.balance;

import com.greenboard.investman.vo.balance.BalanceSummaryVO;

public interface PortfolioService {
    BalanceSummaryVO getBalanceSummary(String userId);
}

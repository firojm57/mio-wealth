package com.greenboard.investman.service.cashflow;

import com.greenboard.investman.vo.cashflow.CashFlowSummaryVO;
import com.greenboard.investman.vo.cashflow.CashFlowTrendVO;

import java.util.List;

public interface CashFlowAnalyticsService {
    CashFlowSummaryVO getSummary(Integer year, Integer month);
    List<CashFlowTrendVO> getTrends(int months);
}

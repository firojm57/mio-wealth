package com.greenboard.investman.service.investment;

import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;

import java.util.List;

public interface InvestmentService {
    List<InvestmentVO> getInvestmentsForUser(String userId);
    InvestmentVO createInvestment(String userId, InvestmentRequestVO request);
}

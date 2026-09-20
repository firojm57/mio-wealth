package com.greenboard.investman.service.investment;

import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;

import java.util.List;

public interface InvestmentService {
    List<InvestmentVO> getInvestments();
    InvestmentVO getInvestmentById(String id);
    InvestmentVO createInvestment(InvestmentRequestVO request);
    InvestmentVO updateInvestment(String id, InvestmentRequestVO request);
    void deleteInvestment(String id);
}

package com.greenboard.investman.service.balance;

import com.greenboard.investman.vo.balance.LiabilityItemVO;
import com.greenboard.investman.vo.balance.LiabilityRequestVO;

import java.util.List;

public interface LiabilityService {
    List<LiabilityItemVO> getLiabilities();
    double calculateTotalLiabilities();
    LiabilityItemVO createLiability(LiabilityRequestVO request);
    void deleteLiability(String liabilityId);
}

package com.greenboard.investman.service.balance;

import com.greenboard.investman.vo.balance.LiabilityItemVO;
import com.greenboard.investman.vo.balance.LiabilityRequestVO;

import java.util.List;

public interface LiabilityService {
    List<LiabilityItemVO> getLiabilitiesForUser(String userId);
    double calculateTotalLiabilities(String userId);
    LiabilityItemVO createLiability(String userId, LiabilityRequestVO request);
    void deleteLiability(String userId, String liabilityId);
}

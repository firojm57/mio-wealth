package com.greenboard.investman.service.balance;

import com.greenboard.investman.vo.balance.AssetItemVO;

import java.util.List;

public interface AssetService {
    List<AssetItemVO> getAssets();
    double calculateTotalAssets();
}

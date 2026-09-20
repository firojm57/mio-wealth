package com.greenboard.investman.service.balance.impl;

import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.model.saving.Saving;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.repository.saving.SavingRepository;
import com.greenboard.investman.service.balance.AssetService;
import com.greenboard.investman.vo.balance.AssetItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    private final InvestmentRepository investmentRepository;
    private final SavingRepository savingRepository;

    public AssetServiceImpl(InvestmentRepository investmentRepository, SavingRepository savingRepository) {
        this.investmentRepository = investmentRepository;
        this.savingRepository = savingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetItemVO> getAssetsForUser(String userId) {
        List<Investment> investments = investmentRepository.findByUser_UserId(userId);
        List<Saving> savings = savingRepository.findByUser_UserId(userId);

        List<AssetItemVO> assets = new ArrayList<>();

        for (Saving saving : savings) {
            assets.add(AssetItemVO.builder()
                    .id(saving.getId())
                    .name(saving.getInstitutionName())
                    .symbol(saving.getCategory() != null ? saving.getCategory().getCode() : "SAVINGS")
                    .domain("SAVING")
                    .categoryCode(saving.getCategory() != null ? saving.getCategory().getCode() : "SAVINGS_ACCOUNT")
                    .categoryName(saving.getCategory() != null ? saving.getCategory().getName() : "Savings")
                    .amount(saving.getAmount())
                    .quantity(1)
                    .unitPrice(saving.getAmount())
                    .tags(saving.getTags())
                    .date(saving.getSavingDate())
                    .build());
        }

        for (Investment inv : investments) {
            assets.add(AssetItemVO.builder()
                    .id(inv.getId())
                    .name(inv.getAssetName())
                    .symbol(inv.getSymbol())
                    .domain("INVESTMENT")
                    .categoryCode(inv.getCategory() != null ? inv.getCategory().getCode() : "STOCKS")
                    .categoryName(inv.getCategory() != null ? inv.getCategory().getName() : "Investment")
                    .amount(inv.getAmount())
                    .quantity(inv.getQuantity())
                    .unitPrice(inv.getUnitPrice())
                    .tags(inv.getTags())
                    .date(inv.getInvestmentDate())
                    .build());
        }

        return assets;
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalAssets(String userId) {
        List<AssetItemVO> assets = getAssetsForUser(userId);
        return assets.stream().mapToDouble(AssetItemVO::getAmount).sum();
    }
}

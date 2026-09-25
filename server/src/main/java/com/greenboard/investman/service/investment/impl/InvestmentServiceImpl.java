package com.greenboard.investman.service.investment.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import com.greenboard.investman.vo.investment.MarkSoldRequestVO;
import com.greenboard.investman.vo.investment.UpdatePercentageRequestVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    private final InvestmentRepository investmentRepository;
    private final FinancialCategoryRepository categoryRepository;
    private final TagService tagService;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 FinancialCategoryRepository categoryRepository,
                                 TagService tagService) {
        this.investmentRepository = investmentRepository;
        this.categoryRepository = categoryRepository;
        this.tagService = tagService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentVO> getInvestments() {
        List<Investment> investments = investmentRepository.findAllByOrderByInvestmentDateDesc();

        if (CollectionUtils.isEmpty(investments)) {
            return Collections.emptyList();
        }

        return investments.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentVO getInvestmentById(String id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));
        return toVO(investment);
    }

    @Override
    @Transactional
    public InvestmentVO createInvestment(InvestmentRequestVO request) {
        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        double unitPrice = request.getUnitPrice() != null && request.getUnitPrice() > 0
                ? request.getUnitPrice()
                : (request.getBuyingPrice() / quantity);

        boolean isSold = Boolean.TRUE.equals(request.getIsSold());
        Double sellingPrice = isSold ? request.getSellingPrice() : null;
        LocalDateTime soldDate = isSold
                ? (request.getSoldDate() != null ? request.getSoldDate() : LocalDateTime.now())
                : null;

        Double percentageChange = request.getCurrentPercentageChange() != null ? request.getCurrentPercentageChange() : 0.0;

        Investment investment = Investment.builder()
                .assetName(request.getAssetName().trim())
                .category(category)
                .buyingPrice(request.getBuyingPrice())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .investmentDate(request.getInvestmentDate() != null ? request.getInvestmentDate() : LocalDateTime.now())
                .isSold(isSold)
                .sellingPrice(sellingPrice)
                .soldDate(soldDate)
                .currentPercentageChange(percentageChange)
                .remarks(request.getRemarks())
                .tags(request.getTags())
                .build();

        Investment saved = investmentRepository.save(investment);
        log.info("Saved investment #{} ({}, buyingPrice: {}, isSold: {})", saved.getId(), saved.getAssetName(), saved.getBuyingPrice(), saved.isSold());

        if (StringUtils.isNotBlank(request.getTags())) {
            tagService.ensureTagsExist(request.getTags(), "INVESTMENT");
        }

        return toVO(saved);
    }

    @Override
    @Transactional
    public InvestmentVO updateInvestment(String id, InvestmentRequestVO request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));

        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        double unitPrice = request.getUnitPrice() != null && request.getUnitPrice() > 0
                ? request.getUnitPrice()
                : (request.getBuyingPrice() / quantity);

        boolean isSold = Boolean.TRUE.equals(request.getIsSold());
        Double sellingPrice = isSold ? request.getSellingPrice() : null;
        LocalDateTime soldDate = isSold
                ? (request.getSoldDate() != null ? request.getSoldDate() : (investment.getSoldDate() != null ? investment.getSoldDate() : LocalDateTime.now()))
                : null;

        investment.setAssetName(request.getAssetName().trim());
        investment.setCategory(category);
        investment.setBuyingPrice(request.getBuyingPrice());
        investment.setQuantity(quantity);
        investment.setUnitPrice(unitPrice);
        if (request.getInvestmentDate() != null) {
            investment.setInvestmentDate(request.getInvestmentDate());
        }
        investment.setSold(isSold);
        investment.setSellingPrice(sellingPrice);
        investment.setSoldDate(soldDate);
        if (request.getCurrentPercentageChange() != null) {
            investment.setCurrentPercentageChange(request.getCurrentPercentageChange());
        }
        investment.setRemarks(request.getRemarks());
        investment.setTags(request.getTags());

        Investment updated = investmentRepository.save(investment);
        log.info("Updated investment #{} ({})", updated.getId(), updated.getAssetName());

        if (StringUtils.isNotBlank(request.getTags())) {
            tagService.ensureTagsExist(request.getTags(), "INVESTMENT");
        }

        return toVO(updated);
    }

    @Override
    @Transactional
    public void deleteInvestment(String id) {
        if (!investmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Investment not found: " + id);
        }
        investmentRepository.deleteById(id);
        log.info("Deleted investment #{}", id);
    }

    @Override
    @Transactional
    public InvestmentVO markAsSold(String id, MarkSoldRequestVO request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));

        investment.setSold(true);
        investment.setSellingPrice(request.getSellingPrice());
        investment.setSoldDate(request.getSoldDate() != null ? request.getSoldDate() : LocalDateTime.now());

        Investment updated = investmentRepository.save(investment);
        log.info("Marked investment #{} as sold (sellingPrice: {})", updated.getId(), updated.getSellingPrice());

        return toVO(updated);
    }

    @Override
    @Transactional
    public InvestmentVO updateCurrentPercentage(String id, UpdatePercentageRequestVO request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));

        Double pct = request.getCurrentPercentageChange() != null ? request.getCurrentPercentageChange() : 0.0;
        investment.setCurrentPercentageChange(pct);

        Investment updated = investmentRepository.save(investment);
        log.info("Updated % change for investment #{} ({}) to {}%", updated.getId(), updated.getAssetName(), pct);

        return toVO(updated);
    }

    private InvestmentVO toVO(Investment investment) {
        double buyingPrice = investment.getBuyingPrice();
        Double profitLoss = null;
        Double returnPercentage = null;
        Double currentValue = null;
        Double unrealizedProfitLoss = null;
        Double currentPercentageChange = investment.getCurrentPercentageChange();

        if (investment.isSold() && investment.getSellingPrice() != null) {
            double pl = investment.getSellingPrice() - buyingPrice;
            double ret = buyingPrice > 0 ? (pl / buyingPrice) * 100.0 : 0.0;
            profitLoss = Math.round(pl * 100.0) / 100.0;
            returnPercentage = Math.round(ret * 100.0) / 100.0;
        } else if (!investment.isSold()) {
            double pct = currentPercentageChange != null ? currentPercentageChange : 0.0;
            double val = buyingPrice * (1.0 + (pct / 100.0));
            currentValue = Math.round(val * 100.0) / 100.0;
            unrealizedProfitLoss = Math.round((val - buyingPrice) * 100.0) / 100.0;
        }

        return InvestmentVO.builder()
                .id(investment.getId())
                .name(investment.getAssetName())
                .domain("INVESTMENT")
                .categoryCode(investment.getCategory() != null ? investment.getCategory().getCode() : null)
                .categoryName(investment.getCategory() != null ? investment.getCategory().getName() : null)
                .buyingPrice(buyingPrice)
                .quantity(investment.getQuantity())
                .unitPrice(investment.getUnitPrice())
                .investmentDate(investment.getInvestmentDate())
                .isSold(investment.isSold())
                .sellingPrice(investment.getSellingPrice())
                .soldDate(investment.getSoldDate())
                .profitLoss(profitLoss)
                .returnPercentage(returnPercentage)
                .currentPercentageChange(currentPercentageChange)
                .currentValue(currentValue)
                .unrealizedProfitLoss(unrealizedProfitLoss)
                .remarks(investment.getRemarks())
                .tags(investment.getTags())
                .build();
    }
}

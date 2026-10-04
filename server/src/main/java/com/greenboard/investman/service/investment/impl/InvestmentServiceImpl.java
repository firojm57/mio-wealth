package com.greenboard.investman.service.investment.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.cashflow.TaxHead;
import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.service.cashflow.CashTransactionService;
import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.cashflow.CashTransactionRequestVO;
import com.greenboard.investman.vo.cashflow.TaxProfileVO;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentSummaryVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import com.greenboard.investman.vo.investment.MarkSoldRequestVO;
import com.greenboard.investman.vo.investment.UpdatePercentageRequestVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    private final InvestmentRepository investmentRepository;
    private final FinancialCategoryRepository categoryRepository;
    private final TagService tagService;
    private final CashTransactionService cashTransactionService;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 FinancialCategoryRepository categoryRepository,
                                 TagService tagService) {
        this(investmentRepository, categoryRepository, tagService, null);
    }

    @Autowired
    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 FinancialCategoryRepository categoryRepository,
                                 TagService tagService,
                                 CashTransactionService cashTransactionService) {
        this.investmentRepository = investmentRepository;
        this.categoryRepository = categoryRepository;
        this.tagService = tagService;
        this.cashTransactionService = cashTransactionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentVO> getInvestments() {
        List<Investment> investments = investmentRepository.findAllByOrderByInvestmentDateDesc();

        if (CollectionUtils.isEmpty(investments)) {
            return Collections.emptyList();
        }

        double totalInvested = investments.stream()
                .mapToDouble(Investment::getBuyingPrice)
                .sum();

        return investments.stream()
                .map(inv -> {
                    InvestmentVO vo = toVO(inv);
                    double alloc = totalInvested > 0 ? (inv.getBuyingPrice() / totalInvested) * 100.0 : 0.0;
                    vo.setAllocationPercentage(Math.round(alloc * 10.0) / 10.0);
                    return vo;
                })
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

        Double percentageChange = request.getEffectivePercentChange();

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
                .percentChange(percentageChange)
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
        if (request.getPercentChange() != null) {
            investment.setPercentChange(request.getPercentChange());
        } else if (request.getCurrentPercentageChange() != null) {
            investment.setPercentChange(request.getCurrentPercentageChange());
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
        LocalDateTime soldDateTime = request.getSoldDate() != null ? request.getSoldDate() : LocalDateTime.now();
        investment.setSoldDate(soldDateTime);

        Investment updated = investmentRepository.save(investment);
        log.info("Marked investment #{} as sold (sellingPrice: {})", updated.getId(), updated.getSellingPrice());

        if (cashTransactionService != null && request.getSellingPrice() != null) {
            try {
                double buyingCost = investment.getBuyingPrice();
                double sellingPrice = request.getSellingPrice();
                double profitOrLoss = sellingPrice - buyingCost;

                long daysHeld = investment.getInvestmentDate() != null
                        ? ChronoUnit.DAYS.between(investment.getInvestmentDate(), soldDateTime)
                        : 0;
                boolean isLongTerm = daysHeld >= 365;

                LocalDate periodStart = investment.getInvestmentDate() != null
                        ? investment.getInvestmentDate().toLocalDate()
                        : null;
                LocalDate periodEnd = soldDateTime.toLocalDate();

                if (profitOrLoss > 0) {
                    CashTransactionRequestVO cashTxn = CashTransactionRequestVO.builder()
                            .transactionType(TransactionType.INCOME)
                            .title("Realized Profit: " + investment.getAssetName())
                            .amount(BigDecimal.valueOf(profitOrLoss).setScale(2, RoundingMode.HALF_UP))
                            .transactionDate(soldDateTime.toLocalDate())
                            .categoryCode("CAPITAL_GAINS")
                            .periodStart(periodStart)
                            .periodEnd(periodEnd)
                            .taxProfile(TaxProfileVO.builder()
                                    .taxHead(isLongTerm ? TaxHead.CAPITAL_GAINS_LTCG : TaxHead.CAPITAL_GAINS_STCG)
                                    .isTaxable(true)
                                    .build())
                            .remarks("Automated liquidation entry on sale of " + investment.getAssetName())
                            .build();
                    cashTransactionService.createTransaction(cashTxn);
                } else if (profitOrLoss < 0) {
                    CashTransactionRequestVO cashTxn = CashTransactionRequestVO.builder()
                            .transactionType(TransactionType.INCOME)
                            .title("Realized Capital Loss: " + investment.getAssetName())
                            .amount(BigDecimal.valueOf(Math.abs(profitOrLoss)).setScale(2, RoundingMode.HALF_UP))
                            .transactionDate(soldDateTime.toLocalDate())
                            .categoryCode("CAPITAL_GAINS")
                            .periodStart(periodStart)
                            .periodEnd(periodEnd)
                            .taxProfile(TaxProfileVO.builder()
                                    .taxHead(isLongTerm ? TaxHead.CAPITAL_GAINS_LTCG : TaxHead.CAPITAL_GAINS_STCG)
                                    .isTaxable(false)
                                    .build())
                            .remarks("Capital loss record from liquidation of " + investment.getAssetName())
                            .build();
                    cashTransactionService.createTransaction(cashTxn);
                }
            } catch (Exception ex) {
                log.warn("Could not record cash flow entry on investment liquidation #{}: {}", id, ex.getMessage());
            }
        }

        return toVO(updated);
    }

    @Override
    @Transactional
    public InvestmentVO updateCurrentPercentage(String id, UpdatePercentageRequestVO request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));

        Double pct = request.getEffectivePercentChange();
        investment.setPercentChange(pct);

        Investment updated = investmentRepository.save(investment);
        log.info("Updated % change for investment #{} ({}) to {}%", updated.getId(), updated.getAssetName(), pct);

        return toVO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentSummaryVO getInvestmentSummary() {
        List<Investment> investments = investmentRepository.findAll();
        if (CollectionUtils.isEmpty(investments)) {
            return InvestmentSummaryVO.builder().build();
        }

        double totalInvested = 0.0;
        double activeCost = 0.0, activeValue = 0.0;
        double soldCost = 0.0, soldProceeds = 0.0;
        int activeCount = 0, soldCount = 0;

        for (Investment inv : investments) {
            double cost = inv.getBuyingPrice();
            totalInvested += cost;

            if (inv.isSold()) {
                soldCount++;
                soldCost += cost;
                soldProceeds += inv.getSellingPrice() != null ? inv.getSellingPrice() : cost;
            } else {
                activeCount++;
                activeCost += cost;
                double pct = inv.getPercentChange() != null ? inv.getPercentChange() : 0.0;
                activeValue += cost * (1.0 + (pct / 100.0));
            }
        }

        double unrealizedPL = activeValue - activeCost;
        double unrealizedPct = activeCost > 0 ? (unrealizedPL / activeCost) * 100.0 : 0.0;
        double realizedPL = soldProceeds - soldCost;
        double realizedPct = soldCost > 0 ? (realizedPL / soldCost) * 100.0 : 0.0;

        return InvestmentSummaryVO.builder()
                .totalInvested(round2(totalInvested))
                .currentPortfolioValue(round2(activeValue + soldProceeds))
                .unrealizedProfitLoss(round2(unrealizedPL))
                .unrealizedProfitLossPercentage(round2(unrealizedPct))
                .realizedProfitLoss(round2(realizedPL))
                .realizedProfitLossPercentage(round2(realizedPct))
                .profitLossPercentage(round2(realizedPct))
                .totalHoldingsCount(investments.size())
                .activeHoldingsCount(activeCount)
                .soldHoldingsCount(soldCount)
                .build();
    }

    private static double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private InvestmentVO toVO(Investment investment) {
        double buyingPrice = investment.getBuyingPrice();
        Double profitLoss = null;
        Double returnPercentage = null;
        Double currentValue = null;
        Double unrealizedProfitLoss = null;
        Double percentChange = investment.getPercentChange() != null ? investment.getPercentChange() : 0.0;

        if (investment.isSold() && investment.getSellingPrice() != null) {
            double pl = investment.getSellingPrice() - buyingPrice;
            double ret = buyingPrice > 0 ? (pl / buyingPrice) * 100.0 : 0.0;
            profitLoss = Math.round(pl * 100.0) / 100.0;
            returnPercentage = Math.round(ret * 100.0) / 100.0;
            currentValue = investment.getSellingPrice();
            unrealizedProfitLoss = 0.0;
        } else if (!investment.isSold()) {
            double val = buyingPrice * (1.0 + (percentChange / 100.0));
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
                .percentChange(percentChange)
                .currentPercentageChange(percentChange)
                .currentValue(currentValue)
                .unrealizedProfitLoss(unrealizedProfitLoss)
                .remarks(investment.getRemarks())
                .tags(investment.getTags())
                .build();
    }
}

package com.greenboard.investman.service.investment.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.service.investment.InvestmentService;
import com.greenboard.investman.vo.investment.InvestmentRequestVO;
import com.greenboard.investman.vo.investment.InvestmentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentServiceImpl.class);

    private final InvestmentRepository investmentRepository;
    private final FinancialCategoryRepository categoryRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 FinancialCategoryRepository categoryRepository) {
        this.investmentRepository = investmentRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentVO> getInvestments() {
        List<Investment> investments = investmentRepository.findAll();

        if (investments.isEmpty()) {
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

        double unitPrice = request.getUnitPrice() != null && request.getUnitPrice() > 0
                ? request.getUnitPrice()
                : (request.getQuantity() > 0 ? request.getAmount() / request.getQuantity() : request.getAmount());

        Investment investment = new Investment();
        investment.setSymbol(request.getSymbol().trim().toUpperCase());
        investment.setAssetName(request.getAssetName().trim());
        investment.setCategory(category);
        investment.setAmount(request.getAmount());
        investment.setQuantity(request.getQuantity());
        investment.setUnitPrice(unitPrice);
        investment.setRemarks(request.getRemarks());
        investment.setTags(request.getTags());
        investment.setAction(request.getAction() != null && !request.getAction().isBlank() ? request.getAction() : "BUY");
        investment.setInvestmentDate(LocalDateTime.now());

        Investment saved = investmentRepository.save(investment);
        log.info("Saved investment #{} (symbol: {}, amount: {})", saved.getId(), saved.getSymbol(), saved.getAmount());

        return toVO(saved);
    }

    @Override
    @Transactional
    public InvestmentVO updateInvestment(String id, InvestmentRequestVO request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + id));

        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        double unitPrice = request.getUnitPrice() != null && request.getUnitPrice() > 0
                ? request.getUnitPrice()
                : (request.getQuantity() > 0 ? request.getAmount() / request.getQuantity() : request.getAmount());

        investment.setSymbol(request.getSymbol().trim().toUpperCase());
        investment.setAssetName(request.getAssetName().trim());
        investment.setCategory(category);
        investment.setAmount(request.getAmount());
        investment.setQuantity(request.getQuantity());
        investment.setUnitPrice(unitPrice);
        investment.setRemarks(request.getRemarks());
        investment.setTags(request.getTags());
        if (request.getAction() != null && !request.getAction().isBlank()) {
            investment.setAction(request.getAction());
        }

        Investment updated = investmentRepository.save(investment);
        log.info("Updated investment #{} (symbol: {}, amount: {})", updated.getId(), updated.getSymbol(), updated.getAmount());

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

    private InvestmentVO toVO(Investment inv) {
        return InvestmentVO.builder()
                .id(inv.getId())
                .symbol(inv.getSymbol())
                .name(inv.getAssetName())
                .domain("INVESTMENT")
                .categoryCode(inv.getCategory() != null ? inv.getCategory().getCode() : "STOCKS")
                .categoryName(inv.getCategory() != null ? inv.getCategory().getName() : "Investment")
                .amount(inv.getAmount())
                .quantity(inv.getQuantity())
                .unitPrice(inv.getUnitPrice())
                .returnRate(0.0)
                .tags(inv.getTags())
                .action(inv.getAction())
                .investmentDate(inv.getInvestmentDate())
                .build();
    }
}

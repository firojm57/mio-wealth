package com.greenboard.investman.service.investment.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.investment.Investment;
import com.greenboard.investman.model.user.User;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.investment.InvestmentRepository;
import com.greenboard.investman.repository.user.UserRepository;
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
    private final UserRepository userRepository;
    private final FinancialCategoryRepository categoryRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 UserRepository userRepository,
                                 FinancialCategoryRepository categoryRepository) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentVO> getInvestmentsForUser(String userId) {
        List<Investment> investments = investmentRepository.findByUser_UserId(userId);

        if (investments.isEmpty()) {
            return Collections.emptyList();
        }

        return investments.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvestmentVO createInvestment(String userId, InvestmentRequestVO request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

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
        investment.setUser(user);

        Investment saved = investmentRepository.save(investment);
        log.info("Saved investment #{} for user '{}' (symbol: {}, amount: {})",
                saved.getId(), userId, saved.getSymbol(), saved.getAmount());

        return toVO(saved);
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
                .returnRate(0.0) // Pure numeric, zero formatted string
                .tags(inv.getTags())
                .action(inv.getAction())
                .investmentDate(inv.getInvestmentDate())
                .build();
    }
}

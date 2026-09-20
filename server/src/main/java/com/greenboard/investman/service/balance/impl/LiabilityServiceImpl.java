package com.greenboard.investman.service.balance.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.liability.Liability;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.liability.LiabilityRepository;
import com.greenboard.investman.service.balance.LiabilityService;
import com.greenboard.investman.vo.balance.LiabilityItemVO;
import com.greenboard.investman.vo.balance.LiabilityRequestVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LiabilityServiceImpl implements LiabilityService {

    private static final Logger log = LoggerFactory.getLogger(LiabilityServiceImpl.class);

    private final LiabilityRepository liabilityRepository;
    private final FinancialCategoryRepository categoryRepository;

    public LiabilityServiceImpl(LiabilityRepository liabilityRepository,
                                FinancialCategoryRepository categoryRepository) {
        this.liabilityRepository = liabilityRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiabilityItemVO> getLiabilities() {
        List<Liability> liabilities = liabilityRepository.findAll();
        return liabilities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalLiabilities() {
        List<Liability> liabilities = liabilityRepository.findAll();
        return liabilities.stream().mapToDouble(Liability::getAmount).sum();
    }

    @Override
    @Transactional
    public LiabilityItemVO createLiability(LiabilityRequestVO request) {
        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        Liability liability = new Liability();
        liability.setName(request.getName());
        liability.setCategory(category);
        liability.setAmount(request.getAmount());
        liability.setInterestRate(request.getInterestRate());
        liability.setRemarks(request.getRemarks());
        liability.setTags(request.getTags());
        liability.setCreatedAt(LocalDateTime.now());

        Liability saved = liabilityRepository.save(liability);
        log.info("Created liability #{} (amount: {})", saved.getId(), saved.getAmount());

        return toVO(saved);
    }

    @Override
    @Transactional
    public void deleteLiability(String liabilityId) {
        Liability liability = liabilityRepository.findById(liabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Liability not found: " + liabilityId));
        liabilityRepository.delete(liability);
        log.info("Deleted liability #{}", liabilityId);
    }

    private LiabilityItemVO toVO(Liability liability) {
        return LiabilityItemVO.builder()
                .id(liability.getId())
                .name(liability.getName())
                .domain("LIABILITY")
                .categoryCode(liability.getCategory() != null ? liability.getCategory().getCode() : "DEBT")
                .categoryName(liability.getCategory() != null ? liability.getCategory().getName() : "Liability")
                .amount(liability.getAmount())
                .interestRate(liability.getInterestRate())
                .tags(liability.getTags())
                .createdAt(liability.getCreatedAt())
                .build();
    }
}

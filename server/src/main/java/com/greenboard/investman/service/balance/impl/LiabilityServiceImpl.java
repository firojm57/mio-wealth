package com.greenboard.investman.service.balance.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.liability.Liability;
import com.greenboard.investman.model.user.User;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.repository.liability.LiabilityRepository;
import com.greenboard.investman.repository.user.UserRepository;
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
    private final UserRepository userRepository;
    private final FinancialCategoryRepository categoryRepository;

    public LiabilityServiceImpl(LiabilityRepository liabilityRepository,
                                UserRepository userRepository,
                                FinancialCategoryRepository categoryRepository) {
        this.liabilityRepository = liabilityRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiabilityItemVO> getLiabilitiesForUser(String userId) {
        List<Liability> liabilities = liabilityRepository.findByUser_UserId(userId);
        return liabilities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalLiabilities(String userId) {
        List<Liability> liabilities = liabilityRepository.findByUser_UserId(userId);
        return liabilities.stream().mapToDouble(Liability::getAmount).sum();
    }

    @Override
    @Transactional
    public LiabilityItemVO createLiability(String userId, LiabilityRequestVO request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

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
        liability.setUser(user);

        Liability saved = liabilityRepository.save(liability);
        log.info("Created liability #{} for user '{}' (amount: {})", saved.getId(), userId, saved.getAmount());

        return toVO(saved);
    }

    @Override
    @Transactional
    public void deleteLiability(String userId, String liabilityId) {
        Liability liability = liabilityRepository.findByIdAndUser_UserId(liabilityId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Liability not found: " + liabilityId));
        liabilityRepository.delete(liability);
        log.info("Deleted liability #{} for user '{}'", liabilityId, userId);
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

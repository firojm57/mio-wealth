package com.greenboard.investman.service.category.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.service.category.CategoryService;
import com.greenboard.investman.vo.category.CategoryRequestVO;
import com.greenboard.investman.vo.category.CategoryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final FinancialCategoryRepository categoryRepository;

    public CategoryServiceImpl(FinancialCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryVO> getCategoriesByDomain(DomainType domain) {
        List<FinancialCategory> categories = domain != null
                ? categoryRepository.findByDomain(domain)
                : categoryRepository.findAll();

        return categories.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryVO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryVO createCategory(CategoryRequestVO request) {
        String normalizedCode = request.getCode().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");

        if (categoryRepository.existsById(normalizedCode)) {
            throw new IllegalArgumentException("Category code already exists: " + normalizedCode);
        }

        FinancialCategory category = FinancialCategory.builder()
                .code(normalizedCode)
                .name(request.getName().trim())
                .domain(request.getDomain())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .isCustom(true)
                .build();

        FinancialCategory saved = categoryRepository.save(category);
        log.info("Created custom category {} ({}) for domain {}", saved.getCode(), saved.getName(), saved.getDomain());

        return toVO(saved);
    }

    private CategoryVO toVO(FinancialCategory category) {
        return CategoryVO.builder()
                .code(category.getCode())
                .name(category.getName())
                .domain(category.getDomain())
                .description(category.getDescription())
                .isCustom(category.isCustom())
                .build();
    }
}

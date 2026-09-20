package com.greenboard.investman.service.category.impl;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.service.category.CategoryService;
import com.greenboard.investman.vo.category.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

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

    private CategoryVO toVO(FinancialCategory category) {
        return CategoryVO.builder()
                .code(category.getCode())
                .name(category.getName())
                .domain(category.getDomain())
                .description(category.getDescription())
                .build();
    }
}

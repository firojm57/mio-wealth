package com.greenboard.investman.service.category;

import com.greenboard.investman.model.common.DomainType;
import com.greenboard.investman.vo.category.CategoryRequestVO;
import com.greenboard.investman.vo.category.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> getCategoriesByDomain(DomainType domain);
    List<CategoryVO> getAllCategories();
    CategoryVO createCategory(CategoryRequestVO request);
}

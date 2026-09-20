package com.greenboard.investman.repository.category;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.common.DomainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, String> {
    List<FinancialCategory> findByDomain(DomainType domain);
}

package com.greenboard.investman.repository.cashflow;

import com.greenboard.investman.model.cashflow.TaxProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxProfileRepository extends JpaRepository<TaxProfile, String> {
    List<TaxProfile> findByFinancialYear(String financialYear);
}

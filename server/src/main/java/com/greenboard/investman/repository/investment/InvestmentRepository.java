package com.greenboard.investman.repository.investment;

import com.greenboard.investman.model.investment.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, String> {
}

package com.greenboard.investman.repository.investment;

import com.greenboard.investman.model.investment.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, String> {
    List<Investment> findByUser_UserId(String userId);
    List<Investment> findByUser_Id(String id);
}

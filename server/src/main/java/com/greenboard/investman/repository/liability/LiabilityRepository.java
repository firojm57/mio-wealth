package com.greenboard.investman.repository.liability;

import com.greenboard.investman.model.liability.Liability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiabilityRepository extends JpaRepository<Liability, String> {
}

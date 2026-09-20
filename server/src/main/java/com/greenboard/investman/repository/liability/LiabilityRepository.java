package com.greenboard.investman.repository.liability;

import com.greenboard.investman.model.liability.Liability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiabilityRepository extends JpaRepository<Liability, String> {
    List<Liability> findByUser_UserId(String userId);
    List<Liability> findByUser_Id(String id);
    Optional<Liability> findByIdAndUser_UserId(String id, String userId);
}

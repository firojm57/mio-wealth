package com.greenboard.investman.repository.saving;

import com.greenboard.investman.model.saving.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingRepository extends JpaRepository<Saving, String> {
    List<Saving> findByUser_UserId(String userId);
    List<Saving> findByUser_Id(String id);
}

package com.greenboard.investman.repository.saving;

import com.greenboard.investman.model.saving.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingRepository extends JpaRepository<Saving, String> {
}

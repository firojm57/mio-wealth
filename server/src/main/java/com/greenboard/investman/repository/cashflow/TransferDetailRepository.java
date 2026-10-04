package com.greenboard.investman.repository.cashflow;

import com.greenboard.investman.model.cashflow.TransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferDetailRepository extends JpaRepository<TransferDetail, String> {
}

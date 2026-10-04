package com.greenboard.investman.model.cashflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tax_profile")
public class TaxProfile {

    @Id
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "transaction_id")
    private CashTransaction transaction;

    @Column(name = "financial_year", length = 10, nullable = false)
    private String financialYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_head", length = 50)
    private TaxHead taxHead;

    @Builder.Default
    @Column(name = "is_taxable", nullable = false)
    private boolean isTaxable = true;
}

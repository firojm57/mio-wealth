package com.greenboard.investman.model.investment;

import com.greenboard.investman.model.category.FinancialCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "investment")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "symbol", length = 30, nullable = false)
    private String symbol;

    @Column(name = "asset_name", length = 150, nullable = false)
    private String assetName;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "investment_date", nullable = false)
    private LocalDateTime investmentDate;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private FinancialCategory category;
}

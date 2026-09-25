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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "investment")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "asset_name", length = 150, nullable = false)
    private String assetName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private FinancialCategory category;

    @Column(name = "buying_price", nullable = false)
    private double buyingPrice;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Builder.Default
    @Column(name = "unit_price", nullable = false)
    private double unitPrice = 0.0;

    @Column(name = "investment_date", nullable = false)
    private LocalDateTime investmentDate;

    @Builder.Default
    @Column(name = "is_sold", nullable = false)
    private boolean isSold = false;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Column(name = "sold_date")
    private LocalDateTime soldDate;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "tags", length = 255)
    private String tags;

    @Builder.Default
    @Column(name = "current_percentage_change")
    private Double currentPercentageChange = 0.0;
}

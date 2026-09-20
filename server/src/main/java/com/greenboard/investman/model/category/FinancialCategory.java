package com.greenboard.investman.model.category;

import com.greenboard.investman.model.common.DomainType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "financial_category", schema = "public")
public class FinancialCategory {

    @Id
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", length = 20, nullable = false)
    private DomainType domain;

    @Column(name = "description", length = 255)
    private String description;
}

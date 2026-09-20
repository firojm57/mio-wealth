package com.greenboard.investman.model.saving;

import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.model.user.User;
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
@Table(name = "saving")
public class Saving {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "institution_name", length = 150, nullable = false)
    private String institutionName;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "saving_date", nullable = false)
    private LocalDateTime savingDate;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private FinancialCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;
}

package com.greenboard.investman.model.investment;

import com.greenboard.investman.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "investment")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_id", nullable = false)
    private long id;

    @Column(name = "amount")
    private double amount;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "investment_date")
    private LocalDateTime investmentDate;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "action", length = 50)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "investment",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<InvestmentType> investmentTypes;
}

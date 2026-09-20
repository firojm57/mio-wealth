package com.greenboard.investman.vo.balance;

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
public class LiabilityItemVO {
    private String id;
    private String name;
    private String domain; // "LIABILITY"
    private String categoryCode;
    private String categoryName;
    private double amount;
    private double interestRate;
    private String tags;
    private LocalDateTime createdAt;
}

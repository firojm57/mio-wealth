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
public class AssetItemVO {
    private String id;
    private String name;
    private String symbol;
    private String domain; // "INVESTMENT" or "SAVING"
    private String categoryCode;
    private String categoryName;
    private double amount;
    private int quantity;
    private double unitPrice;
    private String tags;
    private LocalDateTime date;
}

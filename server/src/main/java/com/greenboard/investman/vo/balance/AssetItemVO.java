package com.greenboard.investman.vo.balance;

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
public class AssetItemVO {
    private String name;
    private String category;
    private double value;
    private String change;
    private String icon;
}

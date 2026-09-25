package com.greenboard.investman.vo.category;

import com.greenboard.investman.model.common.DomainType;
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
public class CategoryVO {
    private String code;
    private String name;
    private DomainType domain;
    private String description;
    private boolean isCustom;
}

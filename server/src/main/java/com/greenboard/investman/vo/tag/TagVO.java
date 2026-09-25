package com.greenboard.investman.vo.tag;

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
public class TagVO {
    private String id;
    private String name;
    private String domain;
    private boolean isSystem;
}

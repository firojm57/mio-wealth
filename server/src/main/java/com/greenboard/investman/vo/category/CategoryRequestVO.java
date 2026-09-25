package com.greenboard.investman.vo.category;

import com.greenboard.investman.model.common.DomainType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CategoryRequestVO {

    @NotBlank(message = "Category code is required")
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "Category code must be uppercase alphanumeric with underscores (2-50 chars)")
    private String code;

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Domain is required")
    private DomainType domain;

    private String description;
}

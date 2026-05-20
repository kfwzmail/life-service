package com.std.lifeService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryBreakdownVO {
    private Long categoryId;
    private String categoryName;
    private String icon;
    private BigDecimal amount;
    private BigDecimal percentage;
}

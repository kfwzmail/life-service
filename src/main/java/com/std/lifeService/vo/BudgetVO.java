package com.std.lifeService.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetVO {
    private Long categoryId;
    private String categoryName;
    private String icon;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal remaining;
    private BigDecimal usageRate;
}

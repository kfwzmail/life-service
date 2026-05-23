package com.std.lifeService.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthStatsVO {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal netBalance;
    private List<CategoryBreakdownVO> expenseBreakdown;
    private List<CategoryBreakdownVO> incomeBreakdown;
}

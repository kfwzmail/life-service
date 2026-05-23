package com.std.lifeService.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TodayStatsVO {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal netBalance;
}

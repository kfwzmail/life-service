package com.std.lifeService.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class YearStatsVO {
    private String month;
    private BigDecimal expense;
    private BigDecimal income;
}

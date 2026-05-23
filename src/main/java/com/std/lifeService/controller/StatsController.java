package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.vo.CategoryBreakdownVO;
import com.std.lifeService.vo.MonthStatsVO;
import com.std.lifeService.vo.TodayStatsVO;
import com.std.lifeService.security.LoginUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final BillMapper billMapper;

    @Operation(summary = "今日统计")
    @GetMapping("/today")
    public Result<TodayStatsVO> today() {
        Long userId = LoginUserContext.getUserId();
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);

        List<Map<String, Object>> sums = billMapper.sumByType(userId, start, end);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Map<String, Object> row : sums) {
            String type = (String) row.get("type");
            BigDecimal total = (BigDecimal) row.get("total");
            if ("EXPENSE".equals(type)) {
                expense = total;
            } else if ("INCOME".equals(type)) {
                income = total;
            }
        }
        return Result.success(new TodayStatsVO(expense, income, income.subtract(expense)));
    }

    @Operation(summary = "月统计 + 分类占比")
    @GetMapping("/month")
    public Result<MonthStatsVO> month(@RequestParam(required = false) String yearMonth) {
        Long userId = LoginUserContext.getUserId();
        YearMonth ym = (yearMonth != null) ? YearMonth.parse(yearMonth) : YearMonth.now();
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Map<String, Object>> sums = billMapper.sumByType(userId, start, end);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Map<String, Object> row : sums) {
            String type = (String) row.get("type");
            BigDecimal total = (BigDecimal) row.get("total");
            if ("EXPENSE".equals(type)) {
                expense = total;
            } else if ("INCOME".equals(type)) {
                income = total;
            }
        }

        List<Map<String, Object>> expenseRows = billMapper.sumByCategory(userId, "EXPENSE", start, end);
        List<CategoryBreakdownVO> expenseBreakdown = toBreakdown(expenseRows, expense);

        List<Map<String, Object>> incomeRows = billMapper.sumByCategory(userId, "INCOME", start, end);
        List<CategoryBreakdownVO> incomeBreakdown = toBreakdown(incomeRows, income);

        return Result.success(new MonthStatsVO(expense, income, income.subtract(expense),
                expenseBreakdown, incomeBreakdown));
    }

    private List<CategoryBreakdownVO> toBreakdown(List<Map<String, Object>> rows, BigDecimal total) {
        List<CategoryBreakdownVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long categoryId = ((Number) row.get("categoryId")).longValue();
            String categoryName = (String) row.get("categoryName");
            String icon = (String) row.get("icon");
            BigDecimal amount = (BigDecimal) row.get("amount");
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            list.add(new CategoryBreakdownVO(categoryId, categoryName, icon, amount, percentage));
        }
        return list;
    }
}

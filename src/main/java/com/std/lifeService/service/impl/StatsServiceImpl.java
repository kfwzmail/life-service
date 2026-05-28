package com.std.lifeService.service.impl;

import com.std.lifeService.constant.CategoryType;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.service.StatsService;
import com.std.lifeService.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final BillMapper billMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int FIRST_DAY_OF_MONTH = 1;
    private static final int FIRST_MONTH = 1;
    private static final int MONTHS_PER_YEAR = 12;

    @Override
    public TodayStatsVO today(Long userId) {
        String start = LocalDate.now().atStartOfDay().format(DTF);
        String end = LocalDate.now().plusDays(1).atStartOfDay().format(DTF);
        List<Map<String, Object>> sums = billMapper.sumByType(userId, start, end);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Map<String, Object> row : sums) {
            String type = (String) row.get("type");
            BigDecimal total = (BigDecimal) row.get("total");
            // 根据账单类型分别累加支出和收入
            if (CategoryType.EXPENSE.name().equals(type)) {
                expense = total;
            } else if (CategoryType.INCOME.name().equals(type)) {
                income = total;
            }
        }
        return new TodayStatsVO(expense, income, income.subtract(expense));
    }

    @Override
    public MonthStatsVO month(Long userId, String yearMonth) {
        YearMonth ym = (yearMonth != null) ? YearMonth.parse(yearMonth) : YearMonth.now();
        String start = ym.atDay(1).atStartOfDay().format(DTF);
        String end = ym.plusMonths(1).atDay(1).atStartOfDay().format(DTF);

        List<Map<String, Object>> sums = billMapper.sumByType(userId, start, end);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Map<String, Object> row : sums) {
            String type = (String) row.get("type");
            BigDecimal total = (BigDecimal) row.get("total");
            if (CategoryType.EXPENSE.name().equals(type)) {
                expense = total;
            } else if (CategoryType.INCOME.name().equals(type)) {
                income = total;
            }
        }

        List<Map<String, Object>> expenseRows = billMapper.sumByCategory(userId, CategoryType.EXPENSE.name(), start, end);
        List<CategoryBreakdownVO> expenseBreakdown = toBreakdown(expenseRows, expense);

        List<Map<String, Object>> incomeRows = billMapper.sumByCategory(userId, CategoryType.INCOME.name(), start, end);
        List<CategoryBreakdownVO> incomeBreakdown = toBreakdown(incomeRows, income);

        return new MonthStatsVO(expense, income, income.subtract(expense),
                expenseBreakdown, incomeBreakdown);
    }

    @Override
    public List<DailyTrendVO> dailyTrend(Long userId, String yearMonth) {
        YearMonth ym = (yearMonth != null) ? YearMonth.parse(yearMonth) : YearMonth.now();
        String start = ym.atDay(1).atStartOfDay().format(DTF);
        String end = ym.plusMonths(1).atDay(1).atStartOfDay().format(DTF);

        List<Map<String, Object>> rows = billMapper.sumByDay(userId, start, end);

        // 填充当月每一天的数据，无账单的日期补零
        int days = ym.lengthOfMonth();
        List<DailyTrendVO> result = new ArrayList<>(days);
        for (int d = FIRST_DAY_OF_MONTH; d <= days; d++) {
            String date = ym + "-" + String.format("%02d", d);
            BigDecimal expense = BigDecimal.ZERO;
            BigDecimal income = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (date.equals(row.get("date"))) {
                    if (CategoryType.EXPENSE.name().equals(row.get("type"))) {
                        expense = (BigDecimal) row.get("total");
                    } else if (CategoryType.INCOME.name().equals(row.get("type"))) {
                        income = (BigDecimal) row.get("total");
                    }
                }
            }
            result.add(new DailyTrendVO(date, expense, income));
        }
        return result;
    }

    @Override
    public List<YearStatsVO> yearly(Long userId, int year) {
        String start = year + "-01-01 00:00:00";
        String end = (year + 1) + "-01-01 00:00:00";

        List<Map<String, Object>> rows = billMapper.sumByMonth(userId, start, end);

        // 填充 1~12 月数据，无账单的月份补零
        List<YearStatsVO> result = new ArrayList<>(MONTHS_PER_YEAR);
        for (int m = FIRST_MONTH; m <= MONTHS_PER_YEAR; m++) {
            String month = year + "-" + String.format("%02d", m);
            BigDecimal expense = BigDecimal.ZERO;
            BigDecimal income = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (month.equals(row.get("month"))) {
                    if (CategoryType.EXPENSE.name().equals(row.get("type"))) {
                        expense = (BigDecimal) row.get("total");
                    } else if (CategoryType.INCOME.name().equals(row.get("type"))) {
                        income = (BigDecimal) row.get("total");
                    }
                }
            }
            result.add(new YearStatsVO(month, expense, income));
        }
        return result;
    }

    /**
     * 将分类汇总行转为占比VO列表，计算每个分类占总体的百分比
     */
    private List<CategoryBreakdownVO> toBreakdown(List<Map<String, Object>> rows, BigDecimal total) {
        List<CategoryBreakdownVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long categoryId = ((Number) row.get("categoryId")).longValue();
            String categoryName = (String) row.get("categoryName");
            String icon = (String) row.get("icon");
            BigDecimal amount = (BigDecimal) row.get("amount");
            // 总金额大于0时才计算百分比，避免除以零
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(HUNDRED).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            list.add(new CategoryBreakdownVO(categoryId, categoryName, icon, amount, percentage));
        }
        return list;
    }
}

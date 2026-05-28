package com.std.lifeService.service.impl;

import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.service.StatsService;
import com.std.lifeService.vo.CategoryBreakdownVO;
import com.std.lifeService.vo.DailyTrendVO;
import com.std.lifeService.vo.MonthStatsVO;
import com.std.lifeService.vo.TodayStatsVO;
import com.std.lifeService.vo.YearStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final BillMapper billMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            if ("EXPENSE".equals(type)) {
                expense = total;
            } else if ("INCOME".equals(type)) {
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

        return new MonthStatsVO(expense, income, income.subtract(expense),
                expenseBreakdown, incomeBreakdown);
    }

    @Override
    public List<DailyTrendVO> dailyTrend(Long userId, String yearMonth) {
        YearMonth ym = (yearMonth != null) ? YearMonth.parse(yearMonth) : YearMonth.now();
        String start = ym.atDay(1).atStartOfDay().format(DTF);
        String end = ym.plusMonths(1).atDay(1).atStartOfDay().format(DTF);

        List<Map<String, Object>> rows = billMapper.sumByDay(userId, start, end);

        int days = ym.lengthOfMonth();
        List<DailyTrendVO> result = new ArrayList<>(days);
        for (int d = 1; d <= days; d++) {
            String date = ym + "-" + String.format("%02d", d);
            BigDecimal expense = BigDecimal.ZERO;
            BigDecimal income = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (date.equals(row.get("date"))) {
                    if ("EXPENSE".equals(row.get("type"))) {
                        expense = (BigDecimal) row.get("total");
                    } else if ("INCOME".equals(row.get("type"))) {
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

        List<YearStatsVO> result = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            String month = year + "-" + String.format("%02d", m);
            BigDecimal expense = BigDecimal.ZERO;
            BigDecimal income = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (month.equals(row.get("month"))) {
                    if ("EXPENSE".equals(row.get("type"))) {
                        expense = (BigDecimal) row.get("total");
                    } else if ("INCOME".equals(row.get("type"))) {
                        income = (BigDecimal) row.get("total");
                    }
                }
            }
            result.add(new YearStatsVO(month, expense, income));
        }
        return result;
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

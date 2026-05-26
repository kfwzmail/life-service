package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.dao.BudgetMapper;
import com.std.lifeService.dao.CategoryMapper;
import com.std.lifeService.entity.Budget;
import com.std.lifeService.entity.Category;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.service.BudgetService;
import com.std.lifeService.vo.BudgetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final BillMapper billMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public List<BudgetVO> list(Long userId, String budgetMonth) {
        List<Budget> budgets = budgetMapper.selectList(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getBudgetMonth, budgetMonth)
        );

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, 1)
                        .or()
                        .eq(Category::getUserId, userId)
        );

        Map<Long, Category> catMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        return budgets.stream().map(b -> {
            Category cat = catMap.get(b.getCategoryId());
            return new BudgetVO(
                    b.getCategoryId(),
                    cat != null ? cat.getName() : "总预算",
                    cat != null ? cat.getIcon() : "📊",
                    b.getAmount(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }).collect(Collectors.toList());
    }

    @Override
    public void save(Long userId, String budgetMonth, List<BudgetVO> items) {
        for (BudgetVO item : items) {
            if (item.getBudgetAmount() == null || item.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Budget exist = budgetMapper.selectOne(
                    new LambdaQueryWrapper<Budget>()
                            .eq(Budget::getUserId, userId)
                            .eq(Budget::getBudgetMonth, budgetMonth)
                            .eq(item.getCategoryId() != null, Budget::getCategoryId, item.getCategoryId())
                            .isNull(item.getCategoryId() == null, Budget::getCategoryId)
            );
            if (exist != null) {
                exist.setAmount(item.getBudgetAmount());
                budgetMapper.updateById(exist);
            } else {
                Budget b = new Budget();
                b.setUserId(userId);
                b.setBudgetMonth(budgetMonth);
                b.setCategoryId(item.getCategoryId());
                b.setAmount(item.getBudgetAmount());
                budgetMapper.insert(b);
            }
        }
    }

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<BudgetVO> comparison(Long userId, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);
        String start = ym.atDay(1).atStartOfDay().format(DTF);
        String end = ym.plusMonths(1).atDay(1).atStartOfDay().format(DTF);

        List<Budget> budgets = budgetMapper.selectList(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getBudgetMonth, budgetMonth)
        );

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, 1)
                        .or()
                        .eq(Category::getUserId, userId)
        );
        Map<Long, Category> catMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<Map<String, Object>> expenseRows = billMapper.sumByCategory(userId, "EXPENSE", start, end);
        Map<Long, BigDecimal> actualMap = expenseRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("categoryId")).longValue(),
                        row -> (BigDecimal) row.get("amount"),
                        (a, b) -> a
                ));

        List<BudgetVO> result = new ArrayList<>();
        for (Budget b : budgets) {
            Category cat = catMap.get(b.getCategoryId());
            BigDecimal actual = actualMap.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
            BigDecimal remaining = b.getAmount().subtract(actual);
            BigDecimal usageRate = b.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? actual.multiply(new BigDecimal("100")).divide(b.getAmount(), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(new BudgetVO(
                    b.getCategoryId(),
                    cat != null ? cat.getName() : "总预算",
                    cat != null ? cat.getIcon() : "📊",
                    b.getAmount(),
                    actual,
                    remaining,
                    usageRate
            ));
        }
        return result;
    }
}

package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.constant.CategoryType;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.dao.BudgetMapper;
import com.std.lifeService.dao.CategoryMapper;
import com.std.lifeService.entity.Budget;
import com.std.lifeService.entity.Category;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final BillMapper billMapper;
    private final CategoryMapper categoryMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final String DEFAULT_ICON = "📊";
    private static final String DEFAULT_CATEGORY_NAME = "总预算";
    private static final int DEFAULT_CATEGORY_FLAG = 1;

    @Override
    public List<BudgetVO> list(Long userId, String budgetMonth) {
        List<Budget> budgets = budgetMapper.selectList(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getBudgetMonth, budgetMonth)
        );

        // 查询系统预设 + 用户自定义分类
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, DEFAULT_CATEGORY_FLAG)
                        .or()
                        .eq(Category::getUserId, userId)
        );

        // 将分类列表转为 categoryId -> Category 映射，便于快速查找
        Map<Long, Category> catMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 预算信息与分类信息合并输出
        return budgets.stream().map(b -> {
            Category cat = catMap.get(b.getCategoryId());
            return new BudgetVO(
                    b.getCategoryId(),
                    cat != null ? cat.getName() : DEFAULT_CATEGORY_NAME,
                    cat != null ? cat.getIcon() : DEFAULT_ICON,
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
            // 跳过空预算或金额≤0的预算项
            if (item.getBudgetAmount() == null || item.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 查询是否已存在当月该分类的预算记录
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

        // 查询系统预设 + 用户自定义分类
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, DEFAULT_CATEGORY_FLAG)
                        .or()
                        .eq(Category::getUserId, userId)
        );
        // 将分类列表转为 categoryId -> Category 映射
        Map<Long, Category> catMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 查询当月各分类实际支出，转为 categoryId -> 实际金额 映射
        List<Map<String, Object>> expenseRows = billMapper.sumByCategory(userId, CategoryType.EXPENSE.name(), start, end);
        Map<Long, BigDecimal> actualMap = expenseRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("categoryId")).longValue(),
                        row -> (BigDecimal) row.get("amount"),
                        (a, b) -> a
                ));

        // 计算每个预算项的使用率和剩余额度
        List<BudgetVO> result = new ArrayList<>();
        for (Budget b : budgets) {
            Category cat = catMap.get(b.getCategoryId());
            BigDecimal actual = actualMap.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
            BigDecimal remaining = b.getAmount().subtract(actual);
            // 预算金额大于0时计算使用率，避免除以零
            BigDecimal usageRate = b.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? actual.multiply(HUNDRED).divide(b.getAmount(), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(new BudgetVO(
                    b.getCategoryId(),
                    cat != null ? cat.getName() : DEFAULT_CATEGORY_NAME,
                    cat != null ? cat.getIcon() : DEFAULT_ICON,
                    b.getAmount(),
                    actual,
                    remaining,
                    usageRate
            ));
        }
        return result;
    }
}

package com.std.lifeService.config;

import com.std.lifeService.dao.CategoryMapper;
import com.std.lifeService.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryMapper categoryMapper;

    private static final List<Category> DEFAULT_CATEGORIES = List.of(
            create("餐饮", "EXPENSE", "🍽️", 1),
            create("交通", "EXPENSE", "🚗", 2),
            create("购物", "EXPENSE", "🛒", 3),
            create("住房", "EXPENSE", "🏠", 4),
            create("娱乐", "EXPENSE", "🎬", 5),
            create("医疗", "EXPENSE", "💊", 6),
            create("教育", "EXPENSE", "📚", 7),
            create("通讯", "EXPENSE", "📱", 8),
            create("服饰", "EXPENSE", "👗", 9),
            create("其他", "EXPENSE", "💸", 99),
            create("工资", "INCOME", "💰", 1),
            create("奖金", "INCOME", "🧧", 2),
            create("兼职", "INCOME", "💼", 3),
            create("理财", "INCOME", "📈", 4),
            create("报销", "INCOME", "📋", 5),
            create("其他", "INCOME", "🎁", 99)
    );

    private static Category create(String name, String type, String icon, int sortOrder) {
        Category c = new Category();
        c.setName(name);
        c.setType(type);
        c.setIcon(icon);
        c.setSortOrder(sortOrder);
        c.setIsDefault(1);
        return c;
    }

    @Override
    public void run(String... args) {
        Long count = categoryMapper.selectCount(null);
        if (count == 0) {
            DEFAULT_CATEGORIES.forEach(categoryMapper::insert);
            log.info("已初始化 {} 个预设分类", DEFAULT_CATEGORIES.size());
        }
    }
}

package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.dao.CategoryMapper;
import com.std.lifeService.entity.Category;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private static final int DEFAULT_CATEGORY_FLAG = 1;
    private static final int CUSTOM_CATEGORY_FLAG = 0;
    private static final String DEFAULT_ICON = "📦";
    private static final int DEFAULT_SORT_ORDER = 99;

    @Override
    public List<Category> listByUser(Long userId) {
        // 查询系统预设分类 + 用户自定义分类
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, DEFAULT_CATEGORY_FLAG)
                        .or()
                        .eq(Category::getUserId, userId)
        );
    }

    @Override
    public Category addCustom(Long userId, String name, String type, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon != null ? icon : DEFAULT_ICON);
        category.setSortOrder(DEFAULT_SORT_ORDER);
        category.setIsDefault(CUSTOM_CATEGORY_FLAG);
        category.setUserId(userId);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void deleteCustom(Long categoryId, Long userId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 系统预设分类不允许删除
        if (category.getIsDefault() == DEFAULT_CATEGORY_FLAG) {
            throw new BusinessException(400, "不能删除系统预设分类");
        }
        // 仅允许删除自己创建的分类
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能删除他人的分类");
        }
        categoryMapper.deleteById(categoryId);
    }
}

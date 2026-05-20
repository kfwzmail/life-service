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

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listByUser(Long userId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, 1)
                        .or()
                        .eq(Category::getUserId, userId)
        );
    }

    @Override
    public Category addCustom(Long userId, String name, String type, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon != null ? icon : "📦");
        category.setSortOrder(99);
        category.setIsDefault(0);
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
        if (category.getIsDefault() == 1) {
            throw new BusinessException(400, "不能删除系统预设分类");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能删除他人的分类");
        }
        categoryMapper.deleteById(categoryId);
    }
}

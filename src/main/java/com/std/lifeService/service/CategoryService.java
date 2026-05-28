package com.std.lifeService.service;

import com.std.lifeService.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 查询用户可见的分类列表（系统预设 + 用户自定义）
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    List<Category> listByUser(Long userId);

    /**
     * 新增用户自定义分类
     *
     * @param userId 用户ID
     * @param name   分类名称
     * @param type   分类类型（EXPENSE/INCOME）
     * @param icon   图标，为null时使用默认图标
     * @return 创建后的分类
     */
    Category addCustom(Long userId, String name, String type, String icon);

    /**
     * 删除用户自定义分类
     *
     * @param categoryId 分类ID
     * @param userId     用户ID（用于权限校验）
     */
    void deleteCustom(Long categoryId, Long userId);
}

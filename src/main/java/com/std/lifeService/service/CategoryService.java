package com.std.lifeService.service;

import com.std.lifeService.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listByUser(Long userId);
    Category addCustom(Long userId, String name, String type, String icon);
    void deleteCustom(Long categoryId, Long userId);
}

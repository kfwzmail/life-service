package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.dto.CategoryRequest;
import com.std.lifeService.entity.Category;
import com.std.lifeService.security.LoginUserContext;
import com.std.lifeService.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表（预设 + 自定义）")
    @GetMapping
    public Result<List<Category>> list() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(categoryService.listByUser(userId));
    }

    @Operation(summary = "新增自定义分类")
    @PostMapping
    public Result<Category> add(@Valid @RequestBody CategoryRequest req) {
        Long userId = LoginUserContext.getUserId();
        Category category = categoryService.addCustom(userId, req.getName(), req.getType(), req.getIcon());
        return Result.success(category);
    }

    @Operation(summary = "删除自定义分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = LoginUserContext.getUserId();
        categoryService.deleteCustom(id, userId);
        return Result.success();
    }
}

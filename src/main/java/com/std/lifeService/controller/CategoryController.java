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
import java.util.Map;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表（预设 + 自定义）")
    @PostMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listByUser(LoginUserContext.getUserId()));
    }

    @Operation(summary = "新增自定义分类")
    @PostMapping("/create")
    public Result<Category> add(@Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.addCustom(LoginUserContext.getUserId(), req.getName(), req.getType(), req.getIcon()));
    }

    @Operation(summary = "删除自定义分类")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        categoryService.deleteCustom(id, LoginUserContext.getUserId());
        return Result.success();
    }
}

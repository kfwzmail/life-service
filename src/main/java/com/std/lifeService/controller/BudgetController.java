package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.dto.BudgetRequest;
import com.std.lifeService.security.LoginUserContext;
import com.std.lifeService.service.BudgetService;
import com.std.lifeService.vo.BudgetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "预算管理")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "查询预算列表")
    @PostMapping("/list")
    public Result<List<BudgetVO>> list(@RequestBody Map<String, String> params) {
        return Result.success(budgetService.list(LoginUserContext.getUserId(), params.get("month")));
    }

    @Operation(summary = "保存预算")
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody BudgetRequest req) {
        budgetService.save(LoginUserContext.getUserId(), req.getBudgetMonth(), req.toVOList());
        return Result.success();
    }

    @Operation(summary = "预算执行对比")
    @PostMapping("/comparison")
    public Result<List<BudgetVO>> comparison(@RequestBody Map<String, String> params) {
        return Result.success(budgetService.comparison(LoginUserContext.getUserId(), params.get("month")));
    }
}
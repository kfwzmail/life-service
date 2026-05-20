package com.std.lifeService.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.std.lifeService.common.PageResult;
import com.std.lifeService.common.Result;
import com.std.lifeService.dto.BillRequest;
import com.std.lifeService.entity.Bill;
import com.std.lifeService.security.LoginUserContext;
import com.std.lifeService.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "账单管理")
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "新增账单")
    @PostMapping
    public Result<Bill> create(@Valid @RequestBody BillRequest req) {
        Bill bill = new Bill();
        bill.setCategoryId(req.getCategoryId());
        bill.setType(req.getType());
        bill.setAmount(req.getAmount());
        bill.setRemark(req.getRemark());
        bill.setBillTime(req.getBillTime());
        return Result.success(billService.create(LoginUserContext.getUserId(), bill));
    }

    @Operation(summary = "修改账单")
    @PutMapping("/{id}")
    public Result<Bill> update(@PathVariable Long id, @Valid @RequestBody BillRequest req) {
        Bill bill = new Bill();
        bill.setCategoryId(req.getCategoryId());
        bill.setType(req.getType());
        bill.setAmount(req.getAmount());
        bill.setRemark(req.getRemark());
        bill.setBillTime(req.getBillTime());
        return Result.success(billService.update(id, bill, LoginUserContext.getUserId()));
    }

    @Operation(summary = "删除账单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        billService.delete(id, LoginUserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "账单列表（分页 + 筛选）")
    @GetMapping
    public Result<PageResult<Bill>> page(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Bill> result = billService.page(LoginUserContext.getUserId(),
                categoryId, type, startDate, endDate, page, size);
        return Result.success(PageResult.of(result));
    }
}

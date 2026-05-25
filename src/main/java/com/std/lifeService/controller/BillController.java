package com.std.lifeService.controller;

import com.std.lifeService.common.PageResult;
import com.std.lifeService.common.Result;
import com.std.lifeService.dto.BillImportRequest;
import com.std.lifeService.dto.BillRequest;
import com.std.lifeService.entity.Bill;
import com.std.lifeService.security.LoginUserContext;
import com.std.lifeService.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "账单管理")
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "新增账单")
    @PostMapping("/create")
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
    @PostMapping("/update")
    public Result<Bill> update(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        BillRequest req = parseBillRequest(params);
        Bill bill = new Bill();
        bill.setCategoryId(req.getCategoryId());
        bill.setType(req.getType());
        bill.setAmount(req.getAmount());
        bill.setRemark(req.getRemark());
        bill.setBillTime(req.getBillTime());
        return Result.success(billService.update(id, bill, LoginUserContext.getUserId()));
    }

    @Operation(summary = "删除账单")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        billService.delete(id, LoginUserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "账单列表（分页 + 筛选）")
    @PostMapping("/list")
    public Result<PageResult<Bill>> page(@RequestBody Map<String, Object> params) {
        Long categoryId = params.get("categoryId") != null ? Long.valueOf(params.get("categoryId").toString()) : null;
        String type = (String) params.get("type");
        String startDateStr = (String) params.get("startDate");
        String endDateStr = (String) params.get("endDate");
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 20;

        java.time.LocalDateTime startDate = startDateStr != null ? java.time.LocalDateTime.parse(startDateStr) : null;
        java.time.LocalDateTime endDate = endDateStr != null ? java.time.LocalDateTime.parse(endDateStr) : null;

        var result = billService.page(LoginUserContext.getUserId(), categoryId, type, startDate, endDate, page, size);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "导入账单（微信/支付宝CSV）")
    @PostMapping("/import")
    public Result<Map<String, Object>> importBills(@Valid @RequestBody BillImportRequest req) {
        Map<String, Object> result = billService.importBills(LoginUserContext.getUserId(), req.getFormat(), req.getContent());
        return Result.success(result);
    }

    private BillRequest parseBillRequest(Map<String, Object> params) {
        BillRequest req = new BillRequest();
        req.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        req.setType((String) params.get("type"));
        req.setAmount(new java.math.BigDecimal(params.get("amount").toString()));
        req.setRemark((String) params.get("remark"));
        String billTimeStr = (String) params.get("billTime");
        req.setBillTime(java.time.LocalDateTime.parse(billTimeStr));
        return req;
    }
}

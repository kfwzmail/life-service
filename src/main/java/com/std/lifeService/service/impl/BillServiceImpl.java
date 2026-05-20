package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.entity.Bill;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;

    @Override
    public Bill create(Long userId, Bill bill) {
        bill.setUserId(userId);
        billMapper.insert(bill);
        return bill;
    }

    @Override
    public Bill update(Long billId, Bill updatedBill, Long userId) {
        Bill exist = billMapper.selectById(billId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能修改他人的账单");
        }
        updatedBill.setId(billId);
        updatedBill.setUserId(userId);
        billMapper.updateById(updatedBill);
        return billMapper.selectById(billId);
    }

    @Override
    public void delete(Long billId, Long userId) {
        Bill exist = billMapper.selectById(billId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能删除他人的账单");
        }
        billMapper.deleteById(billId);
    }

    @Override
    public Page<Bill> page(Long userId, Long categoryId, String type,
                           LocalDateTime startDate, LocalDateTime endDate,
                           int page, int size) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .eq(categoryId != null, Bill::getCategoryId, categoryId)
                .eq(type != null, Bill::getType, type)
                .ge(startDate != null, Bill::getBillTime, startDate)
                .le(endDate != null, Bill::getBillTime, endDate)
                .orderByDesc(Bill::getBillTime);
        return billMapper.selectPage(new Page<>(page, size), wrapper);
    }
}

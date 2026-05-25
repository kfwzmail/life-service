package com.std.lifeService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.std.lifeService.entity.Bill;
import java.time.LocalDateTime;
import java.util.Map;

public interface BillService {
    Bill create(Long userId, Bill bill);
    Bill update(Long billId, Bill updatedBill, Long userId);
    void delete(Long billId, Long userId);
    Page<Bill> page(Long userId, Long categoryId, String type,
                    LocalDateTime startDate, LocalDateTime endDate,
                    int page, int size);
    Map<String, Object> importBills(Long userId, String format, String content);
}

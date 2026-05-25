package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.constant.CategoryType;
import com.std.lifeService.dao.BillMapper;
import com.std.lifeService.dao.CategoryMapper;
import com.std.lifeService.entity.Bill;
import com.std.lifeService.entity.Category;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final CategoryMapper categoryMapper;

    private static final Map<String, String> WECHAT_CATEGORY_MAP = new LinkedHashMap<>();
    private static final Map<String, String> ALIPAY_CATEGORY_MAP = new LinkedHashMap<>();

    static {
        WECHAT_CATEGORY_MAP.put("餐饮", "餐饮");
        WECHAT_CATEGORY_MAP.put("交通出行", "交通");
        WECHAT_CATEGORY_MAP.put("购物", "购物");
        WECHAT_CATEGORY_MAP.put("生活服务", "其他");
        WECHAT_CATEGORY_MAP.put("休闲娱乐", "娱乐");
        WECHAT_CATEGORY_MAP.put("住房物业", "住房");

        ALIPAY_CATEGORY_MAP.put("餐饮美食", "餐饮");
        ALIPAY_CATEGORY_MAP.put("交通出行", "交通");
        ALIPAY_CATEGORY_MAP.put("日用百货", "购物");
        ALIPAY_CATEGORY_MAP.put("生活服务", "其他");
        ALIPAY_CATEGORY_MAP.put("文化休闲", "娱乐");
        ALIPAY_CATEGORY_MAP.put("住房缴费", "住房");
    }

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

    @Override
    public Map<String, Object> importBills(Long userId, String format, String content) {
        Map<String, String> categoryMap = "ALIPAY".equalsIgnoreCase(format)
                ? ALIPAY_CATEGORY_MAP : WECHAT_CATEGORY_MAP;

        List<Category> userCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDefault, 1)
                        .or()
                        .eq(Category::getUserId, userId)
        );
        Map<String, Long> catNameToId = new HashMap<>();
        for (Category c : userCategories) {
            catNameToId.putIfAbsent(c.getName(), c.getId());
        }
        Long defaultCategoryId = catNameToId.getOrDefault("其他", userCategories.get(0).getId());

        int success = 0;
        int fail = 0;
        String[] lines = content.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            try {
                Bill bill = parseLine(line, format, categoryMap, catNameToId, defaultCategoryId, userId);
                if (bill != null) {
                    billMapper.insert(bill);
                    success++;
                } else {
                    fail++;
                }
            } catch (Exception e) {
                log.warn("导入账单解析失败: line={}, error={}", i + 1, e.getMessage());
                fail++;
            }
        }
        log.info("账单导入完成: userId={}, format={}, success={}, fail={}", userId, format, success, fail);
        return Map.of("success", success, "fail", fail, "total", success + fail);
    }

    private Bill parseLine(String line, String format, Map<String, String> categoryMap,
                           Map<String, Long> catNameToId, Long defaultCategoryId, Long userId) {
        String[] cols = line.split(",");
        if (cols.length < 3) return null;

        String rawCategory;
        String amountStr;
        String dateStr;

        if ("ALIPAY".equalsIgnoreCase(format)) {
            if (cols.length < 11) return null;
            rawCategory = cols[4].trim();
            amountStr = cols[7].trim();
            dateStr = cols[2].trim();
        } else {
            rawCategory = cols[1].trim();
            amountStr = cols[4].trim();
            dateStr = cols[0].trim();
        }

        if (amountStr.isEmpty()) return null;

        amountStr = amountStr.replace("¥", "").replace("￥", "").replace("\"", "").strip();
        BigDecimal amount = new BigDecimal(amountStr).abs();

        String mappedName = null;
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            if (rawCategory.contains(entry.getKey())) {
                mappedName = entry.getValue();
                break;
            }
        }
        if (mappedName == null) mappedName = "其他";
        Long categoryId = catNameToId.getOrDefault(mappedName, defaultCategoryId);

        LocalDateTime billTime;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.CHINA);
            billTime = LocalDateTime.parse(dateStr.replace("\"", "").strip(), fmt);
        } catch (Exception e) {
            try {
                billTime = LocalDate.parse(dateStr.replace("\"", "").strip().substring(0, 10)).atStartOfDay();
            } catch (Exception ex) {
                billTime = LocalDateTime.now();
            }
        }

        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setCategoryId(categoryId);
        bill.setType(CategoryType.EXPENSE.name());
        bill.setAmount(amount);
        bill.setBillTime(billTime);
        bill.setRemark(cols[3].trim().replace("\"", ""));
        return bill;
    }
}

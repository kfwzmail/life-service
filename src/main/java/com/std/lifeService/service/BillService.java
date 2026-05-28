package com.std.lifeService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.std.lifeService.entity.Bill;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 账单服务接口
 */
public interface BillService {

    /**
     * 新增账单
     *
     * @param userId 用户ID
     * @param bill   账单实体
     * @return 创建后的账单
     */
    Bill create(Long userId, Bill bill);

    /**
     * 修改账单
     *
     * @param billId      账单ID
     * @param updatedBill 更新的账单内容
     * @param userId      用户ID（用于权限校验）
     * @return 更新后的账单
     */
    Bill update(Long billId, Bill updatedBill, Long userId);

    /**
     * 删除账单
     *
     * @param billId 账单ID
     * @param userId 用户ID（用于权限校验）
     */
    void delete(Long billId, Long userId);

    /**
     * 分页查询账单列表
     *
     * @param userId     用户ID
     * @param categoryId 分类ID，可为null
     * @param type       账单类型，可为null
     * @param startDate  开始时间，可为null
     * @param endDate    结束时间，可为null
     * @param page       页码
     * @param size       每页条数
     * @return 分页结果
     */
    Page<Bill> page(Long userId, Long categoryId, String type,
                    LocalDateTime startDate, LocalDateTime endDate,
                    int page, int size);

    /**
     * 导入账单（微信/支付宝CSV）
     *
     * @param userId  用户ID
     * @param format  导入格式（WECHAT/ALIPAY）
     * @param content CSV文件内容
     * @return 导入结果（success, fail, total）
     */
    Map<String, Object> importBills(Long userId, String format, String content);
}

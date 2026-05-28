package com.std.lifeService.service;

import com.std.lifeService.vo.BudgetVO;

import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService {

    /**
     * 查询用户某月预算列表
     *
     * @param userId      用户ID
     * @param budgetMonth 预算月份（yyyy-MM）
     * @return 预算VO列表
     */
    List<BudgetVO> list(Long userId, String budgetMonth);

    /**
     * 保存或更新用户某月预算
     *
     * @param userId      用户ID
     * @param budgetMonth 预算月份（yyyy-MM）
     * @param items       预算项列表
     */
    void save(Long userId, String budgetMonth, List<BudgetVO> items);

    /**
     * 预算执行对比（预算 vs 实际支出）
     *
     * @param userId      用户ID
     * @param budgetMonth 预算月份（yyyy-MM）
     * @return 带实际支出和使用率的预算VO列表
     */
    List<BudgetVO> comparison(Long userId, String budgetMonth);
}

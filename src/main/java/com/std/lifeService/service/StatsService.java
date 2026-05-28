package com.std.lifeService.service;

import com.std.lifeService.vo.DailyTrendVO;
import com.std.lifeService.vo.MonthStatsVO;
import com.std.lifeService.vo.TodayStatsVO;
import com.std.lifeService.vo.YearStatsVO;

import java.util.List;

/**
 * 统计分析服务接口
 */
public interface StatsService {

    /**
     * 查询今日收支统计
     *
     * @param userId 用户ID
     * @return 今日统计VO（支出、收入、结余）
     */
    TodayStatsVO today(Long userId);

    /**
     * 查询月度收支统计及分类占比
     *
     * @param userId    用户ID
     * @param yearMonth 月份（yyyy-MM），为null时取当月
     * @return 月度统计VO（支出、收入、结余、分类明细）
     */
    MonthStatsVO month(Long userId, String yearMonth);

    /**
     * 查询月度每日收支趋势
     *
     * @param userId    用户ID
     * @param yearMonth 月份（yyyy-MM），为null时取当月
     * @return 每日趋势列表
     */
    List<DailyTrendVO> dailyTrend(Long userId, String yearMonth);

    /**
     * 查询年度每月收支统计
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 年度每月统计列表
     */
    List<YearStatsVO> yearly(Long userId, int year);
}

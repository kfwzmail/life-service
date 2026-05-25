package com.std.lifeService.service;

import com.std.lifeService.vo.DailyTrendVO;
import com.std.lifeService.vo.MonthStatsVO;
import com.std.lifeService.vo.TodayStatsVO;
import com.std.lifeService.vo.YearStatsVO;
import java.util.List;

public interface StatsService {
    TodayStatsVO today(Long userId);
    MonthStatsVO month(Long userId, String yearMonth);
    List<DailyTrendVO> dailyTrend(Long userId, String yearMonth);
    List<YearStatsVO> yearly(Long userId, int year);
}

package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.security.LoginUserContext;
import com.std.lifeService.service.StatsService;
import com.std.lifeService.vo.DailyTrendVO;
import com.std.lifeService.vo.MonthStatsVO;
import com.std.lifeService.vo.TodayStatsVO;
import com.std.lifeService.vo.YearStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "今日统计")
    @PostMapping("/today")
    public Result<TodayStatsVO> today() {
        return Result.success(statsService.today(LoginUserContext.getUserId()));
    }

    @Operation(summary = "月度统计")
    @PostMapping("/month")
    public Result<MonthStatsVO> month(@RequestBody Map<String, String> params) {
        return Result.success(statsService.month(LoginUserContext.getUserId(), params.get("yearMonth")));
    }

    @Operation(summary = "每日趋势")
    @PostMapping("/daily-trend")
    public Result<List<DailyTrendVO>> dailyTrend(@RequestBody Map<String, String> params) {
        return Result.success(statsService.dailyTrend(LoginUserContext.getUserId(), params.get("yearMonth")));
    }

    @Operation(summary = "年度统计")
    @PostMapping("/yearly")
    public Result<List<YearStatsVO>> yearly(@RequestBody Map<String, Object> params) {
        int year = Integer.parseInt(params.get("year").toString());
        return Result.success(statsService.yearly(LoginUserContext.getUserId(), year));
    }
}

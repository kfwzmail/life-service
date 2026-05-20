package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.common.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查接口
 */
@Tag(name = "健康检查")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "time", LocalDateTime.now().toString(),
                "service", "life-service"
        ));
    }

    @Operation(summary = "测试异常")
    @GetMapping("/test-error")
    public Result<Void> testError(@RequestParam(defaultValue = "false") boolean error) {
        if (error) {
            throw new BusinessException(ResultCode.FAIL);
        }
        return Result.success();
    }
}

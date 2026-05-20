package com.std.lifeService.controller;

import com.std.lifeService.common.Result;
import com.std.lifeService.dto.LoginRequest;
import com.std.lifeService.dto.RegisterRequest;
import com.std.lifeService.entity.User;
import com.std.lifeService.security.JwtUtil;
import com.std.lifeService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.getPhone(), req.getPassword(), req.getNickname());
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return Result.success(Map.of("token", token, "user", user));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getPhone(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return Result.success(Map.of("token", token, "user", user));
    }
}

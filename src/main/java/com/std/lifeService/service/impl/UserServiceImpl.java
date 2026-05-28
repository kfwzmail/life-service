package com.std.lifeService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.std.lifeService.common.ResultCode;
import com.std.lifeService.dao.UserMapper;
import com.std.lifeService.entity.User;
import com.std.lifeService.exception.BusinessException;
import com.std.lifeService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(String phone, String password, String nickname) {
        // 检查手机号是否已被注册
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exist != null) {
            throw new BusinessException(400, "手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : phone);
        userMapper.insert(user);
        // 返回前清除密码，防止泄露
        user.setPassword(null);
        return user;
    }

    @Override
    public User login(String phone, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        // 不区分"用户不存在"和"密码错误"，防止撞库
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号或密码错误");
        }
        // 返回前清除密码，防止泄露
        user.setPassword(null);
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            // 返回前清除密码，防止泄露
            user.setPassword(null);
        }
        return user;
    }
}

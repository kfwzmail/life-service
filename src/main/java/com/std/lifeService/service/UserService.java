package com.std.lifeService.service;

import com.std.lifeService.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param phone    手机号
     * @param password 明文密码（服务端加密存储）
     * @param nickname 昵称，为null时默认使用手机号
     * @return 注册成功的用户（密码已脱敏）
     */
    User register(String phone, String password, String nickname);

    /**
     * 用户登录
     *
     * @param phone    手机号
     * @param password 明文密码
     * @return 登录成功的用户（密码已脱敏）
     */
    User login(String phone, String password);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体（密码已脱敏），不存在返回null
     */
    User findById(Long id);
}

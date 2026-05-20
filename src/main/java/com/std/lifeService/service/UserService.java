package com.std.lifeService.service;

import com.std.lifeService.entity.User;

public interface UserService {
    User register(String phone, String password, String nickname);
    User login(String phone, String password);
    User findById(Long id);
}

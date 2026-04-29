package com.elog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elog.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    User register(User user);
    
    /**
     * 用户登录，返回JWT Token
     */
    String login(String username, String password);
    
    /**
     * 根据用户名获取用户
     */
    User getByUsername(String username);
}
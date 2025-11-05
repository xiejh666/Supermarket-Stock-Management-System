package com.supermarket.service;

import com.supermarket.dto.LoginRequest;
import com.supermarket.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(String token);
}




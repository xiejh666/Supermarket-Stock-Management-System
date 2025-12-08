package com.supermarket.service;

/**
 * 登录限流服务接口
 */
public interface LoginLimitService {
    
    /**
     * 记录登录失败
     * @param username 用户名
     * @return 当前失败次数
     */
    int recordLoginFailure(String username);
    
    /**
     * 检查是否被锁定
     * @param username 用户名
     * @return 是否被锁定
     */
    boolean isLocked(String username);
    
    /**
     * 清除登录失败记录（登录成功时调用）
     * @param username 用户名
     */
    void clearLoginFailure(String username);
    
    /**
     * 获取剩余锁定时间（秒）
     * @param username 用户名
     * @return 剩余锁定时间，未锁定返回0
     */
    long getRemainingLockTime(String username);
}

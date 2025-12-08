package com.supermarket.service;

/**
 * Token黑名单服务接口
 */
public interface TokenBlacklistService {
    
    /**
     * 将Token加入黑名单
     * @param token JWT Token
     * @param expireSeconds Token剩余有效时间（秒）
     */
    void addToBlacklist(String token, long expireSeconds);
    
    /**
     * 检查Token是否在黑名单中
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String token);
    
    /**
     * 将用户的所有Token加入黑名单（踢人下线）
     * @param userId 用户ID
     */
    void blacklistUserTokens(Long userId);
}

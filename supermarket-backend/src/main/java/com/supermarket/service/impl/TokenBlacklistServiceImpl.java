package com.supermarket.service.impl;

import com.supermarket.service.TokenBlacklistService;
import com.supermarket.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务实现类
 */
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    @Autowired
    private RedisUtil redisUtil;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";

    @Override
    public void addToBlacklist(String token, long expireSeconds) {
        String key = BLACKLIST_PREFIX + token;
        // 将Token加入黑名单，过期时间与Token剩余有效时间一致
        redisUtil.set(key, "1", expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisUtil.hasKey(key);
    }

    @Override
    public void blacklistUserTokens(Long userId) {
        // 这里可以记录用户的Token，当用户被禁用或修改密码时，将所有Token加入黑名单
        // 简化实现：设置一个用户黑名单标记
        String key = USER_TOKEN_PREFIX + userId + ":blacklisted";
        // 设置24小时过期，确保旧Token都失效
        redisUtil.set(key, "1", 24, TimeUnit.HOURS);
    }
}

package com.supermarket.service.impl;

import com.supermarket.service.LoginLimitService;
import com.supermarket.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录限流服务实现类
 */
@Service
public class LoginLimitServiceImpl implements LoginLimitService {

    @Autowired
    private RedisUtil redisUtil;

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_FAIL_COUNT = 5; // 最大失败次数
    private static final int LOCK_TIME_MINUTES = 30; // 锁定时间（分钟）

    @Override
    public int recordLoginFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        Long count = redisUtil.increment(key, 1);
        
        // 第一次失败时设置过期时间
        if (count == 1) {
            redisUtil.expire(key, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
        
        return count.intValue();
    }

    @Override
    public boolean isLocked(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        Object value = redisUtil.get(key);
        
        if (value == null) {
            return false;
        }
        
        int failCount = Integer.parseInt(value.toString());
        return failCount >= MAX_FAIL_COUNT;
    }

    @Override
    public void clearLoginFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        redisUtil.delete(key);
    }

    @Override
    public long getRemainingLockTime(String username) {
        if (!isLocked(username)) {
            return 0;
        }
        
        String key = LOGIN_FAIL_PREFIX + username;
        Long expire = redisUtil.getExpire(key);
        return expire != null ? expire : 0;
    }
}

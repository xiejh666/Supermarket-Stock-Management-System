package com.supermarket.service.impl;

import com.supermarket.service.CacheService;
import com.supermarket.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务实现类
 */
@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> dataLoader, long expireMinutes) {
        // 先从缓存获取
        Object cached = redisUtil.get(key);
        if (cached != null) {
            return (T) cached;
        }

        // 缓存不存在，从数据库加载
        T data = dataLoader.get();
        if (data != null) {
            // 存入缓存
            redisUtil.set(key, data, expireMinutes, TimeUnit.MINUTES);
        }

        return data;
    }

    @Override
    public void evict(String key) {
        redisUtil.delete(key);
    }

    @Override
    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisUtil.delete(keys);
        }
    }

    @Override
    public <T> T refresh(String key, Supplier<T> dataLoader, long expireMinutes) {
        // 删除旧缓存
        evict(key);
        // 重新加载
        return getOrLoad(key, dataLoader, expireMinutes);
    }
}

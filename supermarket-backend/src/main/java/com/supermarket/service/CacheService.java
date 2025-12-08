package com.supermarket.service;

import java.util.function.Supplier;

/**
 * 缓存服务接口
 */
public interface CacheService {
    
    /**
     * 获取缓存数据，如果不存在则从数据库加载并缓存
     * @param key 缓存键
     * @param dataLoader 数据加载器（从数据库查询）
     * @param expireMinutes 过期时间（分钟）
     * @param <T> 数据类型
     * @return 数据
     */
    <T> T getOrLoad(String key, Supplier<T> dataLoader, long expireMinutes);
    
    /**
     * 删除缓存
     * @param key 缓存键
     */
    void evict(String key);
    
    /**
     * 批量删除缓存（支持通配符）
     * @param pattern 缓存键模式，如 "product:*"
     */
    void evictByPattern(String pattern);
    
    /**
     * 刷新缓存（删除后重新加载）
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param expireMinutes 过期时间（分钟）
     * @param <T> 数据类型
     * @return 新数据
     */
    <T> T refresh(String key, Supplier<T> dataLoader, long expireMinutes);
}

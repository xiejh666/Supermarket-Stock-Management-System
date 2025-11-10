package com.supermarket.service;

import com.supermarket.dto.SystemSettingsDTO;

/**
 * 系统设置服务接口
 */
public interface SystemSettingsService {
    
    /**
     * 获取系统设置（包含用户个性化配置）
     */
    SystemSettingsDTO getSettings(Long userId);
    
    /**
     * 保存系统设置
     */
    void saveSettings(Long userId, SystemSettingsDTO settings);
    
    /**
     * 获取系统配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 获取用户配置值
     */
    String getUserConfigValue(Long userId, String configKey, String defaultValue);
}



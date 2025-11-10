package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.dto.SystemSettingsDTO;
import com.supermarket.entity.SystemConfig;
import com.supermarket.entity.UserConfig;
import com.supermarket.mapper.SystemConfigMapper;
import com.supermarket.mapper.UserConfigMapper;
import com.supermarket.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统设置服务实现
 */
@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {
    
    private final SystemConfigMapper systemConfigMapper;
    private final UserConfigMapper userConfigMapper;
    
    @Override
    public SystemSettingsDTO getSettings(Long userId) {
        SystemSettingsDTO dto = new SystemSettingsDTO();
        
        // 基本设置（系统级配置）
        dto.setSystemName(getConfigValue("system.name"));
        dto.setSystemDescription(getConfigValue("system.description"));
        
        // 通知设置（用户级配置）
        dto.setInventoryWarning(Boolean.parseBoolean(getUserConfigValue(userId, "notification.inventory_warning", "true")));
        dto.setOrderAudit(Boolean.parseBoolean(getUserConfigValue(userId, "notification.order_audit", "true")));
        dto.setSystemNotice(Boolean.parseBoolean(getUserConfigValue(userId, "notification.system_notice", "true")));
        
        // 安全设置（系统级配置）
        dto.setPasswordExpireDays(Integer.parseInt(getConfigValue("security.password_expire_days")));
        dto.setLoginFailTimes(Integer.parseInt(getConfigValue("security.login_fail_times")));
        dto.setSessionTimeout(Integer.parseInt(getConfigValue("security.session_timeout")));
        dto.setStrongPassword(Boolean.parseBoolean(getConfigValue("security.strong_password")));
        
        return dto;
    }
    
    @Override
    @Transactional
    public void saveSettings(Long userId, SystemSettingsDTO settings) {
        // 保存基本设置（系统级配置）
        saveOrUpdateSystemConfig("system.name", settings.getSystemName());
        saveOrUpdateSystemConfig("system.description", settings.getSystemDescription());
        
        // 保存通知设置（用户级配置）
        saveOrUpdateUserConfig(userId, "notification.inventory_warning", String.valueOf(settings.getInventoryWarning()));
        saveOrUpdateUserConfig(userId, "notification.order_audit", String.valueOf(settings.getOrderAudit()));
        saveOrUpdateUserConfig(userId, "notification.system_notice", String.valueOf(settings.getSystemNotice()));
        
        // 保存安全设置（系统级配置）
        saveOrUpdateSystemConfig("security.password_expire_days", String.valueOf(settings.getPasswordExpireDays()));
        saveOrUpdateSystemConfig("security.login_fail_times", String.valueOf(settings.getLoginFailTimes()));
        saveOrUpdateSystemConfig("security.session_timeout", String.valueOf(settings.getSessionTimeout()));
        saveOrUpdateSystemConfig("security.strong_password", String.valueOf(settings.getStrongPassword()));
    }
    
    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public String getUserConfigValue(Long userId, String configKey, String defaultValue) {
        LambdaQueryWrapper<UserConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConfig::getUserId, userId)
               .eq(UserConfig::getConfigKey, configKey);
        UserConfig config = userConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : defaultValue;
    }
    
    /**
     * 保存或更新系统配置
     */
    private void saveOrUpdateSystemConfig(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        
        if (config != null) {
            config.setConfigValue(configValue);
            systemConfigMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            systemConfigMapper.insert(config);
        }
    }
    
    /**
     * 保存或更新用户配置
     */
    private void saveOrUpdateUserConfig(Long userId, String configKey, String configValue) {
        LambdaQueryWrapper<UserConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConfig::getUserId, userId)
               .eq(UserConfig::getConfigKey, configKey);
        UserConfig config = userConfigMapper.selectOne(wrapper);
        
        if (config != null) {
            config.setConfigValue(configValue);
            userConfigMapper.updateById(config);
        } else {
            config = new UserConfig();
            config.setUserId(userId);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            userConfigMapper.insert(config);
        }
    }
}



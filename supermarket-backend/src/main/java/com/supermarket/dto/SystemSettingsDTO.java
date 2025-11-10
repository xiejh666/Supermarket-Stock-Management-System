package com.supermarket.dto;

import lombok.Data;

/**
 * 系统设置DTO
 */
@Data
public class SystemSettingsDTO {
    
    // 基本设置
    private String systemName;
    private String systemDescription;
    
    // 通知设置
    private Boolean inventoryWarning;
    private Boolean orderAudit;
    private Boolean systemNotice;
    
    // 安全设置
    private Integer passwordExpireDays;
    private Integer loginFailTimes;
    private Integer sessionTimeout;
    private Boolean strongPassword;
}



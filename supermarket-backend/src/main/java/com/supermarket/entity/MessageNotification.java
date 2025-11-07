package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("message_notification")
public class MessageNotification {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收用户ID（NULL表示所有人可见）
     */
    private Long userId;
    
    /**
     * 接收角色编码（NULL表示所有角色可见）
     */
    private String roleCode;
    
    /**
     * 消息类型：success/warning/info/error
     */
    private String type;
    
    /**
     * 消息分类：system/purchase/sale/inventory/user
     */
    private String category;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 跳转类型：purchase/sale/inventory/product/supplier
     */
    private String linkType;
    
    /**
     * 关联业务ID
     */
    private Long linkId;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


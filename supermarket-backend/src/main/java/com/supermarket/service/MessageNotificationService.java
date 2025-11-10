package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.MessageNotification;
import com.supermarket.vo.MessageNotificationVO;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface MessageNotificationService extends IService<MessageNotification> {
    
    /**
     * 获取当前用户的消息列表（基于角色）
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @param onlyUnread 是否只查询未读消息
     * @return 消息列表
     */
    List<MessageNotificationVO> getUserMessages(Long userId, String roleCode, Boolean onlyUnread);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 未读消息数
     */
    Integer getUnreadCount(Long userId, String roleCode);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean markAsRead(Long messageId, Long userId);
    
    /**
     * 标记所有消息为已读
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否成功
     */
    Boolean markAllAsRead(Long userId, String roleCode);
    
    /**
     * 删除消息（软删除）
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteMessage(Long messageId, Long userId);
    
    /**
     * 清空所有消息
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否成功
     */
    Boolean clearAllMessages(Long userId, String roleCode);
}



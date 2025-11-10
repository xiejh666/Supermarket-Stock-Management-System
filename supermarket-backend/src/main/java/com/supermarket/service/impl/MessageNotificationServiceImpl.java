package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.MessageNotification;
import com.supermarket.mapper.MessageNotificationMapper;
import com.supermarket.service.MessageNotificationService;
import com.supermarket.vo.MessageNotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class MessageNotificationServiceImpl extends ServiceImpl<MessageNotificationMapper, MessageNotification> 
        implements MessageNotificationService {
    
    private final MessageNotificationMapper messageMapper;
    
    @Override
    public List<MessageNotificationVO> getUserMessages(Long userId, String roleCode, Boolean onlyUnread) {
        LambdaQueryWrapper<MessageNotification> wrapper = new LambdaQueryWrapper<>();
        
        // 查询条件：(user_id = userId OR user_id IS NULL) AND (role_code = roleCode OR role_code IS NULL)
        wrapper.and(w -> w.eq(MessageNotification::getUserId, userId).or().isNull(MessageNotification::getUserId));
        wrapper.and(w -> w.eq(MessageNotification::getRoleCode, roleCode).or().isNull(MessageNotification::getRoleCode));
        
        // 如果只查询未读消息
        if (onlyUnread != null && onlyUnread) {
            wrapper.eq(MessageNotification::getIsRead, false);
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(MessageNotification::getCreateTime);
        
        List<MessageNotification> messages = messageMapper.selectList(wrapper);
        
        // 转换为VO
        return messages.stream().map(message -> {
            MessageNotificationVO vo = new MessageNotificationVO();
            BeanUtils.copyProperties(message, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Integer getUnreadCount(Long userId, String roleCode) {
        LambdaQueryWrapper<MessageNotification> wrapper = new LambdaQueryWrapper<>();
        
        // 查询条件同上
        wrapper.and(w -> w.eq(MessageNotification::getUserId, userId).or().isNull(MessageNotification::getUserId));
        wrapper.and(w -> w.eq(MessageNotification::getRoleCode, roleCode).or().isNull(MessageNotification::getRoleCode));
        wrapper.eq(MessageNotification::getIsRead, false);
        
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }
    
    @Override
    @Transactional
    public Boolean markAsRead(Long messageId, Long userId) {
        MessageNotification message = messageMapper.selectById(messageId);
        if (message == null) {
            return false;
        }
        
        // 检查权限：消息必须是该用户的或者是公共消息
        if (message.getUserId() != null && !message.getUserId().equals(userId)) {
            return false;
        }
        
        message.setIsRead(true);
        message.setReadTime(LocalDateTime.now());
        return messageMapper.updateById(message) > 0;
    }
    
    @Override
    @Transactional
    public Boolean markAllAsRead(Long userId, String roleCode) {
        return messageMapper.markAllAsRead(userId, roleCode) >= 0;
    }
    
    @Override
    @Transactional
    public Boolean deleteMessage(Long messageId, Long userId) {
        MessageNotification message = messageMapper.selectById(messageId);
        if (message == null) {
            return false;
        }
        
        // 检查权限
        if (message.getUserId() != null && !message.getUserId().equals(userId)) {
            return false;
        }
        
        return messageMapper.deleteById(messageId) > 0;
    }
    
    @Override
    @Transactional
    public Boolean clearAllMessages(Long userId, String roleCode) {
        LambdaQueryWrapper<MessageNotification> wrapper = new LambdaQueryWrapper<>();
        
        // 只删除属于该用户或该角色的消息
        wrapper.and(w -> w.eq(MessageNotification::getUserId, userId).or().eq(MessageNotification::getRoleCode, roleCode));
        
        return messageMapper.delete(wrapper) >= 0;
    }
}



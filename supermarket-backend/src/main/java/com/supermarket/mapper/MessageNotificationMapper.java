package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.MessageNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface MessageNotificationMapper extends BaseMapper<MessageNotification> {
    
    /**
     * 批量标记为已读
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 影响行数
     */
    @Update("UPDATE message_notification SET is_read = 1, read_time = NOW() " +
            "WHERE is_read = 0 AND (user_id = #{userId} OR user_id IS NULL) " +
            "AND (role_code = #{roleCode} OR role_code IS NULL)")
    int markAllAsRead(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}


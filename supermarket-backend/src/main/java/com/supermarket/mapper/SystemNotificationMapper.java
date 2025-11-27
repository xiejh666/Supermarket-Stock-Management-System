package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.SystemNotification;
import com.supermarket.vo.SystemNotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统通知Mapper
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 查询用户通知列表
     */
    List<SystemNotificationVO> selectUserNotifications(
            @Param("userId") Long userId,
            @Param("isRead") Integer isRead,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 统计用户通知总数
     */
    Long countUserNotifications(
            @Param("userId") Long userId,
            @Param("isRead") Integer isRead
    );

    /**
     * 获取最新动态
     */
    List<SystemNotificationVO> selectRecentActivities(
            @Param("userId") Long userId,
            @Param("limit") Integer limit
    );
}

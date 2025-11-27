package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.service.SystemNotificationService;
import com.supermarket.vo.SystemNotificationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统通知控制器
 */
@RestController
@RequestMapping("/notifications")
@Api(tags = "系统通知管理")
@RequiredArgsConstructor
public class SystemNotificationController {

    private final SystemNotificationService notificationService;

    @GetMapping("/list")
    @ApiOperation("获取用户通知列表")
    public Result<Page<SystemNotificationVO>> getNotifications(
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("是否已读") @RequestParam(required = false) Integer isRead) {
        Page<SystemNotificationVO> page = notificationService.getUserNotifications(userId, current, size, isRead);
        return Result.success(page);
    }

    @GetMapping("/unread-count")
    @ApiOperation("获取未读通知数量")
    public Result<Long> getUnreadCount(@ApiParam("用户ID") @RequestParam Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @ApiOperation("标记通知为已读")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @ApiParam("用户ID") @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    @ApiOperation("标记所有通知为已读")
    public Result<Void> markAllAsRead(@ApiParam("用户ID") @RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @GetMapping("/recent-activities")
    @ApiOperation("获取最新动态")
    public Result<List<SystemNotificationVO>> getRecentActivities(
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        List<SystemNotificationVO> activities = notificationService.getRecentActivities(userId, limit);
        return Result.success(activities);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<Void> deleteNotification(
            @PathVariable Long id,
            @ApiParam("用户ID") @RequestParam Long userId) {
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }
}

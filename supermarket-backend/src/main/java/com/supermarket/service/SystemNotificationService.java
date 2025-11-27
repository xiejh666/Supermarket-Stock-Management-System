package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.SystemNotification;
import com.supermarket.vo.SystemNotificationVO;

import java.util.List;

/**
 * 系统通知服务接口
 */
public interface SystemNotificationService extends IService<SystemNotification> {

    /**
     * 发送采购审核通知
     */
    void sendPurchaseAuditNotification(Long purchaseOrderId, Long applicantId);

    /**
     * 发送销售支付通知
     */
    void sendSalePaymentNotification(Long saleOrderId, Long cashierId);

    /**
     * 发送采购审核结果通知
     * @param purchaseOrderId 采购订单ID
     * @param applicantId 申请人ID
     * @param auditorId 审核人ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     */
    void sendPurchaseAuditResultNotification(Long purchaseOrderId, Long applicantId, Long auditorId, Integer auditStatus);

    /**
     * 获取用户通知列表
     */
    Page<SystemNotificationVO> getUserNotifications(Long userId, Integer current, Integer size, Integer isRead);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取最新动态（最近的通知）
     */
    List<SystemNotificationVO> getRecentActivities(Long userId, Integer limit);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);
}

package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.SaleOrder;
import com.supermarket.entity.SysUser;
import com.supermarket.entity.SystemNotification;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.PurchaseOrderMapper;
import com.supermarket.mapper.SaleOrderMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.mapper.SystemNotificationMapper;
import com.supermarket.service.SystemNotificationService;
import com.supermarket.vo.SystemNotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements SystemNotificationService {

    private final SystemNotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPurchaseAuditNotification(Long purchaseOrderId, Long applicantId) {
        log.info("开始发送采购审核通知，订单ID: {}, 申请人ID: {}", purchaseOrderId, applicantId);
        
        // 获取采购订单信息
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(purchaseOrderId);
        if (purchaseOrder == null) {
            log.error("采购订单不存在，订单ID: {}", purchaseOrderId);
            throw new BusinessException("采购订单不存在");
        }
        log.info("采购订单信息: 订单号={}, 总金额={}", purchaseOrder.getOrderNo(), purchaseOrder.getTotalAmount());

        // 获取申请人信息
        SysUser applicant = userMapper.selectById(applicantId);
        String applicantName = applicant != null ? applicant.getRealName() : "未知用户";
        log.info("申请人信息: ID={}, 姓名={}", applicantId, applicantName);

        // 获取所有管理员用户（roleId = 1 表示管理员）
        List<SysUser> adminUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, 1L)
                .eq(SysUser::getStatus, 1) // 只获取启用的用户
        );
        
        log.info("找到 {} 个管理员用户", adminUsers.size());
        
        // 为每个管理员创建单独的通知
        for (SysUser admin : adminUsers) {
            SystemNotification notification = new SystemNotification();
            notification.setTitle("新的采购订单待审核");
            notification.setContent(String.format("采购员 %s 提交了采购订单 %s，总金额 ¥%.2f，请及时审核。", 
                    applicantName, purchaseOrder.getOrderNo(), purchaseOrder.getTotalAmount()));
            notification.setType("PURCHASE_AUDIT");
            notification.setReceiverId(admin.getId()); // 发给特定管理员
            notification.setSenderId(applicantId);
            notification.setBusinessId(purchaseOrderId);
            notification.setBusinessType("PURCHASE_ORDER");
            notification.setRelatedCode(purchaseOrder.getOrderNo()); // 设置订单号
            notification.setIsRead(0);
            notification.setPriority(3); // 高优先级

            boolean saved = this.save(notification);
            log.info("管理员通知保存结果: {}, 接收人: {}, 通知ID: {}", 
                    saved ? "成功" : "失败", admin.getRealName(), notification.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPurchaseAuditResultNotification(Long purchaseOrderId, Long applicantId, Long auditorId, Integer auditStatus) {
        log.info("开始发送采购审核结果通知，订单ID: {}, 申请人ID: {}, 审核人ID: {}, 审核状态: {}", 
                purchaseOrderId, applicantId, auditorId, auditStatus);
        
        // 获取采购订单信息
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(purchaseOrderId);
        if (purchaseOrder == null) {
            log.error("采购订单不存在，订单ID: {}", purchaseOrderId);
            throw new BusinessException("采购订单不存在");
        }

        // 获取审核人信息
        SysUser auditor = userMapper.selectById(auditorId);
        String auditorName = auditor != null ? auditor.getRealName() : "未知管理员";
        
        // 根据审核状态生成不同的通知内容
        String title;
        String content;
        String notificationType;
        
        if (auditStatus == 1) {
            // 审核通过
            title = "采购订单审核通过";
            content = String.format("您提交的采购订单 %s（总金额 ¥%.2f）已被管理员 %s 审核通过，可以进行入库操作。", 
                    purchaseOrder.getOrderNo(), purchaseOrder.getTotalAmount(), auditorName);
            notificationType = "PURCHASE_APPROVED";
        } else {
            // 审核拒绝
            title = "采购订单审核拒绝";
            content = String.format("您提交的采购订单 %s（总金额 ¥%.2f）已被管理员 %s 审核拒绝。", 
                    purchaseOrder.getOrderNo(), purchaseOrder.getTotalAmount(), auditorName);
            notificationType = "PURCHASE_REJECTED";
        }

        // 创建通知
        SystemNotification notification = new SystemNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(notificationType);
        notification.setReceiverId(applicantId); // 发给申请人
        notification.setSenderId(auditorId);
        notification.setBusinessId(purchaseOrderId);
        notification.setBusinessType("PURCHASE_ORDER");
        notification.setRelatedCode(purchaseOrder.getOrderNo()); // 设置订单号
        notification.setIsRead(0);
        notification.setPriority(auditStatus == 1 ? 2 : 3); // 通过为中等优先级，拒绝为高优先级

        boolean saved = this.save(notification);
        log.info("审核结果通知保存结果: {}, 通知ID: {}", saved ? "成功" : "失败", notification.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSalePaymentNotification(Long saleOrderId, Long cashierId) {
        // 获取销售订单信息
        SaleOrder saleOrder = saleOrderMapper.selectById(saleOrderId);
        if (saleOrder == null) {
            throw new BusinessException("销售订单不存在");
        }

        // 获取收银员信息
        SysUser cashier = userMapper.selectById(cashierId);
        String cashierName = cashier != null ? cashier.getRealName() : "未知用户";

        // 获取所有管理员用户（roleId = 1 表示管理员）
        List<SysUser> adminUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, 1L)
                .eq(SysUser::getStatus, 1) // 只获取启用的用户
        );
        
        // 为每个管理员创建单独的通知
        for (SysUser admin : adminUsers) {
            SystemNotification notification = new SystemNotification();
            notification.setTitle("销售订单支付完成");
            notification.setContent(String.format("销售员 %s 完成了销售订单 %s 的支付，金额 ¥%.2f。", 
                    cashierName, saleOrder.getOrderNo(), saleOrder.getTotalAmount()));
            notification.setType("SALE_PAYMENT");
            notification.setReceiverId(admin.getId()); // 发给特定管理员
            notification.setSenderId(cashierId);
            notification.setBusinessId(saleOrderId);
            notification.setBusinessType("SALE_ORDER");
            notification.setRelatedCode(saleOrder.getOrderNo()); // 设置订单号
            notification.setIsRead(0);
            notification.setPriority(2); // 中优先级

            this.save(notification);
        }
    }

    @Override
    public Page<SystemNotificationVO> getUserNotifications(Long userId, Integer current, Integer size, Integer isRead) {
        // 计算偏移量
        int offset = (current - 1) * size;
        
        // 查询通知列表
        List<SystemNotificationVO> notifications = notificationMapper.selectUserNotifications(userId, isRead, offset, size);
        
        // 统计总数
        Long total = notificationMapper.countUserNotifications(userId, isRead);
        
        // 构建分页结果
        Page<SystemNotificationVO> page = new Page<>(current, size);
        page.setRecords(notifications);
        page.setTotal(total);
        
        return page;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUserNotifications(userId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<SystemNotification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemNotification::getId, notificationId)
                    .and(wrapper -> wrapper.eq(SystemNotification::getReceiverId, userId)
                                          .or()
                                          .isNull(SystemNotification::getReceiverId))
                    .set(SystemNotification::getIsRead, 1);
        
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<SystemNotification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.and(wrapper -> wrapper.eq(SystemNotification::getReceiverId, userId)
                                          .or()
                                          .isNull(SystemNotification::getReceiverId))
                    .eq(SystemNotification::getIsRead, 0)
                    .set(SystemNotification::getIsRead, 1);
        
        this.update(updateWrapper);
    }

    @Override
    public List<SystemNotificationVO> getRecentActivities(Long userId, Integer limit) {
        return notificationMapper.selectRecentActivities(userId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getId, notificationId)
                   .and(wrapper -> wrapper.eq(SystemNotification::getReceiverId, userId)
                                         .or()
                                         .isNull(SystemNotification::getReceiverId));
        
        this.remove(queryWrapper);
    }
}

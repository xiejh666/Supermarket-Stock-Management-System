package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.SysUser;
import com.supermarket.entity.SystemNotification;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务操作通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessOperationNotificationServiceImpl implements BusinessOperationNotificationService {

    private final SysUserMapper userMapper;
    private final SystemNotificationService notificationService;

    @Override
    public void notifySupplierOperation(Long operatorId, String operatorRole, String operation, String supplierName, Long supplierId) {
        log.info("供应商操作通知：操作人ID={}, 角色={}, 操作={}, 供应商={}", operatorId, operatorRole, operation, supplierName);
        
        String operatorName = getUserName(operatorId);
        String title = getSupplierOperationTitle(operation);
        String content = String.format("%s %s %s了供应商：%s", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), supplierName);
        
        notifyByRole(operatorRole, operatorId, title, content, "SUPPLIER_OPERATION", supplierId, "SUPPLIER");
    }

    @Override
    public void notifyCustomerOperation(Long operatorId, String operatorRole, String operation, String customerName, Long customerId) {
        log.info("客户操作通知：操作人ID={}, 角色={}, 操作={}, 客户={}", operatorId, operatorRole, operation, customerName);
        
        String operatorName = getUserName(operatorId);
        String title = getCustomerOperationTitle(operation);
        String content = String.format("%s %s %s了客户：%s", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), customerName);
        
        notifyByRole(operatorRole, operatorId, title, content, "CUSTOMER_OPERATION", customerId, "CUSTOMER");
    }

    @Override
    public void notifyProductOperation(Long operatorId, String operatorRole, String operation, String productName, String productCode, Long productId) {
        log.info("商品操作通知：操作人ID={}, 角色={}, 操作={}, 商品名称={}, 商品编码={}", operatorId, operatorRole, operation, productName, productCode);
        
        String operatorName = getUserName(operatorId);
        String title = getProductOperationTitle(operation);
        String content = String.format("%s %s %s了商品：%s（编码：%s）", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), productName, productCode);
        
        notifyByRole(operatorRole, operatorId, title, content, "PRODUCT_OPERATION", productId, "PRODUCT", productCode);
    }

    @Override
    public void notifyCategoryOperation(Long operatorId, String operatorRole, String operation, String categoryName, Long categoryId) {
        log.info("分类操作通知：操作人ID={}, 角色={}, 操作={}, 分类名称={}", operatorId, operatorRole, operation, categoryName);
        
        String operatorName = getUserName(operatorId);
        String title = getCategoryOperationTitle(operation);
        String content = String.format("%s %s %s了分类：%s", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), categoryName);
        
        notifyByRole(operatorRole, operatorId, title, content, "CATEGORY_OPERATION", categoryId, "CATEGORY");
    }

    @Override
    public void notifyInventoryOperation(Long operatorId, String operatorRole, String operation, String productName, Long productId, Integer quantity) {
        log.info("库存操作通知：操作人ID={}, 角色={}, 操作={}, 商品名称={}, 数量={}", operatorId, operatorRole, operation, productName, quantity);
        
        String operatorName = getUserName(operatorId);
        String title = getInventoryOperationTitle(operation);
        String content = String.format("%s %s %s了商品：%s，数量：%d", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), productName, quantity);
        
        notifyByRole(operatorRole, operatorId, title, content, "INVENTORY_OPERATION", productId, "INVENTORY");
    }

    @Override
    public void notifyPurchaseOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount) {
        notifyPurchaseOperation(operatorId, operatorRole, operation, orderNo, orderId, amount, null);
    }

    @Override
    public void notifyPurchaseOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount, Integer auditResult) {
        log.info("采购操作通知：操作人ID={}, 角色={}, 操作={}, 订单号={}, 金额={}, 审核结果={}", 
                operatorId, operatorRole, operation, orderNo, amount, auditResult);
        
        String operatorName = getUserName(operatorId);
        String title = getPurchaseOperationTitle(operation, auditResult);
        String content = getPurchaseOperationContent(operatorRole, operatorName, operation, orderNo, amount, auditResult);
        
        notifyByRole(operatorRole, operatorId, title, content, "PURCHASE_OPERATION", orderId, "PURCHASE_ORDER", orderNo);
    }

    @Override
    public void notifySaleOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount) {
        log.info("销售操作通知：操作人ID={}, 角色={}, 操作={}, 订单号={}, 金额={}", operatorId, operatorRole, operation, orderNo, amount);
        
        String operatorName = getUserName(operatorId);
        String title = getSaleOperationTitle(operation);
        String content = String.format("%s %s %s了销售订单：%s，金额：¥%.2f", 
                getRoleDisplayName(operatorRole), operatorName, getOperationDisplayName(operation), orderNo, amount);
        
        notifyByRole(operatorRole, operatorId, title, content, "SALE_OPERATION", orderId, "SALE_ORDER", orderNo);
    }

    /**
     * 根据角色分发通知的通用方法
     */
    private void notifyByRole(String operatorRole, Long operatorId, String title, String content, String type, Long businessId, String businessType, String relatedCode) {
        if ("ADMIN".equals(operatorRole)) {
            // 管理员操作 → 通知采购员和营业员
            log.info("管理员操作，通知采购员和营业员");
            notifyUsersByRole(2L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知采购员
            notifyUsersByRole(3L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知营业员
        } else if ("PURCHASER".equals(operatorRole)) {
            // 采购员操作 → 通知管理员和营业员
            log.info("采购员操作，通知管理员和营业员");
            notifyUsersByRole(1L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知管理员
            notifyUsersByRole(3L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知营业员
        } else if ("CASHIER".equals(operatorRole)) {
            // 营业员操作 → 通知管理员和采购员
            log.info("营业员操作，通知管理员和采购员");
            notifyUsersByRole(1L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知管理员
            notifyUsersByRole(2L, operatorId, title, content, type, businessId, businessType, relatedCode); // 通知采购员
        }
    }

    // 兼容旧方法（不传relatedCode）
    private void notifyByRole(String operatorRole, Long operatorId, String title, String content, String type, Long businessId, String businessType) {
        notifyByRole(operatorRole, operatorId, title, content, type, businessId, businessType, null);
    }

    /**
     * 通知指定角色的所有用户
     */
    private void notifyUsersByRole(Long roleId, Long senderId, String title, String content, String type, Long businessId, String businessType, String relatedCode) {
        log.info("开始查询角色ID为 {} 的用户", roleId);
        
        List<SysUser> users = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, roleId)
                .eq(SysUser::getStatus, 1)
        );
        
        log.info("找到 {} 个角色ID为 {} 的用户", users.size(), roleId);
        
        if (users.isEmpty()) {
            log.warn("没有找到角色ID为 {} 的启用用户！", roleId);
            return;
        }
        
        // 打印找到的用户信息
        for (SysUser user : users) {
            log.info("找到用户：ID={}, 姓名={}, 角色ID={}, 状态={}", 
                    user.getId(), user.getRealName(), user.getRoleId(), user.getStatus());
        }
        
        for (SysUser user : users) {
            SystemNotification notification = new SystemNotification();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setReceiverId(user.getId());
            notification.setSenderId(senderId);
            notification.setBusinessId(businessId);
            notification.setBusinessType(businessType);
            notification.setRelatedCode(relatedCode);
            notification.setIsRead(0);
            notification.setPriority(2); // 中等优先级
            notification.setCreateTime(java.time.LocalDateTime.now());
            notification.setUpdateTime(java.time.LocalDateTime.now());
            
            boolean saved = notificationService.save(notification);
            log.info("业务操作通知发送结果：接收人={}, 保存成功={}, 通知ID={}", 
                    user.getRealName(), saved, notification.getId());
        }
    }

    /**
     * 获取角色显示名称
     */
    private String getRoleDisplayName(String roleCode) {
        switch (roleCode) {
            case "ADMIN": return "管理员";
            case "PURCHASER": return "采购员";
            case "CASHIER": return "营业员";
            default: return "用户";
        }
    }

    /**
     * 获取操作显示名称
     */
    private String getOperationDisplayName(String operation) {
        switch (operation) {
            case "CREATE": return "新增";
            case "UPDATE": return "编辑";
            case "DELETE": return "删除";
            case "INBOUND": return "入库";
            case "OUTBOUND": return "出库";
            case "AUDIT": return "审核";
            case "CONFIRM": return "确认";
            case "PAY": return "支付";
            default: return operation;
        }
    }

    /**
     * 获取供应商操作标题
     */
    private String getSupplierOperationTitle(String operation) {
        switch (operation) {
            case "CREATE": return "新增供应商";
            case "UPDATE": return "编辑供应商";
            case "DELETE": return "删除供应商";
            default: return "供应商操作";
        }
    }

    /**
     * 获取客户操作标题
     */
    private String getCustomerOperationTitle(String operation) {
        switch (operation) {
            case "CREATE": return "新增客户";
            case "UPDATE": return "编辑客户";
            case "DELETE": return "删除客户";
            default: return "客户操作";
        }
    }

    /**
     * 获取商品操作标题
     */
    private String getProductOperationTitle(String operation) {
        switch (operation) {
            case "CREATE": return "新增商品";
            case "UPDATE": return "编辑商品";
            case "DELETE": return "删除商品";
            default: return "商品操作";
        }
    }

    /**
     * 获取分类操作标题
     */
    private String getCategoryOperationTitle(String operation) {
        switch (operation) {
            case "CREATE": return "新增分类";
            case "UPDATE": return "编辑分类";
            case "DELETE": return "删除分类";
            default: return "分类操作";
        }
    }

    /**
     * 获取库存操作标题
     */
    private String getInventoryOperationTitle(String operation) {
        switch (operation) {
            case "INBOUND": return "库存入库";
            case "OUTBOUND": return "库存出库";
            default: return "库存操作";
        }
    }

    /**
     * 获取采购操作标题
     */
    private String getPurchaseOperationTitle(String operation, Integer auditResult) {
        if ("AUDIT".equals(operation)) {
            return auditResult != null && auditResult == 1 ? "采购订单审核通过" : "采购订单审核拒绝";
        }
        switch (operation) {
            case "CREATE": return "新增采购订单";
            case "UPDATE": return "编辑采购订单";
            case "DELETE": return "删除采购订单";
            case "CONFIRM": return "确认采购订单";
            default: return "采购订单操作";
        }
    }

    /**
     * 获取销售操作标题
     */
    private String getSaleOperationTitle(String operation) {
        switch (operation) {
            case "CREATE": return "新增销售订单";
            case "UPDATE": return "编辑销售订单";
            case "DELETE": return "删除销售订单";
            case "PAY": return "销售订单支付";
            default: return "销售订单操作";
        }
    }

    /**
     * 获取采购操作内容
     */
    private String getPurchaseOperationContent(String operatorRole, String operatorName, String operation, String orderNo, Double amount, Integer auditResult) {
        String roleDisplayName = getRoleDisplayName(operatorRole);
        
        if ("AUDIT".equals(operation) && auditResult != null) {
            String result = auditResult == 1 ? "通过" : "拒绝";
            return String.format("%s %s 审核了采购订单：%s（金额：¥%.2f），审核结果：%s", 
                    roleDisplayName, operatorName, orderNo, amount, result);
        }
        
        String operationName = getOperationDisplayName(operation);
        return String.format("%s %s %s了采购订单：%s，金额：¥%.2f", 
                roleDisplayName, operatorName, operationName, orderNo, amount);
    }

    /**
     * 获取用户姓名
     */
    private String getUserName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : "未知用户";
    }
}

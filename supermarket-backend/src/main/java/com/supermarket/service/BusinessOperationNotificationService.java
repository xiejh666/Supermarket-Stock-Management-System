package com.supermarket.service;

/**
 * 业务操作通知服务接口
 * 负责处理各种业务操作的通知分发
 */
public interface BusinessOperationNotificationService {

    /**
     * 供应商管理操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE
     * @param supplierName 供应商名称
     * @param supplierId 供应商ID
     */
    void notifySupplierOperation(Long operatorId, String operatorRole, String operation, String supplierName, Long supplierId);

    /**
     * 客户管理操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE
     * @param customerName 客户名称
     * @param customerId 客户ID
     */
    void notifyCustomerOperation(Long operatorId, String operatorRole, String operation, String customerName, Long customerId);

    /**
     * 商品操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE
     * @param productName 商品名称
     * @param productCode 商品编码
     * @param productId 商品ID
     */
    void notifyProductOperation(Long operatorId, String operatorRole, String operation, String productName, String productCode, Long productId);

    /**
     * 分类操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE
     * @param categoryName 分类名称
     * @param categoryId 分类ID
     */
    void notifyCategoryOperation(Long operatorId, String operatorRole, String operation, String categoryName, Long categoryId);

    /**
     * 库存操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE
     * @param productName 商品名称
     * @param productId 商品ID
     * @param quantity 库存数量
     */
    void notifyInventoryOperation(Long operatorId, String operatorRole, String operation, String productName, Long productId, Integer quantity);

    /**
     * 采购管理操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE, AUDIT, CONFIRM
     * @param orderNo 订单号
     * @param orderId 订单ID
     * @param amount 金额
     */
    void notifyPurchaseOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount);

    /**
     * 采购管理操作通知（带审核结果）
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE, AUDIT, CONFIRM
     * @param orderNo 订单号
     * @param orderId 订单ID
     * @param amount 金额
     * @param auditResult 审核结果：1-通过，2-拒绝
     */
    void notifyPurchaseOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount, Integer auditResult);

    /**
     * 销售管理操作通知
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @param operation 操作类型：CREATE, UPDATE, DELETE, PAY, CONFIRM
     * @param orderNo 订单号
     * @param orderId 订单ID
     * @param amount 金额
     */
    void notifySaleOperation(Long operatorId, String operatorRole, String operation, String orderNo, Long orderId, Double amount);
}

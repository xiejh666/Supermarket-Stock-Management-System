package com.supermarket.vo;

import lombok.Data;

/**
 * 最新动态VO
 */
@Data
public class ActivityVO {
    /**
     * 动态ID
     */
    private String id;
    
    /**
     * 动态类型：purchase-采购, sale-销售, inventory-库存
     */
    private String type;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容描述
     */
    private String content;
    
    /**
     * 状态标签
     */
    private String badge;
    
    /**
     * 状态值（用于判断）
     */
    private Integer status;
    
    /**
     * 业务ID（用于跳转）
     */
    private Long businessId;
    
    /**
     * 订单号（采购订单号或销售订单号）
     */
    private String orderNo;
    
    /**
     * 商品名称（库存相关）
     */
    private String productName;
    
    /**
     * 创建时间
     */
    private String time;
    
    /**
     * 图标类型
     */
    private String icon;
}

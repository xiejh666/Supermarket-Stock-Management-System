package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单实体
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long supplierId;

    private BigDecimal totalAmount;

    /**
     * 订单状态：0-待审核，1-待入库（审核通过），2-已拒绝，3-已入库
     */
    private Integer status;

    private Long applicantId;

    private Long auditorId;

    private LocalDateTime auditTime;

    private String auditRemark;
    
    @TableField("inbound_time")
    private LocalDateTime inboundTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}




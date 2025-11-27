package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@TableName("system_notification")
public class SystemNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型：PURCHASE_AUDIT-采购审核，SALE_PAYMENT-销售支付，SYSTEM-系统通知
     */
    private String type;

    /**
     * 接收用户ID（为空表示发给所有管理员）
     */
    private Long receiverId;

    /**
     * 发送用户ID
     */
    private Long senderId;

    /**
     * 关联业务ID（如采购订单ID、销售订单ID）
     */
    private Long businessId;

    /**
     * 业务类型：PURCHASE_ORDER-采购订单，SALE_ORDER-销售订单
     */
    private String businessType;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 优先级：1-低，2-中，3-高
     */
    private Integer priority;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

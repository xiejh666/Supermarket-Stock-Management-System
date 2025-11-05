package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存变动日志实体
 */
@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    /**
     * 变动类型：1-入库，2-出库，3-盘点调整
     */
    private Integer changeType;

    /**
     * 变动数量（正数表示增加，负数表示减少）
     */
    private Integer changeQuantity;

    private Integer beforeQuantity;

    private Integer afterQuantity;

    private String orderNo;

    private String remark;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}




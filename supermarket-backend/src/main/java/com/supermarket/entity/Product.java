package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String productCode;
    
    private String productName;
    
    private Long categoryId;
    
    private Long supplierId;
    
    private String unit;
    
    private String specification;
    
    @TableField("price")
    private BigDecimal price;
    
    // 零售价（与price字段映射，为了前端兼容性）
    @TableField(exist = false)
    private BigDecimal retailPrice;
    
    @TableField("cost_price")
    private BigDecimal costPrice;
    
    @TableField("image")
    private String image;
    
    @TableField(exist = false)
    private String imageUrl;
    
    // 库存数量（从inventory表获取）
    @TableField(exist = false)
    private Integer stock;
    
    // 预警库存（从inventory表获取）
    @TableField(exist = false)
    private Integer warningStock;
    
    private String description;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}



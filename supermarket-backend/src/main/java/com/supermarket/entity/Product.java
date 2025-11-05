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
    
    @TableField("price")
    private BigDecimal price;
    
    @TableField("cost_price")
    private BigDecimal costPrice;
    
    @TableField("image")
    private String image;
    
    private String description;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}



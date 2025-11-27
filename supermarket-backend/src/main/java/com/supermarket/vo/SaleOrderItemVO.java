package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售订单明细VO
 */
@Data
@ApiModel("销售订单明细视图对象")
public class SaleOrderItemVO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品编码")
    private String productCode;

    @ApiModelProperty("数量")
    private Integer quantity;

    @ApiModelProperty("单价")
    private BigDecimal unitPrice;

    @ApiModelProperty("总价")
    private BigDecimal totalPrice;
}

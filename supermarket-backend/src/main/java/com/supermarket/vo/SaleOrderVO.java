package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单VO
 */
@Data
@ApiModel("销售订单视图对象")
public class SaleOrderVO {

    @ApiModelProperty("销售订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户手机号")
    private String customerPhone;

    @ApiModelProperty("客户地址")
    private String customerAddress;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("订单状态：0-待支付，1-已支付，2-已取消")
    private Integer status;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("收银员ID")
    private Long cashierId;
    
    @ApiModelProperty("收银员名称")
    private String cashierName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("订单明细列表")
    private List<SaleOrderItemVO> items;
}

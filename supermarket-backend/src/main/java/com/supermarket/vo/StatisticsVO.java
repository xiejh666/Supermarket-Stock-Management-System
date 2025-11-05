package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("统计数据视图对象")
public class StatisticsVO {

    @ApiModelProperty("今日销售额")
    private BigDecimal todaySales;

    @ApiModelProperty("本月销售额")
    private BigDecimal monthSales;

    @ApiModelProperty("今日订单数")
    private Integer todayOrders;

    @ApiModelProperty("本月订单数")
    private Integer monthOrders;

    @ApiModelProperty("商品总数")
    private Integer totalProducts;

    @ApiModelProperty("低库存商品数")
    private Integer lowStockProducts;

    @ApiModelProperty("待审核采购订单数")
    private Integer pendingPurchaseOrders;

    @ApiModelProperty("供应商总数")
    private Integer totalSuppliers;
}




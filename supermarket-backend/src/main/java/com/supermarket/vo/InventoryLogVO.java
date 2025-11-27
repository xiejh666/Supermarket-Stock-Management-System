package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存变动日志VO
 */
@Data
@ApiModel("库存变动日志视图对象")
public class InventoryLogVO {

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("变动类型：1-入库，2-出库，3-盘点调整")
    private Integer changeType;

    @ApiModelProperty("变动类型名称")
    private String changeTypeName;

    @ApiModelProperty("变动数量")
    private Integer changeQuantity;

    @ApiModelProperty("变动前数量")
    private Integer beforeQuantity;

    @ApiModelProperty("变动后数量")
    private Integer afterQuantity;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}

package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 采购订单明细DTO
 */
@Data
@ApiModel("采购订单明细数据传输对象")
public class PurchaseOrderItemDTO {

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品不能为空")
    private Long productId;

    @ApiModelProperty(value = "数量", required = true)
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @ApiModelProperty(value = "采购单价", required = true)
    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0.01", message = "采购单价必须大于0")
    private BigDecimal purchasePrice;
    
    // 内部映射字段，用于兼容
    @ApiModelProperty(value = "单价（内部使用）")
    private BigDecimal unitPrice;
}




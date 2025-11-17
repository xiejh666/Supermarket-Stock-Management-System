package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 采购订单DTO
 */
@Data
@ApiModel("采购订单数据传输对象")
public class PurchaseOrderDTO {

    @ApiModelProperty("采购订单ID（更新时必填）")
    private Long id;

    @ApiModelProperty(value = "供应商ID", required = true)
    @NotNull(message = "供应商不能为空")
    private Long supplierId;
    
    @ApiModelProperty(value = "采购日期")
    private String purchaseDate;

    @ApiModelProperty(value = "采购明细列表", required = true)
    @NotEmpty(message = "采购明细不能为空")
    @Valid
    private List<PurchaseOrderItemDTO> items;

    @ApiModelProperty("备注")
    private String remark;
}




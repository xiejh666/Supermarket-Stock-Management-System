package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 销售订单DTO
 */
@Data
@ApiModel("销售订单数据传输对象")
public class SaleOrderDTO {

    @ApiModelProperty("销售订单ID（更新时必填）")
    private Long id;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty(value = "销售明细列表", required = true)
    @NotEmpty(message = "销售明细不能为空")
    @Valid
    private List<SaleOrderItemDTO> items;
}




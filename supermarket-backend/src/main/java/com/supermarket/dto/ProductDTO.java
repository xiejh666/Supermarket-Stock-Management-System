package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品DTO
 */
@Data
@ApiModel("商品数据传输对象")
public class ProductDTO {

    @ApiModelProperty("商品ID（更新时必填）")
    private Long id;

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @ApiModelProperty("商品编码")
    private String productCode;

    @ApiModelProperty(value = "分类ID", required = true)
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty(value = "零售价", required = true)
    @NotNull(message = "零售价不能为空")
    @DecimalMin(value = "0.00", message = "零售价必须大于等于0")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价", required = true)
    @NotNull(message = "成本价不能为空")
    @DecimalMin(value = "0.00", message = "成本价必须大于等于0")
    private BigDecimal costPrice;

    @ApiModelProperty("商品图片URL")
    private String image;

    @ApiModelProperty("商品描述")
    private String description;

    @ApiModelProperty("状态：0-下架，1-上架")
    private Integer status = 1;
}



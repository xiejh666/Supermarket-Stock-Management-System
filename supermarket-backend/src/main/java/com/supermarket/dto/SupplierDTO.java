package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 供应商DTO
 */
@Data
@ApiModel("供应商数据传输对象")
public class SupplierDTO {

    @ApiModelProperty("供应商ID（更新时必填）")
    private Long id;

    @ApiModelProperty(value = "供应商名称", required = true)
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status = 1;
}



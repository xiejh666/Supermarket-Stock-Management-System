package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 商品分类DTO
 */
@Data
@ApiModel("商品分类数据传输对象")
public class CategoryDTO {

    @ApiModelProperty("分类ID（更新时必填）")
    private Long id;

    @ApiModelProperty(value = "分类名称", required = true)
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @ApiModelProperty("父分类ID")
    private Long parentId = 0L;

    @ApiModelProperty("排序")
    private Integer sortOrder = 0;

    @ApiModelProperty("分类描述")
    private String description;
}



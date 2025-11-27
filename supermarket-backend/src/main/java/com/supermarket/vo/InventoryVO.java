package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存VO
 */
@Data
@ApiModel("库存视图对象")
public class InventoryVO {

    @ApiModelProperty("库存ID")
    private Long id;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品编码")
    private String productCode;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("库存数量")
    private Integer quantity;
    
    @ApiModelProperty("当前库存（与quantity相同，前端兼容字段）")
    private Integer stock;

    @ApiModelProperty("预警数量")
    private Integer warningQuantity;
    
    @ApiModelProperty("最小库存（与warningQuantity相同，前端兼容字段）")
    private Integer minStock;

    @ApiModelProperty("是否预警")
    private Boolean isWarning;
    
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}




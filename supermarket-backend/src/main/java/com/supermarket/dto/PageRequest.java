package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分页请求基类
 */
@Data
@ApiModel("分页请求")
public class PageRequest {

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer current = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer size = 10;

    @ApiModelProperty("排序字段")
    private String sortField;

    @ApiModelProperty("排序方式：asc/desc")
    private String sortOrder;

    public Integer getCurrent() {
        return current == null || current < 1 ? 1 : current;
    }

    public Integer getSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return size > 100 ? 100 : size; // 最大100条
    }
}




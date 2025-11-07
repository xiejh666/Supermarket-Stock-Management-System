package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知VO
 */
@Data
@ApiModel("消息通知VO")
public class MessageNotificationVO {
    
    @ApiModelProperty("消息ID")
    private Long id;
    
    @ApiModelProperty("消息类型：success/warning/info/error")
    private String type;
    
    @ApiModelProperty("消息分类：system/purchase/sale/inventory/user")
    private String category;
    
    @ApiModelProperty("消息标题")
    private String title;
    
    @ApiModelProperty("消息内容")
    private String content;
    
    @ApiModelProperty("跳转类型：purchase/sale/inventory/product/supplier")
    private String linkType;
    
    @ApiModelProperty("关联业务ID")
    private Long linkId;
    
    @ApiModelProperty("是否已读")
    private Boolean isRead;
    
    @ApiModelProperty("阅读时间")
    private LocalDateTime readTime;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}


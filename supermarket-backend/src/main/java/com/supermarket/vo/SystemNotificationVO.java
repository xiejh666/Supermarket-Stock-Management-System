package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统通知VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("系统通知")
public class SystemNotificationVO {

    @ApiModelProperty("通知ID")
    private Long id;

    @ApiModelProperty("通知标题")
    private String title;

    @ApiModelProperty("通知内容")
    private String content;

    @ApiModelProperty("通知类型")
    private String type;

    @ApiModelProperty("通知类型描述")
    private String typeDesc;

    @ApiModelProperty("接收用户ID")
    private Long receiverId;

    @ApiModelProperty("发送用户ID")
    private Long senderId;

    @ApiModelProperty("发送用户名")
    private String senderName;

    @ApiModelProperty("关联业务ID")
    private Long businessId;

    @ApiModelProperty("业务类型")
    private String businessType;

    @ApiModelProperty("业务类型描述")
    private String businessTypeDesc;

    @ApiModelProperty("是否已读")
    private Integer isRead;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("优先级描述")
    private String priorityDesc;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("相对时间描述")
    private String timeDesc;
}

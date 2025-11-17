package com.supermarket.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单VO
 */
@Data
@ApiModel("采购订单视图对象")
public class PurchaseOrderVO {

    @ApiModelProperty("采购订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("供应商ID")
    private Long supplierId;

    @ApiModelProperty("供应商名称")
    private String supplierName;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("订单状态：0-待审核，1-已通过，2-已拒绝，3-已入库")
    private Integer status;

    @ApiModelProperty("状态描述")
    private String statusDesc;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @ApiModelProperty("审核人ID")
    private Long auditorId;

    @ApiModelProperty("审核人姓名")
    private String auditorName;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @ApiModelProperty("审核备注")
    private String auditRemark;

    @ApiModelProperty("入库时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inboundTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("采购明细列表")
    private List<PurchaseOrderItemVO> items;
}




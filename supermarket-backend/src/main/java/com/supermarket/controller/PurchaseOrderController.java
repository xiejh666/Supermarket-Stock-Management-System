package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.PurchaseOrderDTO;
import com.supermarket.service.PurchaseOrderService;
import com.supermarket.vo.PurchaseOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 采购管理控制器
 */
@RestController
@RequestMapping("/purchase/orders")
@Api(tags = "采购管理")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping("/list")
    @ApiOperation("分页查询采购订单列表")
    public Result<Page<PurchaseOrderVO>> getPurchaseOrderList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("订单编号") @RequestParam(required = false) String orderNo,
            @ApiParam("供应商ID") @RequestParam(required = false) String supplierId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("申请人ID") @RequestParam(required = false) String applicantId) {
        // 处理supplierId，如果是"all"或为空，则传null
        Long supplierIdLong = null;
        if (supplierId != null && !"all".equalsIgnoreCase(supplierId) && !supplierId.trim().isEmpty()) {
            try {
                supplierIdLong = Long.parseLong(supplierId);
            } catch (NumberFormatException e) {
                // 忽略无效的supplierId
            }
        }
        
        // 处理applicantId，如果是"all"或为空，则传null
        Long applicantIdLong = null;
        if (applicantId != null && !"all".equalsIgnoreCase(applicantId) && !applicantId.trim().isEmpty()) {
            try {
                applicantIdLong = Long.parseLong(applicantId);
            } catch (NumberFormatException e) {
                // 忽略无效的applicantId
            }
        }
        
        Page<PurchaseOrderVO> page = purchaseOrderService.getPurchaseOrderList(
                current, size, orderNo, supplierIdLong, status, applicantIdLong);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询采购订单详情")
    public Result<PurchaseOrderVO> getPurchaseOrderDetail(@PathVariable Long id) {
        PurchaseOrderVO vo = purchaseOrderService.getPurchaseOrderDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    @ApiOperation("创建采购订单")
    public Result<Void> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderDTO dto,
            @ApiParam("申请人ID") @RequestParam Long applicantId) {
        purchaseOrderService.createPurchaseOrder(dto, applicantId);
        return Result.success();
    }

    @PutMapping("/{id}/audit")
    @ApiOperation("审核采购订单")
    public Result<Void> auditPurchaseOrder(
            @PathVariable Long id,
            @ApiParam("审核状态：1-通过，2-拒绝") @RequestParam Integer status,
            @ApiParam("审核备注") @RequestParam(required = false) String auditRemark,
            @ApiParam("审核人ID") @RequestParam Long auditorId) {
        purchaseOrderService.auditPurchaseOrder(id, status, auditRemark, auditorId);
        return Result.success();
    }

    @PutMapping("/{id}/inbound")
    @ApiOperation("确认入库")
    public Result<Void> confirmInbound(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        purchaseOrderService.confirmInbound(id, operatorId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除采购订单")
    public Result<Void> deletePurchaseOrder(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        purchaseOrderService.deletePurchaseOrder(id, operatorId);
        return Result.success();
    }
}



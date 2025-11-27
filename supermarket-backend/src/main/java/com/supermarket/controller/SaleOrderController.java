package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.SaleOrderDTO;
import com.supermarket.vo.SaleOrderVO;
import com.supermarket.service.SaleOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 销售管理控制器
 */
@RestController
@RequestMapping("/sale/orders")
@Api(tags = "销售管理")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    @GetMapping("/list")
    @ApiOperation("分页查询销售订单列表")
    public Result<Page<SaleOrderVO>> getSaleOrderList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("订单编号") @RequestParam(required = false) String orderNo,
            @ApiParam("客户名称") @RequestParam(required = false) String customerName,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate) {
        Page<SaleOrderVO> page = saleOrderService.getSaleOrderList(current, size, orderNo, customerName, status, startDate, endDate);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询销售订单详情")
    public Result<SaleOrderVO> getSaleOrderDetail(@PathVariable Long id) {
        SaleOrderVO order = saleOrderService.getSaleOrderDetail(id);
        return Result.success(order);
    }

    @PostMapping
    @ApiOperation("创建销售订单")
    public Result<Void> createSaleOrder(
            @Valid @RequestBody SaleOrderDTO dto,
            @ApiParam("收银员ID") @RequestParam Long cashierId,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        saleOrderService.createSaleOrder(dto, cashierId, operatorId);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @ApiOperation("支付订单")
    public Result<Void> paySaleOrder(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        saleOrderService.paySaleOrder(id, operatorId);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancelSaleOrder(
            @PathVariable Long id,
            @ApiParam("取消原因") @RequestParam String reason) {
        saleOrderService.cancelSaleOrder(id, reason);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Result<Void> deleteSaleOrder(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        saleOrderService.deleteSaleOrder(id, operatorId);
        return Result.success();
    }
}



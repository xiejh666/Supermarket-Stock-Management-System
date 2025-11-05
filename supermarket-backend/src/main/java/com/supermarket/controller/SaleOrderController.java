package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.SaleOrderDTO;
import com.supermarket.entity.SaleOrder;
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
    public Result<Page<SaleOrder>> getSaleOrderList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("订单编号") @RequestParam(required = false) String orderNo,
            @ApiParam("客户ID") @RequestParam(required = false) Long customerId,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<SaleOrder> page = saleOrderService.getSaleOrderList(current, size, orderNo, customerId, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询销售订单详情")
    public Result<SaleOrder> getSaleOrderDetail(@PathVariable Long id) {
        SaleOrder order = saleOrderService.getSaleOrderDetail(id);
        return Result.success(order);
    }

    @PostMapping
    @ApiOperation("创建销售订单")
    public Result<Void> createSaleOrder(
            @Valid @RequestBody SaleOrderDTO dto,
            @ApiParam("收银员ID") @RequestParam Long cashierId) {
        saleOrderService.createSaleOrder(dto, cashierId);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @ApiOperation("支付订单")
    public Result<Void> paySaleOrder(@PathVariable Long id) {
        saleOrderService.paySaleOrder(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancelSaleOrder(@PathVariable Long id) {
        saleOrderService.cancelSaleOrder(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Result<Void> deleteSaleOrder(@PathVariable Long id) {
        saleOrderService.deleteSaleOrder(id);
        return Result.success();
    }
}



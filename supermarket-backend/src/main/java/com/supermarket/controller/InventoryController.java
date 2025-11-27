package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.service.InventoryService;
import com.supermarket.vo.InventoryLogVO;
import com.supermarket.vo.InventoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理控制器
 */
@RestController
@RequestMapping("/inventory")
@Api(tags = "库存管理")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    @ApiOperation("分页查询库存列表")
    public Result<Page<InventoryVO>> getInventoryList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("商品名称") @RequestParam(required = false) String productName,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("是否预警") @RequestParam(required = false) Boolean isWarning) {
        Page<InventoryVO> page = inventoryService.getInventoryList(current, size, productName, categoryId, isWarning);
        return Result.success(page);
    }

    @PostMapping("/inbound")
    @ApiOperation("入库")
    public Result<Void> inbound(
            @ApiParam("商品ID") @RequestParam Long productId,
            @ApiParam("入库数量") @RequestParam Integer quantity,
            @ApiParam("备注") @RequestParam(required = false) String remark,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        inventoryService.inbound(productId, quantity, null, remark, operatorId);
        return Result.success();
    }

    @PostMapping("/outbound")
    @ApiOperation("出库")
    public Result<Void> outbound(
            @ApiParam("商品ID") @RequestParam Long productId,
            @ApiParam("出库数量") @RequestParam Integer quantity,
            @ApiParam("备注") @RequestParam(required = false) String remark,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        inventoryService.outbound(productId, quantity, null, remark, operatorId);
        return Result.success();
    }

    @PutMapping("/adjust")
    @ApiOperation("盘点调整")
    public Result<Void> adjust(
            @ApiParam("商品ID") @RequestParam Long productId,
            @ApiParam("新库存数量") @RequestParam Integer newQuantity,
            @ApiParam("备注") @RequestParam(required = false) String remark,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        inventoryService.adjust(productId, newQuantity, remark, operatorId);
        return Result.success();
    }

    @GetMapping("/logs/{productId}")
    @ApiOperation("查询商品库存变动历史")
    public Result<List<InventoryLogVO>> getInventoryLogs(
            @PathVariable Long productId) {
        List<InventoryLogVO> logs = inventoryService.getInventoryLogs(productId);
        return Result.success(logs);
    }
}



package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.SupplierDTO;
import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 供应商管理控制器
 */
@RestController
@RequestMapping("/suppliers")
@Api(tags = "供应商管理")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/list")
    @ApiOperation("分页查询供应商列表")
    public Result<Page<Supplier>> getSupplierList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("供应商名称") @RequestParam(required = false) String supplierName,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<Supplier> page = supplierService.getSupplierList(current, size, supplierName, status);
        return Result.success(page);
    }

    @GetMapping("/enabled")
    @ApiOperation("查询所有启用的供应商")
    public Result<List<Supplier>> getEnabledSuppliers() {
        List<Supplier> list = supplierService.getEnabledSuppliers();
        return Result.success(list);
    }

    @GetMapping("/all")
    @ApiOperation("查询所有供应商（不分页）")
    public Result<List<Supplier>> getAllSuppliers() {
        List<Supplier> list = supplierService.list();
        return Result.success(list);
    }

    @PostMapping
    @ApiOperation("创建供应商")
    public Result<Void> createSupplier(@Valid @RequestBody SupplierDTO dto) {
        supplierService.createSupplier(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("更新供应商")
    public Result<Void> updateSupplier(@Valid @RequestBody SupplierDTO dto) {
        supplierService.updateSupplier(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除供应商")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return Result.success();
    }
}



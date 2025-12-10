package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.entity.Customer;
import com.supermarket.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/customers")
@Api(tags = "客户管理")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/all")
    @ApiOperation("获取所有客户列表")
    public Result<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return Result.success(customers);
    }

    @GetMapping("/list")
    @ApiOperation("分页查询客户列表")
    public Result<Page<Customer>> getCustomerList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("客户名称") @RequestParam(required = false) String customerName,
            @ApiParam("手机号") @RequestParam(required = false) String phone,
            @ApiParam("地址") @RequestParam(required = false) String address) {
        Page<Customer> page = customerService.getCustomerList(current, size, customerName, phone, address);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询客户详情")
    public Result<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return Result.success(customer);
    }

    @PostMapping
    @ApiOperation("新增客户")
    public Result<Void> createCustomer(
            @Valid @RequestBody Customer customer,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        customerService.createCustomer(customer, operatorId);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("更新客户")
    public Result<Void> updateCustomer(
            @Valid @RequestBody Customer customer,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        customerService.updateCustomer(customer, operatorId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除客户")
    public Result<Void> deleteCustomer(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        customerService.deleteCustomer(id, operatorId);
        return Result.success();
    }
}

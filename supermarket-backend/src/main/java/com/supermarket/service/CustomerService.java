package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Customer;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService extends IService<Customer> {
    
    /**
     * 获取所有客户列表
     */
    List<Customer> getAllCustomers();
    
    /**
     * 分页查询客户列表
     */
    Page<Customer> getCustomerList(Integer current, Integer size, String customerName, String phone, String address);
    
    /**
     * 新增客户
     */
    void createCustomer(Customer customer, Long operatorId);
    
    /**
     * 更新客户
     */
    void updateCustomer(Customer customer, Long operatorId);
    
    /**
     * 删除客户
     */
    void deleteCustomer(Long id, Long operatorId);
}

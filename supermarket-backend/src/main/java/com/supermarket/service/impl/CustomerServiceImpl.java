package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Customer;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.CustomerMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CustomerMapper customerMapper;
    private final BusinessOperationNotificationService businessNotificationService;
    private final SysUserMapper userMapper;

    @Override
    public List<Customer> getAllCustomers() {
        return list();
    }

    @Override
    public Page<Customer> getCustomerList(Integer current, Integer size, String customerName, String phone) {
        Page<Customer> page = new Page<>(current, size);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(customerName)) {
            wrapper.like(Customer::getCustomerName, customerName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Customer::getPhone, phone);
        }
        
        wrapper.orderByDesc(Customer::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCustomer(Customer customer, Long operatorId) {
        // 检查客户名称是否存在
        Long count = lambdaQuery()
                .eq(Customer::getCustomerName, customer.getCustomerName())
                .count();
        if (count > 0) {
            throw new BusinessException("客户名称已存在");
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(customer.getPhone())) {
            Long phoneCount = lambdaQuery()
                    .eq(Customer::getPhone, customer.getPhone())
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException("手机号已存在");
            }
        }

        customerMapper.insert(customer);
        
        // 发送客户创建通知
        log.info("=== 开始发送客户创建通知 ===");
        log.info("客户ID: {}, 操作人ID: {}, 客户名称: {}", customer.getId(), operatorId, customer.getCustomerName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCustomerOperation(
                operatorId, operatorRole, "CREATE", customer.getCustomerName(), customer.getId()
            );
            log.info("=== 客户创建通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送客户创建通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomer(Customer customer, Long operatorId) {
        if (customer.getId() == null) {
            throw new BusinessException("客户ID不能为空");
        }

        Customer existingCustomer = customerMapper.selectById(customer.getId());
        if (existingCustomer == null) {
            throw new BusinessException("客户不存在");
        }

        // 检查客户名称是否被其他客户占用
        Long count = lambdaQuery()
                .eq(Customer::getCustomerName, customer.getCustomerName())
                .ne(Customer::getId, customer.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("客户名称已存在");
        }

        // 检查手机号是否被其他客户占用
        if (StringUtils.hasText(customer.getPhone())) {
            Long phoneCount = lambdaQuery()
                    .eq(Customer::getPhone, customer.getPhone())
                    .ne(Customer::getId, customer.getId())
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException("手机号已存在");
            }
        }

        customerMapper.updateById(customer);
        
        // 发送客户编辑通知
        log.info("=== 开始发送客户编辑通知 ===");
        log.info("客户ID: {}, 操作人ID: {}, 客户名称: {}", customer.getId(), operatorId, customer.getCustomerName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCustomerOperation(
                operatorId, operatorRole, "UPDATE", customer.getCustomerName(), customer.getId()
            );
            log.info("=== 客户编辑通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送客户编辑通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(Long id, Long operatorId) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 检查是否有关联的销售订单
        // TODO: 添加业务逻辑检查

        customerMapper.deleteById(id);
        
        // 发送客户删除通知
        log.info("=== 开始发送客户删除通知 ===");
        log.info("客户ID: {}, 操作人ID: {}, 客户名称: {}", id, operatorId, customer.getCustomerName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCustomerOperation(
                operatorId, operatorRole, "DELETE", customer.getCustomerName(), customer.getId()
            );
            log.info("=== 客户删除通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送客户删除通知失败 ===", e);
        }
    }

    /**
     * 获取用户角色编码
     */
    private String getUserRole(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            return "UNKNOWN";
        }
        
        Long roleId = user.getRoleId();
        switch (roleId.intValue()) {
            case 1: return "ADMIN";
            case 2: return "PURCHASER";
            case 3: return "CASHIER";
            default: return "UNKNOWN";
        }
    }
}

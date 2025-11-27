package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.SupplierDTO;
import com.supermarket.entity.Supplier;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private final SupplierMapper supplierMapper;
    private final BusinessOperationNotificationService businessNotificationService;
    private final SysUserMapper userMapper;

    @Override
    public Page<Supplier> getSupplierList(Integer current, Integer size, String supplierName, Integer status) {
        Page<Supplier> page = new Page<>(current, size);
        return lambdaQuery()
                .like(supplierName != null, Supplier::getSupplierName, supplierName)
                .eq(status != null, Supplier::getStatus, status)
                .orderByDesc(Supplier::getCreateTime)
                .page(page);
    }

    @Override
    public List<Supplier> getEnabledSuppliers() {
        return lambdaQuery()
                .eq(Supplier::getStatus, 1)
                .orderByDesc(Supplier::getCreateTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSupplier(SupplierDTO dto, Long operatorId) {
        // 检查供应商名称是否存在
        Long count = lambdaQuery()
                .eq(Supplier::getSupplierName, dto.getSupplierName())
                .count();
        if (count > 0) {
            throw new BusinessException("供应商名称已存在");
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.insert(supplier);
        
        // 发送供应商创建通知
        log.info("=== 开始发送供应商创建通知 ===");
        log.info("供应商ID: {}, 操作人ID: {}, 供应商名称: {}", supplier.getId(), operatorId, supplier.getSupplierName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySupplierOperation(
                operatorId, operatorRole, "CREATE", supplier.getSupplierName(), supplier.getId()
            );
            log.info("=== 供应商创建通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送供应商创建通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(SupplierDTO dto, Long operatorId) {
        if (dto.getId() == null) {
            throw new BusinessException("供应商ID不能为空");
        }

        Supplier supplier = supplierMapper.selectById(dto.getId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 检查供应商名称是否被其他供应商占用
        Long count = lambdaQuery()
                .eq(Supplier::getSupplierName, dto.getSupplierName())
                .ne(Supplier::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("供应商名称已存在");
        }

        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.updateById(supplier);
        
        // 发送供应商编辑通知
        log.info("=== 开始发送供应商编辑通知 ===");
        log.info("供应商ID: {}, 操作人ID: {}, 供应商名称: {}", supplier.getId(), operatorId, supplier.getSupplierName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySupplierOperation(
                operatorId, operatorRole, "UPDATE", supplier.getSupplierName(), supplier.getId()
            );
            log.info("=== 供应商编辑通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送供应商编辑通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id, Long operatorId) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 检查是否有关联的采购订单
        // TODO: 添加业务逻辑检查

        supplierMapper.deleteById(id);
        
        // 发送供应商删除通知
        log.info("=== 开始发送供应商删除通知 ===");
        log.info("供应商ID: {}, 操作人ID: {}, 供应商名称: {}", id, operatorId, supplier.getSupplierName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySupplierOperation(
                operatorId, operatorRole, "DELETE", supplier.getSupplierName(), supplier.getId()
            );
            log.info("=== 供应商删除通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送供应商删除通知失败 ===", e);
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

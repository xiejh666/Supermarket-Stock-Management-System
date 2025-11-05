package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.SupplierDTO;
import com.supermarket.entity.Supplier;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商服务实现
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private final SupplierMapper supplierMapper;

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
    public void createSupplier(SupplierDTO dto) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(SupplierDTO dto) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 可以添加检查：是否有采购订单关联

        supplierMapper.deleteById(id);
    }
}




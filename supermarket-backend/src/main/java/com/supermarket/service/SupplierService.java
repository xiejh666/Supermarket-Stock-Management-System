package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.SupplierDTO;
import com.supermarket.entity.Supplier;

import java.util.List;

/**
 * 供应商服务接口
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * 分页查询供应商列表
     */
    Page<Supplier> getSupplierList(Integer current, Integer size, String supplierName, Integer status);

    /**
     * 查询所有启用的供应商
     */
    List<Supplier> getEnabledSuppliers();

    /**
     * 创建供应商
     */
    void createSupplier(SupplierDTO dto, Long operatorId);

    /**
     * 更新供应商
     */
    void updateSupplier(SupplierDTO dto, Long operatorId);

    /**
     * 删除供应商
     */
    void deleteSupplier(Long id, Long operatorId);
}




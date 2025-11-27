package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Category;
import com.supermarket.entity.Inventory;
import com.supermarket.entity.InventoryLog;
import com.supermarket.entity.Product;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.mapper.InventoryLogMapper;
import com.supermarket.mapper.InventoryMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.InventoryService;
import com.supermarket.vo.InventoryLogVO;
import com.supermarket.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务实现
 */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<InventoryVO> getInventoryList(Integer current, Integer size, String productName, Long categoryId, Boolean isWarning) {
        Page<Inventory> page = new Page<>(current, size);
        
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        Page<Inventory> inventoryPage = inventoryMapper.selectPage(page, wrapper);
        
        // 查询商品信息
        List<Long> productIds = inventoryPage.getRecords().stream()
                .map(Inventory::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return new Page<>(current, size, 0);
        }
        
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 查询分类信息
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> categoryMap = categoryIds.isEmpty() ? 
                new HashMap<>() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getCategoryName));
        
        // 转换为VO
        List<InventoryVO> voList = inventoryPage.getRecords().stream()
                .map(inventory -> {
                    Product product = productMap.get(inventory.getProductId());
                    if (product == null) {
                        return null;
                    }
                    
                    InventoryVO vo = new InventoryVO();
                    vo.setId(inventory.getId());
                    vo.setProductId(inventory.getProductId());
                    vo.setProductName(product.getProductName());
                    vo.setProductCode(product.getProductCode());
                    vo.setCategoryName(categoryMap.get(product.getCategoryId()));
                    vo.setQuantity(inventory.getQuantity());
                    vo.setStock(inventory.getQuantity()); // 前端兼容字段
                    vo.setWarningQuantity(inventory.getWarningQuantity());
                    vo.setMinStock(inventory.getWarningQuantity()); // 前端兼容字段
                    vo.setIsWarning(inventory.getQuantity() <= inventory.getWarningQuantity());
                    vo.setUpdateTime(inventory.getUpdateTime());
                    return vo;
                })
                .filter(vo -> vo != null)
                .filter(vo -> productName == null || vo.getProductName().contains(productName))
                .filter(vo -> {
                    // 分类过滤
                    if (categoryId == null) {
                        return true;
                    }
                    Product product = productMap.get(vo.getProductId());
                    return product != null && categoryId.equals(product.getCategoryId());
                })
                .filter(vo -> isWarning == null || vo.getIsWarning().equals(isWarning))
                .collect(Collectors.toList());
        
        Page<InventoryVO> resultPage = new Page<>(current, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(voList.size());
        
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(Long productId, Integer quantity, String orderNo, String remark, Long operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        
        // 查询或创建库存记录
        Inventory inventory = lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .one();
        
        if (inventory == null) {
            // 初始化库存
            inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setQuantity(quantity);
            inventory.setWarningQuantity(10); // 默认预警值
            inventoryMapper.insert(inventory);
        } else {
            // 更新库存
            int beforeQuantity = inventory.getQuantity();
            inventory.setQuantity(beforeQuantity + quantity);
            inventoryMapper.updateById(inventory);
        }
        
        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeType(1); // 入库
        log.setChangeQuantity(quantity);
        log.setBeforeQuantity(inventory.getQuantity() - quantity);
        log.setAfterQuantity(inventory.getQuantity());
        log.setOrderNo(orderNo);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(Long productId, Integer quantity, String orderNo, String remark, Long operatorId) {
        if (quantity <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }
        
        // 查询库存
        Inventory inventory = lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .one();
        
        if (inventory == null) {
            throw new BusinessException("商品库存不存在");
        }
        
        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("库存不足");
        }
        
        // 更新库存
        int beforeQuantity = inventory.getQuantity();
        inventory.setQuantity(beforeQuantity - quantity);
        inventoryMapper.updateById(inventory);
        
        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeType(2); // 出库
        log.setChangeQuantity(-quantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(inventory.getQuantity());
        log.setOrderNo(orderNo);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long productId, Integer newQuantity, String remark, Long operatorId) {
        if (newQuantity < 0) {
            throw new BusinessException("库存数量不能为负数");
        }
        
        // 查询库存
        Inventory inventory = lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .one();
        
        if (inventory == null) {
            throw new BusinessException("商品库存不存在");
        }
        
        int beforeQuantity = inventory.getQuantity();
        int changeQuantity = newQuantity - beforeQuantity;
        
        // 更新库存
        inventory.setQuantity(newQuantity);
        inventoryMapper.updateById(inventory);
        
        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeType(3); // 盘点调整
        log.setChangeQuantity(changeQuantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(newQuantity);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(Long productId, Integer warningQuantity) {
        // 检查是否已存在
        Long count = lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .count();
        
        if (count > 0) {
            return; // 已存在，不重复初始化
        }
        
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(0);
        inventory.setWarningQuantity(warningQuantity != null ? warningQuantity : 10);
        inventoryMapper.insert(inventory);
    }

    @Override
    public List<InventoryLogVO> getInventoryLogs(Long productId) {
        // 查询库存变动日志
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryLog::getProductId, productId)
               .orderByDesc(InventoryLog::getCreateTime);
        List<InventoryLog> logs = inventoryLogMapper.selectList(wrapper);
        
        // 查询商品信息
        Product product = productMapper.selectById(productId);
        String productName = product != null ? product.getProductName() : "";
        
        // 转换为VO
        return logs.stream().map(log -> {
            InventoryLogVO vo = new InventoryLogVO();
            vo.setId(log.getId());
            vo.setProductId(log.getProductId());
            vo.setProductName(productName);
            vo.setChangeType(log.getChangeType());
            vo.setChangeTypeName(getChangeTypeName(log.getChangeType()));
            vo.setChangeQuantity(log.getChangeQuantity());
            vo.setBeforeQuantity(log.getBeforeQuantity());
            vo.setAfterQuantity(log.getAfterQuantity());
            vo.setOrderNo(log.getOrderNo());
            vo.setRemark(log.getRemark());
            vo.setOperatorId(log.getOperatorId());
            vo.setCreateTime(log.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }
    
    private String getChangeTypeName(Integer changeType) {
        switch (changeType) {
            case 1: return "入库";
            case 2: return "出库";
            case 3: return "盘点调整";
            default: return "未知";
        }
    }
}


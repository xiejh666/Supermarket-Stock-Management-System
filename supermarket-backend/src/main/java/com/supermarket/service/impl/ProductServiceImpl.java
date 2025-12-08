package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Inventory;
import com.supermarket.entity.Product;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.InventoryMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.CacheService;
import com.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final BusinessOperationNotificationService businessNotificationService;
    private final SysUserMapper userMapper;
    private final CacheService cacheService;
    
    private static final String CACHE_KEY_PRODUCT_PREFIX = "product:";

    @Override
    public Page<Product> getProductList(Integer current, Integer size, String productName, 
                                       Long categoryId, Integer status) {
        Page<Product> page = new Page<>(current, size);
        Page<Product> result = lambdaQuery()
                .like(productName != null, Product::getProductName, productName)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreateTime)
                .page(page);
        
        // 设置 imageUrl、retailPrice 和库存信息（用于前端展示）
        result.getRecords().forEach(product -> {
            product.setImageUrl(product.getImage());
            product.setRetailPrice(product.getPrice());
            
            // 获取库存信息
            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getProductId, product.getId())
            );
            if (inventory != null) {
                product.setStock(inventory.getQuantity());
                product.setWarningStock(inventory.getWarningQuantity());
            } else {
                product.setStock(0);
                product.setWarningStock(0);
            }
        });
        
        return result;
    }

    /**
     * 获取商品详情（包含库存预警信息）
     */
    public Product getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            // 设置 retailPrice 和 imageUrl 用于前端显示
            product.setRetailPrice(product.getPrice());
            product.setImageUrl(product.getImage());
            
            // 获取库存预警信息
            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getProductId, id)
            );
            if (inventory != null) {
                product.setStock(inventory.getQuantity());
                product.setWarningStock(inventory.getWarningQuantity());
            }
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto, Long operatorId) {
        // 检查商品名称是否存在
        Long count = lambdaQuery()
                .eq(Product::getProductName, dto.getProductName())
                .count();
        if (count > 0) {
            throw new BusinessException("商品名称已存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        // 将 retailPrice 赋值给 price 字段（数据库字段）
        product.setPrice(dto.getRetailPrice());
        // 将 imageUrl 赋值给 image 字段（数据库字段）
        product.setImage(dto.getImageUrl());
        productMapper.insert(product);
        
        // 清除商品相关缓存
        cacheService.evictByPattern(CACHE_KEY_PRODUCT_PREFIX + "*");

        // 创建库存记录
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        // 使用DTO中的预警库存，如果没有设置则默认为10
        inventory.setWarningQuantity(dto.getWarningStock() != null ? dto.getWarningStock() : 10);
        inventoryMapper.insert(inventory);

        // 发送商品创建通知
        try {
            String operatorRole = getUserRole(operatorId);
            businessNotificationService.notifyProductOperation(
                operatorId, operatorRole, "CREATE", product.getProductName(), product.getId()
            );
        } catch (Exception e) {
            log.error("发送商品创建通知失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO dto, Long operatorId) {
        if (dto.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = productMapper.selectById(dto.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查商品名称是否被其他商品占用
        Long count = lambdaQuery()
                .eq(Product::getProductName, dto.getProductName())
                .ne(Product::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("商品名称已存在");
        }

        BeanUtils.copyProperties(dto, product);
        // 将 retailPrice 赋值给 price 字段（数据库字段）
        product.setPrice(dto.getRetailPrice());
        // 将 imageUrl 赋值给 image 字段（数据库字段）
        product.setImage(dto.getImageUrl());
        productMapper.updateById(product);
        
        // 更新库存预警值
        if (dto.getWarningStock() != null) {
            Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getProductId, product.getId())
            );
            if (inventory != null) {
                inventory.setWarningQuantity(dto.getWarningStock());
                inventoryMapper.updateById(inventory);
            }
        }
        
        // 清除商品相关缓存
        cacheService.evict(CACHE_KEY_PRODUCT_PREFIX + product.getId());
        cacheService.evictByPattern(CACHE_KEY_PRODUCT_PREFIX + "*");

        // 发送商品编辑通知
        try {
            String operatorRole = getUserRole(operatorId);
            businessNotificationService.notifyProductOperation(
                operatorId, operatorRole, "UPDATE", product.getProductName(), product.getId()
            );
        } catch (Exception e) {
            log.error("发送商品编辑通知失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id, Long operatorId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否有库存
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, id)
        );
        if (inventory != null && inventory.getQuantity() > 0) {
            throw new BusinessException("商品还有库存，不能删除");
        }

        // 删除库存记录
        if (inventory != null) {
            inventoryMapper.deleteById(inventory.getId());
        }

        // 删除商品
        productMapper.deleteById(id);
        
        // 清除商品相关缓存
        cacheService.evict(CACHE_KEY_PRODUCT_PREFIX + id);
        cacheService.evictByPattern(CACHE_KEY_PRODUCT_PREFIX + "*");

        // 发送商品删除通知
        try {
            String operatorRole = getUserRole(operatorId);
            businessNotificationService.notifyProductOperation(
                operatorId, operatorRole, "DELETE", product.getProductName(), product.getId()
            );
        } catch (Exception e) {
            log.error("发送商品删除通知失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long id, Integer status, Long operatorId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        productMapper.updateById(product);
        
        // 清除商品相关缓存
        cacheService.evict(CACHE_KEY_PRODUCT_PREFIX + id);
        cacheService.evictByPattern(CACHE_KEY_PRODUCT_PREFIX + "*");

        // 发送商品状态更新通知
        try {
            String operatorRole = getUserRole(operatorId);
            String operation = status == 1 ? "ENABLE" : "DISABLE";
            businessNotificationService.notifyProductOperation(
                operatorId, operatorRole, operation, product.getProductName(), product.getId()
            );
        } catch (Exception e) {
            log.error("发送商品状态更新通知失败", e);
        }
    }

    /**
     * 获取用户角色编码
     */
    private String getUserRole(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
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

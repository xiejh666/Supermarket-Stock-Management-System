package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.CacheService;
import com.supermarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final BusinessOperationNotificationService businessNotificationService;
    private final SysUserMapper userMapper;
    private final CacheService cacheService;
    
    private static final String CACHE_KEY_CATEGORY_TREE = "category:tree";

    @Override
    public Page<Category> getCategoryList(Integer current, Integer size, String categoryName, Integer status) {
        Page<Category> page = new Page<>(current, size);
        return lambdaQuery()
                .like(categoryName != null, Category::getCategoryName, categoryName)
                .orderByDesc(Category::getCreateTime)
                .page(page);
    }

    @Override
    public List<Category> getCategoryTree() {
        // 使用缓存，缓存30分钟
        return cacheService.getOrLoad(CACHE_KEY_CATEGORY_TREE, () -> {
            log.info("从数据库加载分类树数据");
            return lambdaQuery()
                    .orderByAsc(Category::getSortOrder)
                    .list();
        }, 30);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryDTO dto, Long operatorId) {
        // 检查分类名称是否存在
        Long count = lambdaQuery()
                .eq(Category::getCategoryName, dto.getCategoryName())
                .count();
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        categoryMapper.insert(category);
        
        // 清除分类树缓存
        cacheService.evict(CACHE_KEY_CATEGORY_TREE);
        log.info("已清除分类树缓存");

        // 发送分类创建通知
        log.info("=== 开始发送分类创建通知 ===");
        log.info("分类ID: {}, 操作人ID: {}, 分类名称: {}", category.getId(), operatorId, category.getCategoryName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCategoryOperation(
                operatorId, operatorRole, "CREATE", category.getCategoryName(), category.getId()
            );
            log.info("=== 分类创建通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送分类创建通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryDTO dto, Long operatorId) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }

        Category category = categoryMapper.selectById(dto.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查分类名称是否被其他分类占用
        Long count = lambdaQuery()
                .eq(Category::getCategoryName, dto.getCategoryName())
                .ne(Category::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        BeanUtils.copyProperties(dto, category);
        categoryMapper.updateById(category);
        
        // 清除分类树缓存
        cacheService.evict(CACHE_KEY_CATEGORY_TREE);
        log.info("已清除分类树缓存");

        // 发送分类编辑通知
        log.info("=== 开始发送分类编辑通知 ===");
        log.info("分类ID: {}, 操作人ID: {}, 分类名称: {}", category.getId(), operatorId, category.getCategoryName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCategoryOperation(
                operatorId, operatorRole, "UPDATE", category.getCategoryName(), category.getId()
            );
            log.info("=== 分类编辑通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送分类编辑通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id, Long operatorId) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有商品使用该分类
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id)
        );
        if (productCount > 0) {
            throw new BusinessException("该分类下还有商品，不能删除");
        }

        // 删除分类
        categoryMapper.deleteById(id);
        
        // 清除分类树缓存
        cacheService.evict(CACHE_KEY_CATEGORY_TREE);
        log.info("已清除分类树缓存");

        // 发送分类删除通知
        log.info("=== 开始发送分类删除通知 ===");
        log.info("分类ID: {}, 操作人ID: {}, 分类名称: {}", id, operatorId, category.getCategoryName());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifyCategoryOperation(
                operatorId, operatorRole, "DELETE", category.getCategoryName(), category.getId()
            );
            log.info("=== 分类删除通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送分类删除通知失败 ===", e);
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

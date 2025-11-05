package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Page<Category> getCategoryList(Integer current, Integer size, String categoryName, Integer status) {
        Page<Category> page = new Page<>(current, size);
        return lambdaQuery()
                .like(categoryName != null, Category::getCategoryName, categoryName)
                .orderByAsc(Category::getSortOrder)
                .orderByDesc(Category::getCreateTime)
                .page(page);
    }

    @Override
    public List<Category> getCategoryTree() {
        return lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryDTO dto) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryDTO dto) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        Long childCount = lambdaQuery()
                .eq(Category::getParentId, id)
                .count();
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，不能删除");
        }

        // 可以添加检查：是否有商品使用该分类

        categoryMapper.deleteById(id);
    }
}



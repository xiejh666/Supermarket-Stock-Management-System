package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分页查询分类列表
     */
    Page<Category> getCategoryList(Integer current, Integer size, String categoryName, Integer status);

    /**
     * 查询所有分类（树形结构）
     */
    List<Category> getCategoryTree();

    /**
     * 创建分类
     */
    void createCategory(CategoryDTO dto, Long operatorId);

    /**
     * 更新分类
     */
    void updateCategory(CategoryDTO dto, Long operatorId);

    /**
     * 删除分类
     */
    void deleteCategory(Long id, Long operatorId);
}




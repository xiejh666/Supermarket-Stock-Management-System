package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     */
    Page<Product> getProductList(Integer current, Integer size, String productName, 
                                  Long categoryId, Integer status);

    /**
     * 创建商品
     */
    void createProduct(ProductDTO dto);

    /**
     * 更新商品
     */
    void updateProduct(ProductDTO dto);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);

    /**
     * 修改商品状态
     */
    void updateProductStatus(Long id, Integer status);
}




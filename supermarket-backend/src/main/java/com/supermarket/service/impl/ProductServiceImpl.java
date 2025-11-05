package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Product;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> getProductList(Integer current, Integer size, String productName, 
                                        Long categoryId, Integer status) {
        Page<Product> page = new Page<>(current, size);
        return lambdaQuery()
                .like(productName != null, Product::getProductName, productName)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreateTime)
                .page(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto) {
        // 检查商品编码是否存在（如果提供了编码）
        if (dto.getProductCode() != null) {
            Long count = lambdaQuery()
                    .eq(Product::getProductCode, dto.getProductCode())
                    .count();
            if (count > 0) {
                throw new BusinessException("商品编码已存在");
            }
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        
        productMapper.insert(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = productMapper.selectById(dto.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查商品编码是否被其他商品占用
        if (dto.getProductCode() != null) {
            Long count = lambdaQuery()
                    .eq(Product::getProductCode, dto.getProductCode())
                    .ne(Product::getId, dto.getId())
                    .count();
            if (count > 0) {
                throw new BusinessException("商品编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, product);
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 可以添加检查：是否有库存或订单关联

        productMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        productMapper.updateById(product);
    }
}



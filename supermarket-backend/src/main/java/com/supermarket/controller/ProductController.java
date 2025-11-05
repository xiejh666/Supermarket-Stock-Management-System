package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Product;
import com.supermarket.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/products")
@Api(tags = "商品管理")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    @ApiOperation("分页查询商品列表")
    public Result<Page<Product>> getProductList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("商品名称") @RequestParam(required = false) String productName,
            @ApiParam("分类ID") @RequestParam(required = false) String categoryId,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        // 处理categoryId，如果是"all"或为空，则传null
        Long categoryIdLong = null;
        if (categoryId != null && !"all".equalsIgnoreCase(categoryId) && !categoryId.trim().isEmpty()) {
            try {
                categoryIdLong = Long.parseLong(categoryId);
            } catch (NumberFormatException e) {
                // 忽略无效的categoryId
            }
        }
        Page<Product> page = productService.getProductList(current, size, productName, categoryIdLong, status);
        return Result.success(page);
    }

    @GetMapping("/all")
    @ApiOperation("查询所有商品（不分页）")
    public Result<List<Product>> getAllProducts() {
        List<Product> list = productService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商品")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @PostMapping
    @ApiOperation("创建商品")
    public Result<Void> createProduct(@Valid @RequestBody ProductDTO dto) {
        productService.createProduct(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("更新商品")
    public Result<Void> updateProduct(@Valid @RequestBody ProductDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("修改商品状态")
    public Result<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        productService.updateProductStatus(id, status);
        return Result.success();
    }
}



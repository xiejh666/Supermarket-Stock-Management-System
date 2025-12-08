package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品分类管理控制器
 */
@RestController
@RequestMapping("/categories")
@Api(tags = "商品分类管理")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("分页查询分类列表")
    public Result<Page<Category>> getCategoryList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("分类名称") @RequestParam(required = false) String categoryName,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<Category> page = categoryService.getCategoryList(current, size, categoryName, status);
        return Result.success(page);
    }

    @GetMapping("/tree")
    @ApiOperation("查询所有分类（树形结构）")
    public Result<List<Category>> getCategoryTree() {
        List<Category> list = categoryService.getCategoryTree();
        return Result.success(list);
    }

    @GetMapping("/all")
    @ApiOperation("查询所有分类（不分页）")
    public Result<List<Category>> getAllCategories() {
        List<Category> list = categoryService.list();
        return Result.success(list);
    }

    @PostMapping
    @ApiOperation("创建分类")
    public Result<Void> createCategory(
            @Valid @RequestBody CategoryDTO dto,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        categoryService.createCategory(dto, operatorId);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("更新分类")
    public Result<Void> updateCategory(
            @Valid @RequestBody CategoryDTO dto,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        categoryService.updateCategory(dto, operatorId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public Result<Void> deleteCategory(
            @PathVariable Long id,
            @ApiParam("操作人ID") @RequestParam Long operatorId) {
        categoryService.deleteCategory(id, operatorId);
        return Result.success();
    }
}



package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Inventory;
import com.supermarket.vo.InventoryLogVO;
import com.supermarket.vo.InventoryVO;

import java.util.List;

/**
 * 库存服务接口
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * 分页查询库存列表
     */
    Page<InventoryVO> getInventoryList(Integer current, Integer size, String productName, Long categoryId, Boolean isWarning);

    /**
     * 入库
     */
    void inbound(Long productId, Integer quantity, String orderNo, String remark, Long operatorId);

    /**
     * 出库
     */
    void outbound(Long productId, Integer quantity, String orderNo, String remark, Long operatorId);

    /**
     * 盘点调整
     */
    void adjust(Long productId, Integer newQuantity, String remark, Long operatorId);

    /**
     * 初始化商品库存
     */
    void initInventory(Long productId, Integer warningQuantity);
    
    /**
     * 查询商品库存变动历史
     */
    List<InventoryLogVO> getInventoryLogs(Long productId);
}




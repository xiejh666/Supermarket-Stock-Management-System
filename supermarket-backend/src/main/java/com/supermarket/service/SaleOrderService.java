package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.SaleOrderDTO;
import com.supermarket.entity.SaleOrder;

/**
 * 销售订单服务接口
 */
public interface SaleOrderService extends IService<SaleOrder> {

    /**
     * 分页查询销售订单列表
     */
    Page<SaleOrder> getSaleOrderList(Integer current, Integer size, String orderNo, 
                                      Long customerId, Integer status);

    /**
     * 根据ID查询销售订单详情
     */
    SaleOrder getSaleOrderDetail(Long id);

    /**
     * 创建销售订单
     */
    void createSaleOrder(SaleOrderDTO dto, Long cashierId);

    /**
     * 支付订单
     */
    void paySaleOrder(Long id);

    /**
     * 取消订单
     */
    void cancelSaleOrder(Long id);

    /**
     * 删除订单
     */
    void deleteSaleOrder(Long id);
}




package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.PurchaseOrderDTO;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.vo.PurchaseOrderVO;

/**
 * 采购订单服务接口
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 分页查询采购订单列表
     */
    Page<PurchaseOrderVO> getPurchaseOrderList(Integer current, Integer size, String orderNo, 
                                                 Long supplierId, Integer status, Long applicantId);

    /**
     * 根据ID查询采购订单详情
     */
    PurchaseOrderVO getPurchaseOrderDetail(Long id);

    /**
     * 创建采购订单
     */
    void createPurchaseOrder(PurchaseOrderDTO dto, Long applicantId);

    /**
     * 审核采购订单
     */
    void auditPurchaseOrder(Long id, Integer status, String auditRemark, Long auditorId);

    /**
     * 确认入库
     */
    void confirmInbound(Long id, Long operatorId);

    /**
     * 删除采购订单
     */
    void deletePurchaseOrder(Long id, Long operatorId);
}




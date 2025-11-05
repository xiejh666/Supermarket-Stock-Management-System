package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.vo.PurchaseOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购订单Mapper
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    /**
     * 查询采购订单列表（包含关联信息）
     */
    List<PurchaseOrderVO> selectPurchaseOrderVOList(@Param("orderNo") String orderNo,
                                                      @Param("supplierId") Long supplierId,
                                                      @Param("status") Integer status,
                                                      @Param("applicantId") Long applicantId);

    /**
     * 根据ID查询采购订单（包含明细）
     */
    PurchaseOrderVO selectPurchaseOrderVOById(@Param("id") Long id);
}




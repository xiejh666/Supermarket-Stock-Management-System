package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.PurchaseOrderItem;
import com.supermarket.vo.PurchaseOrderItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购订单明细Mapper
 */
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {

    /**
     * 根据订单ID查询明细列表
     */
    List<PurchaseOrderItemVO> selectItemVOListByOrderId(@Param("orderId") Long orderId);
}




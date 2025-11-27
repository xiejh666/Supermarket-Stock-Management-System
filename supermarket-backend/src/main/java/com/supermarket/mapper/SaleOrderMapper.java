package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.entity.SaleOrder;
import com.supermarket.vo.SaleOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 销售订单Mapper
 */
@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {

    /**
     * 分页查询销售订单列表
     */
    Page<SaleOrderVO> selectSaleOrderPage(Page<SaleOrderVO> page,
                                         @Param("orderNo") String orderNo,
                                         @Param("customerName") String customerName,
                                         @Param("status") Integer status,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
    
    /**
     * 获取销售趋势数据 - 按天统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售趋势数据
     */
    List<Map<String, Object>> getSalesTrendByDay(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取销售趋势数据 - 按月统计
     * @param year 年份
     * @param currentMonth 当前月份
     * @return 销售趋势数据
     */
    List<Map<String, Object>> getSalesTrendByMonth(@Param("year") int year, 
                                                  @Param("currentMonth") int currentMonth);
}




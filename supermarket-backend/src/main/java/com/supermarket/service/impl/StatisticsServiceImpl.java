package com.supermarket.service.impl;

import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import com.supermarket.service.StatisticsService;
import com.supermarket.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 统计分析服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SaleOrderMapper saleOrderMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public StatisticsVO getDashboardStatistics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        // 今日销售额
        BigDecimal todaySales = saleOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, 1)
                        .between(SaleOrder::getCreateTime, todayStart, todayEnd)
        ).stream()
                .map(SaleOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月销售额
        BigDecimal monthSales = saleOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, 1)
                        .ge(SaleOrder::getCreateTime, monthStart)
        ).stream()
                .map(SaleOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日订单数
        Integer todayOrders = Math.toIntExact(saleOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .between(SaleOrder::getCreateTime, todayStart, todayEnd)
        ));

        // 本月订单数
        Integer monthOrders = Math.toIntExact(saleOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, monthStart)
        ));

        // 商品总数
        Integer totalProducts = Math.toIntExact(productMapper.selectCount(null));

        // 低库存商品数
        Integer lowStockProducts = Math.toIntExact(inventoryMapper.selectList(null).stream()
                .filter(inv -> inv.getQuantity() <= inv.getWarningQuantity())
                .count());

        // 待审核采购订单数
        Integer pendingPurchaseOrders = Math.toIntExact(purchaseOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getStatus, 0)
        ));

        // 供应商总数
        Integer totalSuppliers = Math.toIntExact(supplierMapper.selectCount(null));

        return StatisticsVO.builder()
                .todaySales(todaySales)
                .monthSales(monthSales)
                .todayOrders(todayOrders)
                .monthOrders(monthOrders)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .pendingPurchaseOrders(pendingPurchaseOrders)
                .totalSuppliers(totalSuppliers)
                .build();
    }
}


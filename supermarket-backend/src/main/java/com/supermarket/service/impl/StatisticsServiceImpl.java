package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import com.supermarket.service.StatisticsService;
import com.supermarket.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierMapper supplierMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public StatisticsVO getDashboardStatistics(String period) {
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

        // 昨日销售额（用于计算今日变化）
        LocalDateTime yesterdayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime yesterdayEnd = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
        BigDecimal yesterdaySales = saleOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, 1)
                        .between(SaleOrder::getCreateTime, yesterdayStart, yesterdayEnd)
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

        // 上月销售额（用于计算本月变化）
        LocalDateTime lastMonthStart = LocalDateTime.of(LocalDate.now().minusMonths(1).withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastMonthEnd = LocalDateTime.of(LocalDate.now().withDayOfMonth(1).minusDays(1), LocalTime.MAX);
        BigDecimal lastMonthSales = saleOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrder>()
                        .eq(SaleOrder::getStatus, 1)
                        .between(SaleOrder::getCreateTime, lastMonthStart, lastMonthEnd)
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

        // 计算变化百分比
        Double todaySalesChange = calculatePercentageChange(todaySales, yesterdaySales);
        Double monthSalesChange = calculatePercentageChange(monthSales, lastMonthSales);
        
        // 简化的低库存和待审核订单变化（这里可以根据实际需求调整）
        Double lowStockChange = -2.1; // 示例：假设库存管理改善，低库存商品减少
        Double pendingOrdersChange = 5.4; // 示例：假设订单增加

        return StatisticsVO.builder()
                .todaySales(todaySales)
                .monthSales(monthSales)
                .todayOrders(todayOrders)
                .monthOrders(monthOrders)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .pendingPurchaseOrders(pendingPurchaseOrders)
                .totalSuppliers(totalSuppliers)
                .todaySalesChange(todaySalesChange)
                .monthSalesChange(monthSalesChange)
                .lowStockChange(lowStockChange)
                .pendingOrdersChange(pendingOrdersChange)
                .salesTrend(getSalesTrendData(period))
                .categoryRatio(getCategoryRatioData(period))
                .inventoryWarning(getInventoryWarningData())
                .purchaseStatistics(getPurchaseStatisticsData())
                .build();
    }

    /**
     * 获取销售趋势数据
     * @param period 时间周期 (week/month/year)
     */
    private StatisticsVO.SalesTrendData getSalesTrendData(String period) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesData = new ArrayList<>();
        List<Integer> orderData = new ArrayList<>();

        switch (period) {
            case "week":
                // 获取最近7天的数据
                LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
                LocalDateTime weekEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
                
                List<Map<String, Object>> weekData = saleOrderMapper.getSalesTrendByDay(weekStart, weekEnd);
                
                // 创建完整的7天数据，包括没有数据的日期
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    String dateStr = date.toString();
                    String label = date.format(DateTimeFormatter.ofPattern("MM-dd"));
                    
                    // 查找对应日期的数据
                    Map<String, Object> dayData = weekData.stream()
                            .filter(data -> dateStr.equals(data.get("date").toString()))
                            .findFirst()
                            .orElse(null);
                    
                    labels.add(label);
                    if (dayData != null) {
                        salesData.add(new BigDecimal(dayData.get("salesAmount").toString()));
                        orderData.add(Integer.parseInt(dayData.get("orderCount").toString()));
                    } else {
                        salesData.add(BigDecimal.ZERO);
                        orderData.add(0);
                    }
                }
                break;

            case "month":
                // 获取本月每天的数据
                LocalDate today = LocalDate.now();
                LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
                LocalDateTime monthEnd = LocalDateTime.of(today, LocalTime.MAX);
                
                List<Map<String, Object>> monthData = saleOrderMapper.getSalesTrendByDay(monthStart, monthEnd);
                
                // 创建本月每天的数据
                LocalDate currentDay = today.withDayOfMonth(1);
                while (!currentDay.isAfter(today)) {
                    String dateStr = currentDay.toString();
                    String label = currentDay.format(DateTimeFormatter.ofPattern("MM-dd"));
                    
                    // 查找对应日期的数据
                    Map<String, Object> dayData = monthData.stream()
                            .filter(data -> dateStr.equals(data.get("date").toString()))
                            .findFirst()
                            .orElse(null);
                    
                    labels.add(label);
                    if (dayData != null) {
                        salesData.add(new BigDecimal(dayData.get("salesAmount").toString()));
                        orderData.add(Integer.parseInt(dayData.get("orderCount").toString()));
                    } else {
                        salesData.add(BigDecimal.ZERO);
                        orderData.add(0);
                    }
                    
                    currentDay = currentDay.plusDays(1);
                }
                break;

            case "year":
                // 获取今年每月的数据
                LocalDate currentDate = LocalDate.now();
                int currentYear = currentDate.getYear();
                int currentMonth = currentDate.getMonthValue();
                
                List<Map<String, Object>> yearData = saleOrderMapper.getSalesTrendByMonth(currentYear, currentMonth);
                
                // 创建今年每月的数据
                for (int month = 1; month <= currentMonth; month++) {
                    String label = month + "月";
                    
                    // 查找对应月份的数据
                    final int finalMonth = month;
                    Map<String, Object> monthDataItem = yearData.stream()
                            .filter(data -> finalMonth == Integer.parseInt(data.get("month").toString()))
                            .findFirst()
                            .orElse(null);
                    
                    labels.add(label);
                    if (monthDataItem != null) {
                        salesData.add(new BigDecimal(monthDataItem.get("salesAmount").toString()));
                        orderData.add(Integer.parseInt(monthDataItem.get("orderCount").toString()));
                    } else {
                        salesData.add(BigDecimal.ZERO);
                        orderData.add(0);
                    }
                }
                break;

            default:
                // 默认返回本周数据
                return getSalesTrendData("week");
        }

        return StatisticsVO.SalesTrendData.builder()
                .labels(labels)
                .salesData(salesData)
                .orderData(orderData)
                .build();
    }

    /**
     * 获取分类占比数据
     * @param period 时间周期 (week/month/year)
     */
    private List<StatisticsVO.CategoryRatioData> getCategoryRatioData(String period) {
        // 计算时间范围
        LocalDateTime startTime = getStartTimeByPeriod(period);
        LocalDateTime endTime = LocalDateTime.now();

        // 获取时间范围内所有已完成的销售订单ID（只查询ID，减少数据传输）
        List<Long> orderIds = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .select(SaleOrder::getId)
                        .eq(SaleOrder::getStatus, 1) // 只统计已完成的订单
                        .between(SaleOrder::getCreateTime, startTime, endTime)
        ).stream()
                .map(SaleOrder::getId)
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取所有商品信息，避免在循环中查询
        List<Product> allProducts = productMapper.selectList(null);
        Map<Long, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 批量获取所有分类信息
        List<Category> allCategories = categoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        // 分批查询订单明细（避免IN子句过长）
        List<SaleOrderItem> allOrderItems = new ArrayList<>();
        int batchSize = 1000;
        for (int i = 0; i < orderIds.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, orderIds.size());
            List<Long> batchIds = orderIds.subList(i, endIndex);
            
            List<SaleOrderItem> batchItems = saleOrderItemMapper.selectList(
                    new LambdaQueryWrapper<SaleOrderItem>()
                            .in(SaleOrderItem::getOrderId, batchIds)
            );
            allOrderItems.addAll(batchItems);
        }

        // 统计每个分类的销售额
        Map<Long, BigDecimal> categorySalesMap = new HashMap<>();
        BigDecimal totalSales = BigDecimal.ZERO;

        for (SaleOrderItem item : allOrderItems) {
            // 从缓存的Map中获取商品信息
            Product product = productMap.get(item.getProductId());
            if (product != null && product.getCategoryId() != null) {
                Long categoryId = product.getCategoryId();
                
                // 计算该商品的销售额（数量 * 单价）
                BigDecimal itemSales = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .setScale(2, RoundingMode.HALF_UP);
                
                // 累加到分类销售额
                categorySalesMap.put(categoryId, 
                        categorySalesMap.getOrDefault(categoryId, BigDecimal.ZERO).add(itemSales));
                totalSales = totalSales.add(itemSales);
            }
        }

        // 构建返回数据
        List<StatisticsVO.CategoryRatioData> categoryRatioList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categorySalesMap.entrySet()) {
            Long categoryId = entry.getKey();
            BigDecimal sales = entry.getValue();
            String categoryName = categoryNameMap.get(categoryId);
            
            if (categoryName != null && sales.compareTo(BigDecimal.ZERO) > 0) {
                Double percentage = totalSales.compareTo(BigDecimal.ZERO) > 0 
                        ? sales.divide(totalSales, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue()
                        : 0.0;
                
                categoryRatioList.add(StatisticsVO.CategoryRatioData.builder()
                        .name(categoryName)
                        .value(sales)
                        .percentage(percentage)
                        .build());
            }
        }

        // 按销售额排序，取前5名
        return categoryRatioList.stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 获取库存预警数据
     */
    private List<StatisticsVO.InventoryWarningData> getInventoryWarningData() {
        // 使用联查获取库存和商品信息
        List<Map<String, Object>> inventoryData = inventoryMapper.selectMaps(
                new LambdaQueryWrapper<Inventory>()
                        .select(Inventory::getProductId, Inventory::getQuantity, Inventory::getWarningQuantity)
        );
        
        List<StatisticsVO.InventoryWarningData> warningList = new ArrayList<>();

        for (Map<String, Object> data : inventoryData) {
            Long productId = (Long) data.get("product_id");
            Integer currentStock = (Integer) data.get("quantity");
            Integer warningQuantity = (Integer) data.get("warning_quantity");
            
            if (productId != null && currentStock != null && warningQuantity != null) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    int yellowThreshold = (int) Math.ceil(warningQuantity * 1.5); // 黄色预警阈值

                    // 红色预警：库存少于预警数量
                    if (currentStock < warningQuantity) {
                        warningList.add(StatisticsVO.InventoryWarningData.builder()
                                .productName(product.getProductName())
                                .currentStock(currentStock)
                                .warningStock(warningQuantity)
                                .warningLevel(3) // 红色 - 严重
                                .build());
                    }
                    // 黄色预警：库存少于预警数量的1.5倍
                    else if (currentStock < yellowThreshold) {
                        warningList.add(StatisticsVO.InventoryWarningData.builder()
                                .productName(product.getProductName())
                                .currentStock(currentStock)
                                .warningStock(warningQuantity)
                                .warningLevel(2) // 黄色 - 中等
                                .build());
                    }
                }
            }
        }

        // 如果没有预警，添加一个提示信息
        if (warningList.isEmpty()) {
            warningList.add(StatisticsVO.InventoryWarningData.builder()
                    .productName("当前库存充裕")
                    .currentStock(0)
                    .warningStock(0)
                    .warningLevel(0) // 绿色 - 正常
                    .build());
        }

        // 按预警级别排序，严重的在前
        return warningList.stream()
                .sorted((a, b) -> b.getWarningLevel().compareTo(a.getWarningLevel()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 获取采购统计数据
     */
    private StatisticsVO.PurchaseStatisticsData getPurchaseStatisticsData() {
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        // 本月采购金额和订单数
        List<PurchaseOrder> monthPurchaseOrders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getStatus, 1) // 已审核
                        .ge(PurchaseOrder::getCreateTime, monthStart)
        );

        BigDecimal monthPurchaseAmount = monthPurchaseOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer monthPurchaseOrderCount = monthPurchaseOrders.size();

        // 供应商采购排行
        Map<Long, StatisticsVO.SupplierPurchaseData> supplierDataMap = new HashMap<>();
        
        for (PurchaseOrder order : monthPurchaseOrders) {
            Long supplierId = order.getSupplierId();
            StatisticsVO.SupplierPurchaseData data = supplierDataMap.get(supplierId);
            
            if (data == null) {
                Supplier supplier = supplierMapper.selectById(supplierId);
                data = StatisticsVO.SupplierPurchaseData.builder()
                        .supplierName(supplier != null ? supplier.getSupplierName() : "未知供应商")
                        .purchaseAmount(BigDecimal.ZERO)
                        .orderCount(0)
                        .build();
                supplierDataMap.put(supplierId, data);
            }
            
            data.setPurchaseAmount(data.getPurchaseAmount().add(order.getTotalAmount()));
            data.setOrderCount(data.getOrderCount() + 1);
        }

        List<StatisticsVO.SupplierPurchaseData> supplierRanking = supplierDataMap.values().stream()
                .sorted((a, b) -> b.getPurchaseAmount().compareTo(a.getPurchaseAmount()))
                .limit(5)
                .collect(Collectors.toList());

        // 获取采购订单状态统计
        List<StatisticsVO.PurchaseStatusData> statusStatistics = getPurchaseStatusStatistics();

        return StatisticsVO.PurchaseStatisticsData.builder()
                .monthPurchaseAmount(monthPurchaseAmount)
                .monthPurchaseOrders(monthPurchaseOrderCount)
                .supplierRanking(supplierRanking)
                .statusStatistics(statusStatistics)
                .build();
    }

    /**
     * 获取采购订单状态统计
     */
    private List<StatisticsVO.PurchaseStatusData> getPurchaseStatusStatistics() {
        List<StatisticsVO.PurchaseStatusData> statusList = new ArrayList<>();
        
        // 统计各种状态的采购订单数量
        // 0-待审核, 1-已审核, 2-已拒绝, 3-已入库
        for (int status = 0; status <= 3; status++) {
            long count = purchaseOrderMapper.selectCount(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .eq(PurchaseOrder::getStatus, status)
            );
            
            String statusName;
            switch (status) {
                case 0: statusName = "待审核"; break;
                case 1: statusName = "已通过"; break;
                case 2: statusName = "已拒绝"; break;
                case 3: statusName = "已入库"; break;
                default: statusName = "未知"; break;
            }
            
            statusList.add(StatisticsVO.PurchaseStatusData.builder()
                    .statusName(statusName)
                    .orderCount((int) count)
                    .statusValue(status)
                    .build());
        }
        
        return statusList;
    }

    /**
     * 计算百分比变化
     * @param current 当前值
     * @param previous 之前值
     * @return 变化百分比
     */
    private Double calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change.divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return percentage.doubleValue();
    }

    /**
     * 根据时间周期获取开始时间
     * @param period 时间周期 (week/month/year)
     * @return 开始时间
     */
    private LocalDateTime getStartTimeByPeriod(String period) {
        LocalDate today = LocalDate.now();
        switch (period) {
            case "week":
                // 本周一
                return LocalDateTime.of(today.minusDays(today.getDayOfWeek().getValue() - 1), LocalTime.MIN);
            case "month":
                // 本月第一天
                return LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
            case "year":
                // 本年第一天
                return LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);
            default:
                // 默认本月
                return LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        }
    }

}


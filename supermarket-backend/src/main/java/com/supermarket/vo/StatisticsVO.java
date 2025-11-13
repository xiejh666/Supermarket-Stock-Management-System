package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("统计数据视图对象")
public class StatisticsVO {

    @ApiModelProperty("今日销售额")
    private BigDecimal todaySales;

    @ApiModelProperty("本月销售额")
    private BigDecimal monthSales;

    @ApiModelProperty("今日订单数")
    private Integer todayOrders;

    @ApiModelProperty("本月订单数")
    private Integer monthOrders;

    // 比较数据
    @ApiModelProperty("今日销售额变化百分比")
    private Double todaySalesChange;

    @ApiModelProperty("本月销售额变化百分比")
    private Double monthSalesChange;

    @ApiModelProperty("低库存商品变化百分比")
    private Double lowStockChange;

    @ApiModelProperty("待审核订单变化百分比")
    private Double pendingOrdersChange;

    @ApiModelProperty("商品总数")
    private Integer totalProducts;

    @ApiModelProperty("低库存商品数")
    private Integer lowStockProducts;

    @ApiModelProperty("待审核采购订单数")
    private Integer pendingPurchaseOrders;

    @ApiModelProperty("供应商总数")
    private Integer totalSuppliers;

    // 图表数据
    @ApiModelProperty("销售趋势数据")
    private SalesTrendData salesTrend;

    @ApiModelProperty("分类占比数据")
    private List<CategoryRatioData> categoryRatio;

    @ApiModelProperty("库存预警数据")
    private List<InventoryWarningData> inventoryWarning;

    @ApiModelProperty("采购统计数据")
    private PurchaseStatisticsData purchaseStatistics;

    /**
     * 销售趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrendData {
        @ApiModelProperty("日期标签")
        private List<String> labels;
        
        @ApiModelProperty("销售额数据")
        private List<BigDecimal> salesData;
        
        @ApiModelProperty("订单数数据")
        private List<Integer> orderData;
    }

    /**
     * 分类占比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRatioData {
        @ApiModelProperty("分类名称")
        private String name;
        
        @ApiModelProperty("销售额")
        private BigDecimal value;
        
        @ApiModelProperty("占比百分比")
        private Double percentage;
    }

    /**
     * 库存预警数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryWarningData {
        @ApiModelProperty("商品名称")
        private String productName;
        
        @ApiModelProperty("当前库存")
        private Integer currentStock;
        
        @ApiModelProperty("预警库存")
        private Integer warningStock;
        
        @ApiModelProperty("预警级别：1-轻微，2-中等，3-严重")
        private Integer warningLevel;
    }

    /**
     * 采购统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseStatisticsData {
        @ApiModelProperty("本月采购金额")
        private BigDecimal monthPurchaseAmount;
        
        @ApiModelProperty("本月采购订单数")
        private Integer monthPurchaseOrders;
        
        @ApiModelProperty("供应商采购排行")
        private List<SupplierPurchaseData> supplierRanking;
        
        @ApiModelProperty("采购状态统计")
        private List<PurchaseStatusData> statusStatistics;
    }

    /**
     * 供应商采购数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierPurchaseData {
        @ApiModelProperty("供应商名称")
        private String supplierName;
        
        @ApiModelProperty("采购金额")
        private BigDecimal purchaseAmount;
        
        @ApiModelProperty("采购订单数")
        private Integer orderCount;
    }

    /**
     * 采购状态数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseStatusData {
        @ApiModelProperty("状态名称")
        private String statusName;
        
        @ApiModelProperty("订单数量")
        private Integer orderCount;
        
        @ApiModelProperty("状态值")
        private Integer statusValue;
    }
}




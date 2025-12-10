package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import com.supermarket.service.ActivityService;
import com.supermarket.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 最新动态服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final ProductMapper productMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<ActivityVO> getRecentActivities(Long userId, String type, Integer limit) {
        log.info("获取最新动态，用户ID: {}, 类型: {}, 限制数量: {}", userId, type, limit);
        
        // 获取用户角色
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            return new ArrayList<>();
        }
        
        Long roleId = user.getRoleId();
        log.info("用户角色ID: {}", roleId);
        
        List<ActivityVO> activities = new ArrayList<>();
        
        // 根据类型筛选
        if ("all".equals(type) || "purchase".equals(type)) {
            // 1. 获取采购订单动态
            activities.addAll(getPurchaseActivities(roleId));
        }
        
        if ("all".equals(type) || "sale".equals(type)) {
            // 2. 获取销售订单动态
            activities.addAll(getSaleActivities(roleId));
        }
        
        if ("all".equals(type) || "inventory".equals(type)) {
            // 3. 获取库存变动动态
            activities.addAll(getInventoryActivities(roleId));
        }
        
        // 按时间倒序排序并限制数量
        List<ActivityVO> result = activities.stream()
                .sorted(Comparator.comparing(ActivityVO::getTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("返回最新动态数量: {}", result.size());
        return result;
    }
    
    /**
     * 获取采购订单动态
     */
    private List<ActivityVO> getPurchaseActivities(Long roleId) {
        List<ActivityVO> activities = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
            
            // 管理员：查看待审核的订单
            // 采购员：查看已通过的订单（可以入库）
            if (roleId == 1) {
                // 管理员查看待审核
                wrapper.eq(PurchaseOrder::getStatus, 0);
            } else if (roleId == 2) {
                // 采购员查看已通过
                wrapper.eq(PurchaseOrder::getStatus, 1);
            } else {
                // 营业员不查看采购订单
                return activities;
            }
            
            wrapper.orderByDesc(PurchaseOrder::getCreateTime).last("LIMIT 5");
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);
            
            for (PurchaseOrder order : orders) {
                ActivityVO activity = new ActivityVO();
                activity.setId("purchase-" + order.getId());
                activity.setType("purchase");
                activity.setBusinessId(order.getId());
                activity.setOrderNo(order.getOrderNo());
                activity.setStatus(order.getStatus());
                
                if (order.getStatus() == 0) {
                    activity.setTitle("采购订单待审核");
                    activity.setContent(String.format("采购订单 %s 等待审核，总金额 ¥%.2f", 
                            order.getOrderNo(), order.getTotalAmount()));
                    activity.setBadge("待审核");
                    activity.setIcon("warning");
                } else if (order.getStatus() == 1) {
                    activity.setTitle("采购订单已通过");
                    activity.setContent(String.format("采购订单 %s 已审核通过，可以入库", 
                            order.getOrderNo()));
                    activity.setBadge("已通过");
                    activity.setIcon("success");
                }
                
                activity.setTime(formatTime(order.getCreateTime()));
                activities.add(activity);
            }
            
            log.info("获取采购动态数量: {}", activities.size());
        } catch (Exception e) {
            log.error("获取采购动态失败", e);
        }
        
        return activities;
    }
    
    /**
     * 获取销售订单动态
     */
    private List<ActivityVO> getSaleActivities(Long roleId) {
        List<ActivityVO> activities = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
            
            // 所有角色都可以查看最新的销售订单
            wrapper.orderByDesc(SaleOrder::getCreateTime).last("LIMIT 5");
            List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);
            
            for (SaleOrder order : orders) {
                ActivityVO activity = new ActivityVO();
                activity.setId("sale-" + order.getId());
                activity.setType("sale");
                activity.setBusinessId(order.getId());
                activity.setOrderNo(order.getOrderNo());
                activity.setStatus(order.getStatus());
                
                if (order.getStatus() == 0) {
                    activity.setTitle("新销售订单");
                    activity.setContent(String.format("销售订单 %s 已创建，总金额 ¥%.2f", 
                            order.getOrderNo(), order.getTotalAmount()));
                    activity.setBadge("待支付");
                    activity.setIcon("info");
                } else if (order.getStatus() == 1) {
                    activity.setTitle("销售订单已完成");
                    activity.setContent(String.format("销售订单 %s 已支付完成", 
                            order.getOrderNo()));
                    activity.setBadge("已完成");
                    activity.setIcon("success");
                }
                
                activity.setTime(formatTime(order.getCreateTime()));
                activities.add(activity);
            }
            
            log.info("获取销售动态数量: {}", activities.size());
        } catch (Exception e) {
            log.error("获取销售动态失败", e);
        }
        
        return activities;
    }
    
    /**
     * 获取库存变动动态
     */
    private List<ActivityVO> getInventoryActivities(Long roleId) {
        List<ActivityVO> activities = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
            
            // 所有角色都可以查看库存变动
            wrapper.orderByDesc(InventoryLog::getCreateTime).last("LIMIT 5");
            List<InventoryLog> logs = inventoryLogMapper.selectList(wrapper);
            
            for (InventoryLog log : logs) {
                Product product = productMapper.selectById(log.getProductId());
                if (product == null) continue;
                
                ActivityVO activity = new ActivityVO();
                activity.setId("inventory-" + log.getId());
                activity.setType("inventory");
                activity.setBusinessId(log.getProductId());
                activity.setProductName(product.getProductName());
                activity.setStatus(log.getChangeType());
                
                String changeTypeName = "";
                String icon = "info";
                if (log.getChangeType() == 1) {
                    changeTypeName = "入库";
                    icon = "success";
                } else if (log.getChangeType() == 2) {
                    changeTypeName = "出库";
                    icon = "warning";
                } else if (log.getChangeType() == 3) {
                    changeTypeName = "盘点调整";
                    icon = "info";
                }
                
                activity.setTitle("库存" + changeTypeName);
                activity.setContent(String.format("商品 %s %s %d 件，当前库存 %d", 
                        product.getProductName(), changeTypeName, 
                        Math.abs(log.getChangeQuantity()), log.getAfterQuantity()));
                activity.setBadge(changeTypeName);
                activity.setIcon(icon);
                activity.setTime(formatTime(log.getCreateTime()));
                
                activities.add(activity);
            }
            
            log.info("获取库存动态数量: {}", activities.size());
        } catch (Exception e) {
            log.error("获取库存动态失败", e);
        }
        
        return activities;
    }
    
    /**
     * 格式化时间为相对时间
     */
    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "刚刚";
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();
        
        if (seconds < 60) return "刚刚";
        if (seconds < 3600) return (seconds / 60) + " 分钟前";
        if (seconds < 86400) return (seconds / 3600) + " 小时前";
        if (seconds < 604800) return (seconds / 86400) + " 天前";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
}

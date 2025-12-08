package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.SaleOrderDTO;
import com.supermarket.dto.SaleOrderItemDTO;
import com.supermarket.entity.Customer;
import com.supermarket.entity.Product;
import com.supermarket.entity.SaleOrder;
import com.supermarket.entity.SaleOrderItem;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.*;
import com.supermarket.service.BusinessOperationNotificationService;
import com.supermarket.service.CacheService;
import com.supermarket.service.InventoryService;
import com.supermarket.service.SaleOrderService;
import com.supermarket.service.SystemNotificationService;
import com.supermarket.vo.SaleOrderItemVO;
import com.supermarket.vo.SaleOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder> implements SaleOrderService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;
    private final SysUserMapper userMapper;
    private final InventoryService inventoryService;
    private final SystemNotificationService notificationService;
    private final BusinessOperationNotificationService businessNotificationService;
    private final CacheService cacheService;
    
    private static final String CACHE_KEY_SALES_TODAY_PREFIX = "sales:today:";
    private static final String CACHE_KEY_SALES_MONTH_PREFIX = "sales:month:";

    @Override
    public Page<SaleOrderVO> getSaleOrderList(Integer current, Integer size, String orderNo, 
                                            String customerName, Integer status, 
                                            String startDate, String endDate) {
        Page<SaleOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(SaleOrder::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(SaleOrder::getStatus, status);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(SaleOrder::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(SaleOrder::getCreateTime, endDate + " 23:59:59");
        }
        
        wrapper.orderByDesc(SaleOrder::getCreateTime);
        Page<SaleOrder> saleOrderPage = page(page, wrapper);
        
        // 转换为VO
        Page<SaleOrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(saleOrderPage, voPage);
        
        List<SaleOrderVO> voList = saleOrderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public SaleOrderVO getSaleOrderDetail(Long id) {
        SaleOrder saleOrder = saleOrderMapper.selectById(id);
        if (saleOrder == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        SaleOrderVO vo = convertToVO(saleOrder);
        
        // 查询订单明细
        List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>()
                        .eq(SaleOrderItem::getOrderId, id)
        );
        
        List<SaleOrderItemVO> itemVOs = items.stream().map(item -> {
            SaleOrderItemVO itemVO = new SaleOrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            
            // 获取商品信息
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                itemVO.setProductName(product.getProductName());
            }
            
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSaleOrder(SaleOrderDTO dto, Long cashierId, Long operatorId) {
        // 生成订单编号
        String orderNo = generateOrderNo();
        
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleOrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            
            if (product.getStatus() == 0) {
                throw new BusinessException("商品已下架：" + product.getProductName());
            }
            
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        
        // 创建销售订单
        SaleOrder order = new SaleOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(dto.getCustomerId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付
        order.setCashierId(cashierId);
        
        saleOrderMapper.insert(order);
        
        // 创建订单明细并扣减库存
        for (SaleOrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            
            SaleOrderItem item = new SaleOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
            
            saleOrderItemMapper.insert(item);
            
            // 扣减库存
            inventoryService.outbound(itemDTO.getProductId(), itemDTO.getQuantity(), 
                    orderNo, "销售出库", cashierId);
        }
        
        // 清除销售统计缓存
        String today = LocalDate.now().toString();
        String month = YearMonth.now().toString();
        cacheService.evict(CACHE_KEY_SALES_TODAY_PREFIX + today);
        cacheService.evict(CACHE_KEY_SALES_MONTH_PREFIX + month);
        log.info("已清除销售统计缓存");
        
        // 发送销售订单创建通知
        log.info("=== 开始发送销售订单创建通知 ===");
        log.info("订单ID: {}, 操作人ID: {}, 订单号: {}, 金额: {}", 
                order.getId(), operatorId, order.getOrderNo(), order.getTotalAmount());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySaleOperation(
                operatorId, operatorRole, "CREATE", order.getOrderNo(), order.getId(), 
                order.getTotalAmount().doubleValue()
            );
            log.info("=== 销售订单创建通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送销售订单创建通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySaleOrder(Long id, Long operatorId) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能支付待支付状态的订单");
        }
        
        order.setStatus(1); // 已支付
        saleOrderMapper.updateById(order);
        
        // 发送支付通知
        log.info("=== 开始发送销售订单支付通知 ===");
        log.info("订单ID: {}, 操作人ID: {}, 订单号: {}, 金额: {}", 
                id, operatorId, order.getOrderNo(), order.getTotalAmount());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySaleOperation(
                operatorId, operatorRole, "PAY", order.getOrderNo(), order.getId(), 
                order.getTotalAmount().doubleValue()
            );
            log.info("=== 销售订单支付通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送销售订单支付通知失败 ===", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSaleOrder(Long id, String reason) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待支付状态的订单");
        }
        
        // 恢复库存
        List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>()
                        .eq(SaleOrderItem::getOrderId, id)
        );
        
        for (SaleOrderItem item : items) {
            inventoryService.inbound(item.getProductId(), item.getQuantity(), 
                    order.getOrderNo(), "销售取消退库", order.getCashierId());
        }
        
        order.setStatus(2); // 已取消
        saleOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOrder(Long id, Long operatorId) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() == 1) {
            throw new BusinessException("已支付的订单不能删除");
        }
        
        // 如果是待支付状态，需要恢复库存
        if (order.getStatus() == 0) {
            List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                    new LambdaQueryWrapper<SaleOrderItem>()
                            .eq(SaleOrderItem::getOrderId, id)
            );
            
            for (SaleOrderItem item : items) {
                inventoryService.inbound(item.getProductId(), item.getQuantity(), 
                        order.getOrderNo(), "销售删除退库", order.getCashierId());
            }
        }
        
        // 删除订单明细
        saleOrderItemMapper.delete(
                new LambdaQueryWrapper<SaleOrderItem>()
                        .eq(SaleOrderItem::getOrderId, id)
        );
        
        // 删除订单
        saleOrderMapper.deleteById(id);
        
        // 发送销售订单删除通知
        log.info("=== 开始发送销售订单删除通知 ===");
        log.info("订单ID: {}, 操作人ID: {}, 订单号: {}, 金额: {}", 
                id, operatorId, order.getOrderNo(), order.getTotalAmount());
        try {
            // 获取操作人角色
            String operatorRole = getUserRole(operatorId);
            log.info("操作人角色: {}", operatorRole);
            
            businessNotificationService.notifySaleOperation(
                operatorId, operatorRole, "DELETE", order.getOrderNo(), order.getId(), 
                order.getTotalAmount().doubleValue()
            );
            log.info("=== 销售订单删除通知发送完成 ===");
        } catch (Exception e) {
            log.error("=== 发送销售订单删除通知失败 ===", e);
        }
    }

    /**
     * 转换为VO
     */
    private SaleOrderVO convertToVO(SaleOrder saleOrder) {
        SaleOrderVO vo = new SaleOrderVO();
        BeanUtils.copyProperties(saleOrder, vo);
        
        // 设置客户名称
        if (saleOrder.getCustomerId() != null) {
            Customer customer = customerMapper.selectById(saleOrder.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getCustomerName());
            }
        }
        
        // 设置收银员名称
        if (saleOrder.getCashierId() != null) {
            SysUser cashier = userMapper.selectById(saleOrder.getCashierId());
            if (cashier != null) {
                vo.setCashierName(cashier.getRealName());
            }
        }
        
        // 状态描述由前端处理
        
        return vo;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SO" + dateStr + String.format("%04d", (int)(Math.random() * 10000));
    }


    /**
     * 获取用户角色编码
     */
    private String getUserRole(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            return "UNKNOWN";
        }
        
        Long roleId = user.getRoleId();
        switch (roleId.intValue()) {
            case 1: return "ADMIN";
            case 2: return "PURCHASER";
            case 3: return "CASHIER";
            default: return "UNKNOWN";
        }
    }
}

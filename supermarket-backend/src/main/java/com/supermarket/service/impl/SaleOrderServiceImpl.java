package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.SaleOrderDTO;
import com.supermarket.dto.SaleOrderItemDTO;
import com.supermarket.entity.Product;
import com.supermarket.entity.SaleOrder;
import com.supermarket.entity.SaleOrderItem;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.SaleOrderItemMapper;
import com.supermarket.mapper.SaleOrderMapper;
import com.supermarket.service.InventoryService;
import com.supermarket.service.SaleOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 销售订单服务实现
 */
@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder> implements SaleOrderService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;

    @Override
    public Page<SaleOrder> getSaleOrderList(Integer current, Integer size, String orderNo,
                                             Long customerId, Integer status) {
        Page<SaleOrder> page = new Page<>(current, size);
        return lambdaQuery()
                .like(orderNo != null, SaleOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleOrder::getCustomerId, customerId)
                .eq(status != null, SaleOrder::getStatus, status)
                .orderByDesc(SaleOrder::getCreateTime)
                .page(page);
    }

    @Override
    public SaleOrder getSaleOrderDetail(Long id) {
        return saleOrderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSaleOrder(SaleOrderDTO dto, Long cashierId) {
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
        
        // 创建订单明细
        for (SaleOrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            
            SaleOrderItem item = new SaleOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
            saleOrderItemMapper.insert(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySaleOrder(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能支付待支付状态的订单");
        }
        
        // 查询订单明细并扣减库存
        saleOrderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrderItem>()
                        .eq(SaleOrderItem::getOrderId, id)
        ).forEach(item -> {
            inventoryService.outbound(item.getProductId(), item.getQuantity(),
                    order.getOrderNo(), "销售出库", order.getCashierId());
        });
        
        // 更新订单状态
        order.setStatus(1); // 已支付
        saleOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSaleOrder(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待支付状态的订单");
        }
        
        order.setStatus(2); // 已取消
        saleOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOrder(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        
        if (order.getStatus() == 1) {
            throw new BusinessException("已支付的订单不能删除");
        }
        
        // 删除订单明细
        saleOrderItemMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrderItem>()
                        .eq(SaleOrderItem::getOrderId, id)
        );
        
        // 删除订单
        saleOrderMapper.deleteById(id);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SO" + dateStr + String.format("%04d", (int)(Math.random() * 10000));
    }
}


package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.PurchaseOrderDTO;
import com.supermarket.dto.PurchaseOrderItemDTO;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.PurchaseOrderItem;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.PurchaseOrderItemMapper;
import com.supermarket.mapper.PurchaseOrderMapper;
import com.supermarket.service.InventoryService;
import com.supermarket.service.PurchaseOrderService;
import com.supermarket.vo.PurchaseOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 采购订单服务实现
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> 
        implements PurchaseOrderService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;

    @Override
    public Page<PurchaseOrderVO> getPurchaseOrderList(Integer current, Integer size, String orderNo,
                                                        Long supplierId, Integer status, Long applicantId) {
        Page<PurchaseOrderVO> page = new Page<>(current, size);
        List<PurchaseOrderVO> list = purchaseOrderMapper.selectPurchaseOrderVOList(orderNo, supplierId, status, applicantId);
        
        // 设置状态描述
        list.forEach(this::setStatusDesc);
        
        // 计算分页
        int total = list.size();
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        
        List<PurchaseOrderVO> pageList = list.subList(start, end);
        page.setRecords(pageList);
        page.setTotal(total);
        
        return page;
    }

    @Override
    public PurchaseOrderVO getPurchaseOrderDetail(Long id) {
        PurchaseOrderVO vo = purchaseOrderMapper.selectPurchaseOrderVOById(id);
        if (vo == null) {
            throw new BusinessException("采购订单不存在");
        }
        
        // 查询订单明细
        vo.setItems(purchaseOrderItemMapper.selectItemVOListByOrderId(id));
        
        // 设置状态描述
        setStatusDesc(vo);
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseOrder(PurchaseOrderDTO dto, Long applicantId) {
        // 生成订单编号
        String orderNo = generateOrderNo();
        
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO item : dto.getItems()) {
            // 优先使用purchasePrice，如果为空则使用unitPrice
            BigDecimal price = item.getPurchasePrice() != null ? item.getPurchasePrice() : item.getUnitPrice();
            if (price == null) {
                price = BigDecimal.ZERO;
            }
            BigDecimal itemTotal = price.multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        
        // 创建采购订单
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setSupplierId(dto.getSupplierId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待审核
        order.setApplicantId(applicantId);
        // createTime会自动填充，作为采购时间
        
        purchaseOrderMapper.insert(order);
        
        // 创建订单明细
        for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            
            // 优先使用purchasePrice，如果为空则使用unitPrice
            BigDecimal price = itemDTO.getPurchasePrice() != null ? itemDTO.getPurchasePrice() : itemDTO.getUnitPrice();
            if (price == null) {
                price = BigDecimal.ZERO;
            }
            
            item.setUnitPrice(price);
            item.setTotalPrice(price.multiply(new BigDecimal(itemDTO.getQuantity())));
            purchaseOrderItemMapper.insert(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPurchaseOrder(Long id, Integer status, String auditRemark, Long auditorId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能审核待审核状态的订单");
        }
        
        if (status != 1 && status != 2) {
            throw new BusinessException("审核状态不正确");
        }
        
        order.setStatus(status);
        order.setAuditorId(auditorId);
        order.setAuditTime(LocalDateTime.now());
        order.setAuditRemark(auditRemark);
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmInbound(Long id, Long operatorId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        
        if (order.getStatus() != 1) {
            throw new BusinessException("只能对已通过审核的订单进行入库操作");
        }
        
        // 查询订单明细
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, id)
        );
        
        // 更新库存
        for (PurchaseOrderItem item : items) {
            inventoryService.inbound(item.getProductId(), item.getQuantity(), 
                    order.getOrderNo(), "采购入库", operatorId);
        }
        
        // 更新订单状态和入库时间
        order.setStatus(3); // 已入库
        order.setInboundTime(LocalDateTime.now()); // 设置入库时间
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        
        if (order.getStatus() == 3) {
            throw new BusinessException("已入库的订单不能删除");
        }
        
        // 删除订单明细
        purchaseOrderItemMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, id)
        );
        
        // 删除订单
        purchaseOrderMapper.deleteById(id);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PO" + dateStr + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 设置状态描述
     */
    private void setStatusDesc(PurchaseOrderVO vo) {
        switch (vo.getStatus()) {
            case 0:
                vo.setStatusDesc("待审核");
                break;
            case 1:
                vo.setStatusDesc("待入库");
                break;
            case 2:
                vo.setStatusDesc("已拒绝");
                break;
            case 3:
                vo.setStatusDesc("已入库");
                break;
            default:
                vo.setStatusDesc("未知");
        }
    }
}


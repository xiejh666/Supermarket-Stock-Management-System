import request from '@/utils/request'

/**
 * 获取采购订单列表
 */
export function getPurchaseOrderList(params) {
  return request({
    url: '/purchase/orders/list',
    method: 'get',
    params
  })
}

/**
 * 获取采购订单详情
 */
export function getPurchaseOrderDetail(id) {
  return request({
    url: `/purchase/orders/${id}`,
    method: 'get'
  })
}

/**
 * 创建采购订单
 */
export function createPurchaseOrder(data, applicantId) {
  return request({
    url: '/purchase/orders',
    method: 'post',
    data,
    params: { applicantId }
  })
}

/**
 * 审核采购订单
 */
export function auditPurchaseOrder(id, status, auditRemark, auditorId) {
  return request({
    url: `/purchase/orders/${id}/audit`,
    method: 'put',
    params: { status, auditRemark, auditorId }
  })
}

/**
 * 确认入库
 */
export function confirmInbound(id, operatorId) {
  return request({
    url: `/purchase/orders/${id}/inbound`,
    method: 'put',
    params: { operatorId }
  })
}

/**
 * 删除采购订单
 */
export function deletePurchaseOrder(id) {
  return request({
    url: `/purchase/orders/${id}`,
    method: 'delete'
  })
}

export default {
  getList: getPurchaseOrderList,
  getDetail: getPurchaseOrderDetail,
  create: createPurchaseOrder,
  audit: auditPurchaseOrder,
  confirm: confirmInbound,
  delete: deletePurchaseOrder
}


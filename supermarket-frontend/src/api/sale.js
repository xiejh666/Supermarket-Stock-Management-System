import request from '@/utils/request'

/**
 * 获取销售订单列表
 */
export function getSaleOrderList(params) {
  return request({
    url: '/sale/orders/list',
    method: 'get',
    params
  })
}

/**
 * 获取销售订单详情
 */
export function getSaleOrderDetail(id) {
  return request({
    url: `/sale/orders/${id}`,
    method: 'get'
  })
}

/**
 * 创建销售订单
 */
export function createSaleOrder(data, cashierId) {
  return request({
    url: '/sale/orders',
    method: 'post',
    data,
    params: { cashierId }
  })
}

/**
 * 支付订单
 */
export function paySaleOrder(id) {
  return request({
    url: `/sale/orders/${id}/pay`,
    method: 'put'
  })
}

/**
 * 取消订单
 */
export function cancelSaleOrder(id, reason) {
  return request({
    url: `/sale/orders/${id}/cancel`,
    method: 'put',
    params: { reason }
  })
}

/**
 * 删除订单
 */
export function deleteSaleOrder(id) {
  return request({
    url: `/sale/orders/${id}`,
    method: 'delete'
  })
}

export default {
  getList: getSaleOrderList,
  getDetail: getSaleOrderDetail,
  create: createSaleOrder,
  pay: paySaleOrder,
  confirm: paySaleOrder,
  cancel: cancelSaleOrder,
  delete: deleteSaleOrder
}


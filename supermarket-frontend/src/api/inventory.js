import request from '@/utils/request'

/**
 * 获取库存列表
 */
export function getInventoryList(params) {
  return request({
    url: '/inventory/list',
    method: 'get',
    params
  })
}

/**
 * 入库
 */
export function inbound(params) {
  return request({
    url: '/inventory/inbound',
    method: 'post',
    params
  })
}

/**
 * 出库
 */
export function outbound(params) {
  return request({
    url: '/inventory/outbound',
    method: 'post',
    params
  })
}

/**
 * 盘点调整
 */
export function adjustInventory(params) {
  return request({
    url: '/inventory/adjust',
    method: 'put',
    params
  })
}

/**
 * 获取库存变动历史
 */
export function getInventoryLogs(productId) {
  return request({
    url: `/inventory/logs/${productId}`,
    method: 'get'
  })
}

export default {
  getList: getInventoryList,
  inbound: inbound,
  outbound: outbound,
  adjust: adjustInventory,
  getLogs: getInventoryLogs
}


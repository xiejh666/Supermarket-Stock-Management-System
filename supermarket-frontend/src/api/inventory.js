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
 * 盘点调整
 */
export function adjustInventory(data) {
  return request({
    url: '/inventory/adjust',
    method: 'post',
    data
  })
}

/**
 * 获取库存统计
 */
export function getInventoryStats() {
  return request({
    url: '/inventory/stats',
    method: 'get'
  })
}

export default {
  getList: getInventoryList,
  adjust: adjustInventory,
  getStats: getInventoryStats
}


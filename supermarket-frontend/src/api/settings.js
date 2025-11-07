import request from '@/utils/request'

/**
 * 获取系统设置
 */
export function getSettings() {
  return request({
    url: '/settings',
    method: 'get'
  })
}

/**
 * 保存系统设置
 */
export function saveSettings(data) {
  return request({
    url: '/settings',
    method: 'post',
    data
  })
}

/**
 * 导出商品数据
 */
export function exportProducts() {
  return request({
    url: '/settings/export/products',
    method: 'get',
    responseType: 'blob'
  })
}

export default {
  getSettings,
  saveSettings,
  exportProducts
}


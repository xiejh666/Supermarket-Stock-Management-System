import request from '@/utils/request'

/**
 * 获取首页统计数据
 */
export function getDashboardStatistics() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}




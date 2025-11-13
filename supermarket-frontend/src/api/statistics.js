import request from '@/utils/request'

/**
 * 获取首页统计数据
 */
export function getDashboardStatistics(period = 'week') {
  return request({
    url: '/statistics/dashboard',
    method: 'get',
    params: { period }
  })
}




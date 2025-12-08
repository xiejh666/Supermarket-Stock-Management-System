import request from '@/utils/request'

/**
 * 获取最新动态
 */
export function getRecentActivities(params) {
  return request({
    url: '/activities/recent',
    method: 'get',
    params
  })
}

export default {
  getRecent: getRecentActivities
}

import request from '@/utils/request'

/**
 * 获取通知列表
 */
export function getNotifications(params) {
  return request({
    url: '/notifications/list',
    method: 'get',
    params
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount(userId) {
  return request({
    url: '/notifications/unread-count',
    method: 'get',
    params: { userId }
  })
}

/**
 * 标记通知为已读
 */
export function markAsRead(id, userId) {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put',
    params: { userId }
  })
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead(userId) {
  return request({
    url: '/notifications/read-all',
    method: 'put',
    params: { userId }
  })
}

/**
 * 获取最新动态
 */
export function getRecentActivities(userId, limit = 10) {
  return request({
    url: '/notifications/recent-activities',
    method: 'get',
    params: { userId, limit }
  })
}

/**
 * 删除通知
 */
export function deleteNotification(id, userId) {
  return request({
    url: `/notifications/${id}`,
    method: 'delete',
    params: { userId }
  })
}

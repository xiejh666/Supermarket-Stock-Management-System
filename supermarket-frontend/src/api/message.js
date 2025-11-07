import request from '@/utils/request'

/**
 * 获取消息列表
 * @param {Boolean} onlyUnread 是否只查询未读消息
 * @returns {Promise}
 */
export function getMessageList(onlyUnread = false) {
  return request({
    url: '/messages/list',
    method: 'get',
    params: { onlyUnread }
  })
}

/**
 * 获取未读消息数量
 * @returns {Promise}
 */
export function getUnreadCount() {
  return request({
    url: '/messages/unread-count',
    method: 'get'
  })
}

/**
 * 标记消息为已读
 * @param {Number} id 消息ID
 * @returns {Promise}
 */
export function markMessageAsRead(id) {
  return request({
    url: `/messages/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记所有消息为已读
 * @returns {Promise}
 */
export function markAllMessagesAsRead() {
  return request({
    url: '/messages/read-all',
    method: 'put'
  })
}

/**
 * 删除消息
 * @param {Number} id 消息ID
 * @returns {Promise}
 */
export function deleteMessage(id) {
  return request({
    url: `/messages/${id}`,
    method: 'delete'
  })
}

/**
 * 清空所有消息
 * @returns {Promise}
 */
export function clearAllMessages() {
  return request({
    url: '/messages/clear-all',
    method: 'delete'
  })
}

// 默认导出
export default {
  getList: getMessageList,
  getUnreadCount,
  markAsRead: markMessageAsRead,
  markAllAsRead: markAllMessagesAsRead,
  delete: deleteMessage,
  clearAll: clearAllMessages
}


import request from '@/utils/request'

/**
 * 获取所有客户列表
 */
export function getAllCustomers() {
  return request({
    url: '/customers/all',
    method: 'get'
  })
}

/**
 * 分页查询客户列表
 */
export function getCustomerList(params) {
  return request({
    url: '/customers/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询客户详情
 */
export function getCustomerById(id) {
  return request({
    url: `/customers/${id}`,
    method: 'get'
  })
}

/**
 * 新增客户
 */
export function createCustomer(data, operatorId) {
  return request({
    url: '/customers',
    method: 'post',
    data,
    params: { operatorId }
  })
}

/**
 * 更新客户
 */
export function updateCustomer(data, operatorId) {
  return request({
    url: '/customers',
    method: 'put',
    data,
    params: { operatorId }
  })
}

/**
 * 删除客户
 */
export function deleteCustomer(id, operatorId) {
  return request({
    url: `/customers/${id}`,
    method: 'delete',
    params: { operatorId }
  })
}

export default {
  getAll: getAllCustomers,
  getList: getCustomerList,
  getById: getCustomerById,
  create: createCustomer,
  update: updateCustomer,
  delete: deleteCustomer
}

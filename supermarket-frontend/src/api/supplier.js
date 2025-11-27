import request from '@/utils/request'

/**
 * 获取供应商列表
 */
export function getSupplierList(params) {
  return request({
    url: '/suppliers/list',
    method: 'get',
    params
  })
}

/**
 * 获取启用的供应商
 */
export function getEnabledSuppliers() {
  return request({
    url: '/suppliers/enabled',
    method: 'get'
  })
}

/**
 * 创建供应商
 */
export function createSupplier(data, operatorId) {
  return request({
    url: '/suppliers',
    method: 'post',
    data,
    params: { operatorId }
  })
}

/**
 * 更新供应商
 */
export function updateSupplier(data, operatorId) {
  return request({
    url: '/suppliers',
    method: 'put',
    data,
    params: { operatorId }
  })
}

/**
 * 删除供应商
 */
export function deleteSupplier(id, operatorId) {
  return request({
    url: `/suppliers/${id}`,
    method: 'delete',
    params: { operatorId }
  })
}

/**
 * 获取所有供应商（不分页）
 */
export function getAllSuppliers() {
  return request({
    url: '/suppliers/all',
    method: 'get'
  })
}

export default {
  getList: getSupplierList,
  getEnabled: getEnabledSuppliers,
  getAll: getAllSuppliers,
  create: createSupplier,
  update: updateSupplier,
  delete: deleteSupplier
}


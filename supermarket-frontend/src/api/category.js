import request from '@/utils/request'

/**
 * 获取分类列表
 */
export function getCategoryList(params) {
  return request({
    url: '/categories/list',
    method: 'get',
    params
  })
}

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}

/**
 * 创建分类
 */
export function createCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(data) {
  return request({
    url: '/categories',
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有分类（不分页）
 */
export function getAllCategories() {
  return request({
    url: '/categories/all',
    method: 'get'
  })
}

export default {
  getList: getCategoryList,
  getTree: getCategoryTree,
  getAll: getAllCategories,
  create: createCategory,
  update: updateCategory,
  delete: deleteCategory
}


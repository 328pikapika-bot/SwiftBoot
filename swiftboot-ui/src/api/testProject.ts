import request from '@/utils/request'

/**
 * 示例_项目表列表
 */
export function listTestProject(query: any) {
  return request({
    url: '/test/testProject/list',
    method: 'get',
    params: query
  })
}

/**
 * 示例_项目表详情
 */
export function getTestProject(id: number) {
  return request({
    url: '/test/testProject/' + id,
    method: 'get'
  })
}

/**
 * 新增示例_项目表
 */
export function addTestProject(data: any) {
  return request({
    url: '/test/testProject',
    method: 'post',
    data
  })
}

/**
 * 修改示例_项目表
 */
export function updateTestProject(data: any) {
  return request({
    url: '/test/testProject',
    method: 'put',
    data
  })
}

/**
 * 删除示例_项目表
 */
export function deleteTestProject(ids: string) {
  return request({
    url: '/test/testProject/' + ids,
    method: 'delete'
  })
}
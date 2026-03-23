import request from '@/utils/request'

/**
 * 项目示例列表
 */
export function listTestProject(query: any) {
  return request({
    url: '/test/testProject/list',
    method: 'get',
    params: query
  })
}

/**
 * 项目示例详情
 */
export function getTestProject(id: number) {
  return request({
    url: '/test/testProject/' + id,
    method: 'get'
  })
}

/**
 * 新增项目示例
 */
export function addTestProject(data: any) {
  return request({
    url: '/test/testProject',
    method: 'post',
    data
  })
}

/**
 * 修改项目示例
 */
export function updateTestProject(data: any) {
  return request({
    url: '/test/testProject',
    method: 'put',
    data
  })
}

/**
 * 删除项目示例
 */
export function deleteTestProject(ids: string) {
  return request({
    url: '/test/testProject/' + ids,
    method: 'delete'
  })
}

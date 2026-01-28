import request from '@/utils/request'

/**
 * 测试学生表列表
 */
export function listTestStudent(query: any) {
  return request({
    url: '/student/testStudent/list',
    method: 'get',
    params: query
  })
}

/**
 * 测试学生表详情
 */
export function getTestStudent(id: number) {
  return request({
    url: '/student/testStudent/' + id,
    method: 'get'
  })
}

/**
 * 新增测试学生表
 */
export function addTestStudent(data: any) {
  return request({
    url: '/student/testStudent',
    method: 'post',
    data
  })
}

/**
 * 修改测试学生表
 */
export function updateTestStudent(data: any) {
  return request({
    url: '/student/testStudent',
    method: 'put',
    data
  })
}

/**
 * 删除测试学生表
 */
export function deleteTestStudent(ids: string) {
  return request({
    url: '/student/testStudent/' + ids,
    method: 'delete'
  })
}

import request from '@/utils/request'

/**
 * 学生示例列表
 */
export function listTestStudent(query: any) {
  return request({
    url: '/student/testStudent/list',
    method: 'get',
    params: query
  })
}

/**
 * 学生示例详情
 */
export function getTestStudent(id: number) {
  return request({
    url: '/student/testStudent/' + id,
    method: 'get'
  })
}

/**
 * 新增学生示例
 */
export function addTestStudent(data: any) {
  return request({
    url: '/student/testStudent',
    method: 'post',
    data
  })
}

/**
 * 修改学生示例
 */
export function updateTestStudent(data: any) {
  return request({
    url: '/student/testStudent',
    method: 'put',
    data
  })
}

/**
 * 删除学生示例
 */
export function deleteTestStudent(ids: string) {
  return request({
    url: '/student/testStudent/' + ids,
    method: 'delete'
  })
}

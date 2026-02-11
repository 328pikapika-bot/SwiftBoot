import request from '@/utils/request'

export function list(query: any) {
  return request({
    url: '/monitor/operlog/list',
    method: 'get',
    params: query
  })
}

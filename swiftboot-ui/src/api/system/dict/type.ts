import request from '@/utils/request'

// 查询字典类型列表
export function listType(query: any) {
  return request({
    url: '/system/dict/type/list',
    method: 'get',
    params: query
  })
}

// 查询字典类型详细
export function getType(dictId: number) {
  return request({
    url: '/system/dict/type/' + dictId,
    method: 'get'
  })
}

// 获取字典选择框列表
export function optionselect() {
  return request({
    url: '/system/dict/type/optionselect',
    method: 'get'
  })
}

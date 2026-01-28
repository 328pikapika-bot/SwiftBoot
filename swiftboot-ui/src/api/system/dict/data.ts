import request from '@/utils/request'

// 根据字典类型查询字典数据信息
export function getDicts(dictType: string) {
  return request({
    url: '/system/dict/data/type/' + dictType,
    method: 'get'
  })
}

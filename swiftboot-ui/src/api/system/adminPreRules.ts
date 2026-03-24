import request, { ApiResponse } from '@/utils/request'

export interface AdminPreRuleItem {
  id: string
  ruleName: string
  ruleContent: string
  enabled: boolean
  priority: number
}

export interface AdminPreRuleConfig {
  enabled: boolean
  interceptionMessage: string
  rules: AdminPreRuleItem[]
  maxRules: number
  maxRuleLength: number
}

export interface AdminPreRuleConfigPayload {
  enabled: boolean
  interceptionMessage: string
  rules: AdminPreRuleItem[]
}

export function getAdminPreRuleConfig(): Promise<ApiResponse<AdminPreRuleConfig>> {
  return request({
    url: '/system/ai/admin-pre-rules',
    method: 'get'
  })
}

export function updateAdminPreRuleConfig(data: AdminPreRuleConfigPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/admin-pre-rules',
    method: 'put',
    data
  })
}

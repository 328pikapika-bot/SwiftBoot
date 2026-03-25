import request, { ApiResponse } from '@/utils/request'

export interface AiBlockCategory {
  id: number
  dictDataId?: number
  categoryName: string
  categoryCode: string
  status: number
  sort: number
  remark?: string
  wordCount: number
  previewWords: string[]
}

export interface AiBlockWord {
  id: number
  categoryId: number
  categoryName: string
  wordText: string
  status: number
  sort: number
  remark?: string
}

export interface AiBlockOverview {
  enabledCategoryCount: number
  enabledWordCount: number
  totalCategoryCount: number
  totalWordCount: number
  categories: AiBlockCategory[]
  words: AiBlockWord[]
}

export interface AiBlockCategoryPayload {
  id?: number
  dictDataId?: number
  categoryName: string
  categoryCode: string
  status: number
  sort: number
  remark?: string
}

export interface AiBlockWordPayload {
  id?: number
  categoryId: number
  wordText: string
  status: number
  sort: number
  remark?: string
}

export interface AiBlockWordBatchPayload {
  categoryId: number
  wordLines: string
  remark?: string
}

export function getAiBlockOverview(): Promise<ApiResponse<AiBlockOverview>> {
  return request({
    url: '/system/ai/block-words/overview',
    method: 'get'
  })
}

export function createAiBlockCategory(data: AiBlockCategoryPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/block-words/categories',
    method: 'post',
    data
  })
}

export function updateAiBlockCategory(data: AiBlockCategoryPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/block-words/categories',
    method: 'put',
    data
  })
}

export function deleteAiBlockCategory(categoryId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/system/ai/block-words/categories/${categoryId}`,
    method: 'delete'
  })
}

export function createAiBlockWord(data: AiBlockWordPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/block-words/words',
    method: 'post',
    data
  })
}

export function updateAiBlockWord(data: AiBlockWordPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/block-words/words',
    method: 'put',
    data
  })
}

export function batchCreateAiBlockWords(data: AiBlockWordBatchPayload): Promise<ApiResponse<void>> {
  return request({
    url: '/system/ai/block-words/words/batch',
    method: 'post',
    data
  })
}

export function deleteAiBlockWord(wordId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/system/ai/block-words/words/${wordId}`,
    method: 'delete'
  })
}

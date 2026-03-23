import request, { ApiResponse, PageResult } from '@/utils/request'

export interface TestProject {
  id?: number
  projectName: string
  projectCode: string
  projectType?: number
  managerId?: number
  managerName?: string
  deptId?: number
  startDate?: string
  endDate?: string
  budget?: number | string
  progress?: number
  status?: number
  priority?: number
  description?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface TestProjectQuery {
  pageNum?: number
  pageSize?: number
  projectName?: string
  projectCode?: string
  managerName?: string
  projectType?: number
  status?: number
  priority?: number
}

export interface TestProjectImportResult {
  successCount: number
  updateCount: number
  failureCount: number
  failureMessages: string[]
}

export function listTestProject(query: TestProjectQuery): Promise<ApiResponse<PageResult<TestProject>>> {
  return request({
    url: '/test/testProject/list',
    method: 'get',
    params: query
  })
}

export function getTestProject(id: number): Promise<ApiResponse<TestProject>> {
  return request({
    url: `/test/testProject/${id}`,
    method: 'get'
  })
}

export function addTestProject(data: TestProject): Promise<ApiResponse<TestProject>> {
  return request({
    url: '/test/testProject',
    method: 'post',
    data
  })
}

export function updateTestProject(data: TestProject): Promise<ApiResponse<TestProject>> {
  return request({
    url: '/test/testProject',
    method: 'put',
    data
  })
}

export function deleteTestProject(ids: string): Promise<ApiResponse<void>> {
  return request({
    url: `/test/testProject/${ids}`,
    method: 'delete'
  })
}

export function exportTestProject(params: Omit<TestProjectQuery, 'pageNum' | 'pageSize'> & { ids?: string }): Promise<Blob> {
  return request({
    url: '/test/testProject/export',
    method: 'get',
    params,
    responseType: 'blob'
  } as any)
}

export function downloadImportTemplate(): Promise<Blob> {
  return request({
    url: '/test/testProject/import-template',
    method: 'get',
    responseType: 'blob'
  } as any)
}

export function importTestProject(file: File, updateSupport = false): Promise<ApiResponse<TestProjectImportResult>> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', String(updateSupport))

  return request({
    url: '/test/testProject/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

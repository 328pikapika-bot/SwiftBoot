import request, { ApiResponse } from '@/utils/request'

export interface SysJob {
  jobId?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  misfirePolicy?: number
  status?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface JobQuery {
  pageNum?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: number
}

/**
 * 获取定时任务列表
 */
export function listJob(query?: JobQuery): Promise<ApiResponse<SysJob[]>> {
  return request({
    url: '/monitor/job/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取定时任务详情
 */
export function getJob(jobId: number): Promise<ApiResponse<SysJob>> {
  return request({
    url: '/monitor/job/' + jobId,
    method: 'get'
  })
}

/**
 * 新增定时任务
 */
export function addJob(data: SysJob): Promise<ApiResponse<void>> {
  return request({
    url: '/monitor/job',
    method: 'post',
    data
  })
}

/**
 * 修改定时任务
 */
export function updateJob(data: SysJob): Promise<ApiResponse<void>> {
  return request({
    url: '/monitor/job',
    method: 'put',
    data
  })
}

/**
 * 删除定时任务
 */
export function deleteJob(jobIds: string): Promise<ApiResponse<void>> {
  return request({
    url: '/monitor/job/' + jobIds,
    method: 'delete'
  })
}

/**
 * 修改定时任务状态
 */
export function changeJobStatus(data: { jobId: number; status: number }): Promise<ApiResponse<void>> {
  return request({
    url: '/monitor/job/changeStatus',
    method: 'put',
    data
  })
}

/**
 * 立即执行定时任务
 */
export function runJob(data: { jobId: number }): Promise<ApiResponse<void>> {
  return request({
    url: '/monitor/job/run',
    method: 'put',
    data
  })
}

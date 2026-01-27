<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="模块名称">
          <el-input v-model="queryParams.title" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="异常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>操作日志列表</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button type="danger" @click="handleClean">
              <el-icon><Delete /></el-icon>清空
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column label="日志ID" prop="id" min-width="180" />
        <el-table-column label="模块" prop="title" min-width="120" />
        <el-table-column label="操作类型" prop="businessType" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ operTypeMap[row.businessType] || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" prop="requestMethod" min-width="100" align="center" />
        <el-table-column label="操作人员" prop="operName" min-width="120" />
        <el-table-column label="主机" prop="operIp" min-width="140" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" prop="costTime" min-width="100" align="center">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operTime" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="操作日志详情"
      width="30%"
      :close-on-click-modal="false"
      class="detail-dialog"
    >
      <div class="detail-content">
        <!-- 基础信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基础信息</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="5">
              <div class="info-item">
                <div class="label">模块名称</div>
                <div class="value">{{ detailData.title || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="5">
              <div class="info-item">
                <div class="label">操作类型</div>
                <div class="value">
                  <el-tag>{{ operTypeMap[detailData.businessType] || '其他' }}</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="5">
              <div class="info-item">
                <div class="label">请求方式</div>
                <div class="value">
                  <el-tag type="primary">{{ detailData.requestMethod || '-' }}</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="5">
            <div class="info-item">
                <div class="label">操作状态</div>
                <div class="value">
                  <el-tag :type="detailData.status === 0 ? 'success' : 'danger'">
                    {{ detailData.status === 0 ? '正常' : '异常' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="info-item">
                <div class="label">耗时</div>
                <div class="value">
                  <span class="cost-time">{{ detailData.costTime || 0 }}</span> ms
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <div class="label">主机地址</div>
                <div class="value">{{ detailData.operIp || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <div class="label">操作时间</div>
                <div class="value">{{ detailData.operTime || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <div class="label">操作人员</div>
                <div class="value">{{ detailData.operName || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <div class="label">日志ID</div>
                <div class="value">{{ detailData.id || '-' }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 详细信息 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 请求信息 -->
          <el-tab-pane label="请求信息" name="request">
            <div class="tab-content">
              <div class="param-group">
                <div class="param-header">
                  <el-icon><Link /></el-icon>
                  <span>请求地址</span>
                  <el-button text type="primary" @click="copyText(detailData.operUrl)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
                <div class="param-value url-value">{{ detailData.operUrl || '-' }}</div>
              </div>

              <div class="param-group">
                <div class="param-header">
                  <el-icon><Setting /></el-icon>
                  <span>操作方法</span>
                  <el-button text type="primary" @click="copyText(detailData.method)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
                <div class="param-value method-value">{{ detailData.method || '-' }}</div>
              </div>

              <div class="param-group" v-if="detailData.operParam">
                <div class="param-header">
                  <el-icon><DocumentAdd /></el-icon>
                  <span>请求参数</span>
                  <el-button text type="primary" @click="copyText(detailData.operParam)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
                <div class="param-value json-value">
                  <pre>{{ formatJson(detailData.operParam) }}</pre>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 响应信息 -->
          <el-tab-pane label="响应信息" name="response">
            <div class="tab-content">
              <div class="param-group" v-if="detailData.jsonResult">
                <div class="param-header">
                  <el-icon><DocumentChecked /></el-icon>
                  <span>返回参数</span>
                  <el-button text type="primary" @click="copyText(detailData.jsonResult)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
                <div class="param-value json-value">
                  <pre>{{ formatJson(detailData.jsonResult) }}</pre>
                </div>
              </div>

              <div class="param-group" v-else>
                <div class="empty-state">
                  <el-icon size="48" class="empty-icon"><DocumentRemove /></el-icon>
                  <p>无返回数据</p>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 错误信息 -->
          <el-tab-pane label="错误信息" name="error" v-if="detailData.errorMsg">
            <div class="tab-content">
              <div class="param-group">
                <div class="param-header">
                  <el-icon class="error-icon"><WarningFilled /></el-icon>
                  <span>错误详情</span>
                  <el-button text type="danger" @click="copyText(detailData.errorMsg)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
                <div class="param-value error-value">
                  <pre>{{ detailData.errorMsg }}</pre>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  InfoFilled, Link, Setting, DocumentAdd, DocumentChecked, DocumentRemove,
  WarningFilled, CopyDocument
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const operTypeMap: Record<number, string> = {
  0: '其他',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '查询',
  5: '导出',
  6: '导入'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const detailVisible = ref(false)
const detailData = ref<any>({})
const activeTab = ref('request')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  operName: '',
  status: undefined as number | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/monitor/operlog/list', method: 'get', params: queryParams })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.status = undefined
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDetail = (row: any) => {
  detailData.value = row
  detailVisible.value = true
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/operlog/' + selectedIds.value.join(','), method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const handleClean = () => {
  ElMessageBox.confirm('确定清空所有操作日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/operlog/clean', method: 'delete' })
    ElMessage.success('清空成功')
    getList()
  })
}

// 复制文本到剪贴板
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    // 降级方案
    const input = document.createElement('input')
    input.value = text
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('已复制到剪贴板')
  }
}

// 格式化JSON字符串
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return ''
  try {
    const parsed = JSON.parse(jsonStr)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return jsonStr
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
}

// 详情弹窗样式
.detail-dialog {
  :deep(.el-dialog) {
    margin-top: 5vh !important;
    max-height: 90vh;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  .detail-content {
    .info-card {
      margin-bottom: 20px;

      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        color: #303133;

        .el-icon {
          color: #409eff;
        }
      }

      .info-item {
        margin-bottom: 16px;

        .label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .value {
          font-size: 14px;
          color: #303133;
          font-weight: 500;

          .cost-time {
            color: #e6a23c;
            font-weight: 600;
          }

          .el-tag {
            font-size: 12px;
          }
        }
      }
    }

    .detail-tabs {
      :deep(.el-tabs__header) {
        margin: 0 0 20px;
      }

      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }

      .tab-content {
        .param-group {
          margin-bottom: 24px;

          .param-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 12px;
            padding: 12px 16px;
            background: #f5f7fa;
            border-radius: 8px;
            border-left: 4px solid #409eff;

            .el-icon {
              color: #409eff;
              font-size: 16px;
            }

            span {
              font-weight: 600;
              color: #303133;
            }

            .el-button {
              margin-left: auto;
              font-size: 12px;
            }

            .error-icon {
              color: #f56c6c;
            }
          }

          .param-value {
            &.url-value {
              font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
              font-size: 13px;
              color: #409eff;
              word-break: break-all;
              background: #f0f9ff;
              padding: 12px;
              border-radius: 6px;
              border: 1px solid #e6f7ff;
            }

            &.method-value {
              font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
              font-size: 13px;
              color: #67c23a;
              background: #f0f9ff;
              padding: 12px;
              border-radius: 6px;
              border: 1px solid #e6f7ff;
            }

            &.json-value {
              max-height: 400px;
              overflow: auto;
              background: #f6f8fa;
              border: 1px solid #e1e4e8;
              border-radius: 6px;

              pre {
                margin: 0;
                padding: 16px;
                font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                font-size: 12px;
                line-height: 1.5;
                color: #24292e;
                white-space: pre-wrap;
                word-break: break-all;
              }
            }

            &.error-value {
              background: #fef0f0;
              border: 1px solid #fab1a0;
              border-radius: 6px;

              pre {
                margin: 0;
                padding: 16px;
                font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                font-size: 12px;
                line-height: 1.5;
                color: #f56c6c;
                white-space: pre-wrap;
                word-break: break-all;
              }
            }
          }

          .empty-state {
            text-align: center;
            padding: 40px 20px;
            color: #909399;

            .empty-icon {
              color: #c0c4cc;
              margin-bottom: 12px;
            }

            p {
              margin: 0;
              font-size: 14px;
            }
          }
        }
      }
    }
  }

  .dialog-footer {
    text-align: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .detail-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin-top: 2vh !important;
    }

    .info-card .el-row .el-col {
      margin-bottom: 12px;

      &:nth-child(2n) {
        padding-left: 10px;
      }

      &:nth-child(2n+1) {
        padding-right: 10px;
      }
    }
  }
}

// 滚动条样式
.param-value.json-value::-webkit-scrollbar,
.param-value.error-value::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.param-value.json-value::-webkit-scrollbar-track,
.param-value.error-value::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.param-value.json-value::-webkit-scrollbar-thumb,
.param-value.error-value::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.param-value.json-value::-webkit-scrollbar-thumb:hover,
.param-value.error-value::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

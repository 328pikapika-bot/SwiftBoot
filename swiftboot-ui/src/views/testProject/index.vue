<template>
  <div class="page-container project-page">
    <el-card shadow="never" class="search-card">
      <el-form ref="queryRef" :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="queryParams.projectCode" placeholder="请输入项目编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="项目经理" prop="managerName">
          <el-input v-model="queryParams.managerName" placeholder="请输入项目经理" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="项目类型" prop="projectType">
          <el-select v-model="queryParams.projectType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in projectTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in priorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <CrudActionToolbar :left-actions="toolbarActions" :summary="selectionSummary" @action="handleToolbarAction">
        <template #right>
          <div class="toolbar-summary">
            <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
            <el-tag effect="plain" :type="selectedIds.length ? 'success' : 'info'">已勾选 {{ selectedIds.length }} 条</el-tag>
          </div>
        </template>
      </CrudActionToolbar>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        row-key="id"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="项目名称" prop="projectName" min-width="180" show-overflow-tooltip />
        <el-table-column label="项目编号" prop="projectCode" min-width="130" />
        <el-table-column label="项目类型" prop="projectType" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ getProjectTypeLabel(row.projectType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项目经理" prop="managerName" min-width="120" />
        <el-table-column label="预算" prop="budget" min-width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.budget) }}
          </template>
        </el-table-column>
        <el-table-column label="进度" prop="progress" min-width="150">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.progress || 0)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" prop="priority" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" effect="plain">{{ getPriorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始日期" prop="startDate" width="110" />
        <el-table-column label="结束日期" prop="endDate" width="110" />
        <el-table-column label="更新时间" prop="updateTime" min-width="165" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="importVisible" title="导入示例项目" width="560px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #title>
          支持导入 `.xlsx` 文件。若勾选“覆盖已有项目”，系统会按项目编号更新现有数据。
        </template>
      </el-alert>
      <el-upload
        ref="importUploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :show-file-list="true"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到这里，或 <em>点击选择</em></div>
      </el-upload>
      <el-checkbox v-model="updateSupport" class="mt-4">覆盖已有项目</el-checkbox>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button @click="handleDownloadTemplate">下载模板</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImportSubmit">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="960px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目编号" prop="projectCode">
              <el-input v-model="formData.projectCode" placeholder="请输入项目编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-select v-model="formData.projectType" placeholder="请选择项目类型" style="width: 100%">
                <el-option v-for="item in projectTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目经理" prop="managerName">
              <el-input v-model="formData.managerName" placeholder="请输入项目经理" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="项目预算" prop="budget">
              <el-input-number v-model="formData.budget" :min="0" :precision="2" :step="1000" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="项目进度" prop="progress">
              <el-input-number v-model="formData.progress" :min="0" :max="100" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="项目状态" prop="status">
              <el-select v-model="formData.status" placeholder="请选择项目状态" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="formData.priority">
                <el-radio-button v-for="item in priorityOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目描述" prop="description">
              <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="attachment-section">
        <AttachmentManager biz-type="test:testProject" :biz-id="formData.id || null" title="项目附件" />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" @click="handleSubmit(false)">保存</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit(true)">保存并关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadFile, UploadInstance, UploadRawFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, EditPen, FolderAdd, Refresh, Search, Upload, UploadFilled } from '@element-plus/icons-vue'
import AttachmentManager from '@/components/AttachmentManager/index.vue'
import CrudActionToolbar, { type CrudActionItem } from '@/components/CrudActionToolbar/index.vue'
import {
  addTestProject,
  deleteTestProject,
  downloadImportTemplate,
  exportTestProject,
  getTestProject,
  importTestProject,
  listTestProject,
  type TestProject,
  type TestProjectImportResult,
  type TestProjectQuery,
  updateTestProject
} from '@/api/testProject'

const projectTypeOptions = [
  { label: '内部项目', value: 1 },
  { label: '外包项目', value: 2 },
  { label: '合作项目', value: 3 }
]

const statusOptions = [
  { label: '进行中', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已暂停', value: 2 },
  { label: '已取消', value: 3 }
]

const priorityOptions = [
  { label: '低', value: 1 },
  { label: '中', value: 2 },
  { label: '高', value: 3 }
]

const createEmptyForm = (): TestProject => ({
  id: undefined,
  projectName: '',
  projectCode: '',
  projectType: 1,
  managerName: '',
  startDate: '',
  endDate: '',
  budget: 0,
  progress: 0,
  status: 0,
  priority: 2,
  description: '',
  remark: ''
})

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const dialogTitle = ref('新增示例项目')
const total = ref(0)
const tableData = ref<TestProject[]>([])
const selectedIds = ref<number[]>([])
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const tableRef = ref()
const importUploadRef = ref<UploadInstance>()
const updateSupport = ref(false)
const importFile = ref<File | null>(null)

const queryParams = reactive<TestProjectQuery>({
  pageNum: 1,
  pageSize: 10,
  projectName: '',
  projectCode: '',
  managerName: '',
  projectType: undefined,
  status: undefined,
  priority: undefined
})

const formData = reactive<TestProject>(createEmptyForm())

const rules: FormRules<TestProject> = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  progress: [{ required: true, message: '请输入项目进度', trigger: 'blur' }]
}

const toolbarActions = computed<CrudActionItem[]>(() => [
  { key: 'add', label: '新增项目', type: 'primary', icon: FolderAdd, permission: 'test:testProject:add' },
  {
    key: 'edit',
    label: '编辑所选',
    icon: EditPen,
    disabled: selectedIds.value.length !== 1,
    permission: 'test:testProject:edit'
  },
  {
    key: 'remove',
    label: '批量删除',
    type: 'danger',
    icon: Delete,
    disabled: !selectedIds.value.length,
    permission: 'test:testProject:remove'
  },
  { key: 'import', label: '导入', icon: Upload, permission: 'test:testProject:import' },
  {
    key: 'exportSelected',
    label: '导出所选',
    icon: Download,
    disabled: !selectedIds.value.length,
    permission: 'test:testProject:export'
  },
  { key: 'exportCurrent', label: '导出当前条件', icon: Download, plain: true, permission: 'test:testProject:export' },
  { key: 'downloadTemplate', label: '下载模板', plain: true, permission: 'test:testProject:template' }
])

const selectionSummary = computed(() => (selectedIds.value.length ? `当前已勾选 ${selectedIds.value.length} 条记录` : '支持按勾选结果导出或批量删除'))

const getList = async () => {
  loading.value = true
  try {
    const { data } = await listTestProject(queryParams)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryRef.value?.resetFields()
  handleQuery()
}

const handleSelectionChange = (selection: TestProject[]) => {
  selectedIds.value = selection.map((item) => Number(item.id))
}

const handleToolbarAction = async (key: string) => {
  if (key === 'add') {
    handleAdd()
    return
  }
  if (key === 'edit') {
    const row = tableData.value.find((item) => item.id === selectedIds.value[0])
    if (row) {
      await handleEdit(row)
    }
    return
  }
  if (key === 'remove') {
    await handleBatchDelete()
    return
  }
  if (key === 'import') {
    importVisible.value = true
    return
  }
  if (key === 'exportSelected') {
    await handleExport(true)
    return
  }
  if (key === 'exportCurrent') {
    await handleExport(false)
    return
  }
  if (key === 'downloadTemplate') {
    await handleDownloadTemplate()
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增示例项目'
  dialogVisible.value = true
}

const handleEdit = async (row: TestProject) => {
  resetForm()
  const { data } = await getTestProject(Number(row.id))
  Object.assign(formData, createEmptyForm(), data)
  dialogTitle.value = '编辑示例项目'
  dialogVisible.value = true
}

const handleSubmit = async (closeAfterSave: boolean) => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      const { data } = await updateTestProject(formData)
      Object.assign(formData, createEmptyForm(), data)
      ElMessage.success('项目已更新')
      if (closeAfterSave) {
        dialogVisible.value = false
      }
    } else {
      const { data } = await addTestProject(formData)
      Object.assign(formData, createEmptyForm(), data)
      dialogTitle.value = '编辑示例项目'
      ElMessage.success(closeAfterSave ? '项目已创建' : '项目已创建，可继续上传附件')
      if (closeAfterSave) {
        dialogVisible.value = false
      }
    }
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: TestProject) => {
  await ElMessageBox.confirm(`确定删除项目【${row.projectName}】吗？`, '提示', { type: 'warning' })
  await deleteTestProject(String(row.id))
  ElMessage.success('删除成功')
  await getList()
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) {
    return
  }
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', { type: 'warning' })
  await deleteTestProject(selectedIds.value.join(','))
  ElMessage.success('批量删除成功')
  selectedIds.value = []
  await getList()
}

const handleImportFileChange = (file: UploadFile) => {
  importFile.value = file.raw || null
}

const handleImportFileRemove = () => {
  importFile.value = null
}

const handleImportSubmit = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择导入文件')
    return
  }
  importLoading.value = true
  try {
    const { data } = await importTestProject(importFile.value, updateSupport.value)
    showImportSummary(data)
    importVisible.value = false
    importFile.value = null
    updateSupport.value = false
    importUploadRef.value?.clearFiles()
    await getList()
  } finally {
    importLoading.value = false
  }
}

const handleExport = async (selectedOnly: boolean) => {
  const params = selectedOnly
    ? { ids: selectedIds.value.join(',') }
    : {
        projectName: queryParams.projectName || undefined,
        projectCode: queryParams.projectCode || undefined,
        managerName: queryParams.managerName || undefined,
        projectType: queryParams.projectType,
        status: queryParams.status,
        priority: queryParams.priority
      }
  const blob = await exportTestProject(params)
  downloadBlob(blob, selectedOnly ? '示例项目_勾选导出.xlsx' : '示例项目_当前条件导出.xlsx')
  ElMessage.success('导出成功')
}

const handleDownloadTemplate = async () => {
  const blob = await downloadImportTemplate()
  downloadBlob(blob, '示例项目导入模板.xlsx')
}

const resetForm = () => {
  Object.assign(formData, createEmptyForm())
  formRef.value?.clearValidate()
}

const showImportSummary = (result: TestProjectImportResult) => {
  const lines = [
    `成功 ${result.successCount} 条`,
    `其中更新 ${result.updateCount} 条`,
    `失败 ${result.failureCount} 条`
  ]
  if (result.failureMessages?.length) {
    lines.push(result.failureMessages.slice(0, 5).join('\n'))
  }
  ElMessageBox.alert(lines.join('\n'), '导入结果', {
    confirmButtonText: '知道了'
  })
}

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

const formatCurrency = (value?: number | string) => {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const getProjectTypeLabel = (value?: number) => projectTypeOptions.find((item) => item.value === value)?.label || '-'
const getStatusLabel = (value?: number) => statusOptions.find((item) => item.value === value)?.label || '-'
const getPriorityLabel = (value?: number) => priorityOptions.find((item) => item.value === value)?.label || '-'
const getStatusTagType = (value?: number) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[value ?? 0] || 'info')
const getPriorityTagType = (value?: number) => ({ 1: 'info', 2: 'warning', 3: 'danger' }[value ?? 2] || 'info')

getList()
</script>

<style scoped>
.project-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.attachment-section {
  margin-top: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>

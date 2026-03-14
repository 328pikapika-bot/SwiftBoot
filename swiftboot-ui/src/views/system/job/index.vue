<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="queryParams.jobName" placeholder="请输入任务名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="任务分组" prop="jobGroup">
          <el-input v-model="queryParams.jobGroup" placeholder="请输入任务分组" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>定时任务列表</span>
          <div>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增</el-button>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column type="selection" width="55" />
        <el-table-column label="任务名称" prop="jobName" min-width="150" />
        <el-table-column label="任务分组" prop="jobGroup" min-width="120" />
        <el-table-column label="调用目标" prop="invokeTarget" min-width="200" show-overflow-tooltip />
        <el-table-column label="Cron表达式" prop="cronExpression" min-width="120" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleRun(row)">执行</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="formData.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务分组" prop="jobGroup">
          <el-input v-model="formData.jobGroup" placeholder="请输入任务分组" />
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="formData.invokeTarget" placeholder="请输入调用目标" />
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="formData.cronExpression" placeholder="如: 0/5 * * * * ?" />
        </el-form-item>
        <el-form-item label="策略">
          <el-radio-group v-model="formData.misfirePolicy">
            <el-radio :value="0">默认</el-radio>
            <el-radio :value="1">立即执行</el-radio>
            <el-radio :value="2">执行一次</el-radio>
            <el-radio :value="3">不执行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { listJob, getJob, addJob, updateJob, deleteJob, changeJobStatus, runJob } from '@/api/system/job'

const loading = ref(false)
const tableData = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const queryRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  jobName: '',
  jobGroup: '',
  status: undefined as number | undefined
})

const formData = reactive({
  jobId: undefined as number | undefined,
  jobName: '',
  jobGroup: 'DEFAULT',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: 0,
  status: 0,
  remark: ''
})

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listJob()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  getList()
}

const handleReset = () => {
  queryRef.value?.resetFields()
  getList()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.jobId)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增任务'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getJob(row.jobId)
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑任务'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  if (formData.jobId) {
    await updateJob(formData)
    ElMessage.success('修改成功')
  } else {
    await addJob(formData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该任务吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteJob(String(row.jobId))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的任务吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteJob(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleStatusChange = async (row: any) => {
  const text = row.status === 0 ? '启用' : '禁用'
  try {
    await changeJobStatus({ jobId: row.jobId, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

const handleRun = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定立即执行该任务吗?', '提示', { type: 'warning' })
    await runJob({ jobId: row.jobId })
    ElMessage.success('执行成功')
  } catch {}
}

const resetForm = () => {
  formData.jobId = undefined
  formData.jobName = ''
  formData.jobGroup = 'DEFAULT'
  formData.invokeTarget = ''
  formData.cronExpression = ''
  formData.misfirePolicy = 0
  formData.status = 0
  formData.remark = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.search-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

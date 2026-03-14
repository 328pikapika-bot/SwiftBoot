<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input v-model="queryParams.noticeTitle" placeholder="请输入公告标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-select v-model="queryParams.noticeType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
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
          <span>公告列表</span>
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
        <el-table-column label="公告标题" prop="noticeTitle" min-width="200" show-overflow-tooltip />
        <el-table-column label="公告类型" prop="noticeType" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 1 ? 'info' : 'warning'">
              {{ row.noticeType === 1 ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input v-model="formData.noticeTitle" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="formData.noticeType">
            <el-radio :value="1">通知</el-radio>
            <el-radio :value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input v-model="formData.noticeContent" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">禁用</el-radio>
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
import { listNotice, getNotice, addNotice, updateNotice, deleteNotice } from '@/api/system/notice'

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
  noticeTitle: '',
  noticeType: undefined as number | undefined
})

const formData = reactive({
  noticeId: undefined as number | undefined,
  noticeTitle: '',
  noticeType: 1,
  noticeContent: '',
  status: 0,
  remark: ''
})

const rules: FormRules = {
  noticeTitle: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeContent: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listNotice()
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
  selectedIds.value = selection.map(item => item.noticeId)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getNotice(row.noticeId)
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  if (formData.noticeId) {
    await updateNotice(formData)
    ElMessage.success('修改成功')
  } else {
    await addNotice(formData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该公告吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteNotice(String(row.noticeId))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的公告吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteNotice(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleStatusChange = async (row: any) => {
  const text = row.status === 0 ? '启用' : '禁用'
  try {
    await updateNotice({ ...row })
    ElMessage.success(`${text}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

const resetForm = () => {
  formData.noticeId = undefined
  formData.noticeTitle = ''
  formData.noticeType = 1
  formData.noticeContent = ''
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

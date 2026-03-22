<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="queryParams.projectCode" placeholder="请输入项目编号" clearable />
        </el-form-item>
        <el-form-item label="项目经理姓名" prop="managerName">
          <el-input v-model="queryParams.managerName" placeholder="请输入项目经理姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>示例_项目表列表</span>
          <div>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column type="selection" width="55" />
        <el-table-column label="项目名称" prop="projectName" width="140"/>
        <el-table-column label="项目编号" prop="projectCode" width="120"/>
        <el-table-column label="项目经理姓名" prop="managerName" />
        <el-table-column label="开始日期" prop="startDate" />
        <el-table-column label="结束日期" prop="endDate" />
        <el-table-column label="项目预算" prop="budget" />
        <el-table-column label="项目进度" prop="progress" />
        <el-table-column label="项目描述" prop="description" />
        <el-table-column label="创建时间" prop="createTime" />
        <el-table-column label="更新时间" prop="updateTime" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="handleQuery"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="formData.projectCode" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目经理姓名" prop="managerName">
          <el-input v-model="formData.managerName" placeholder="请输入项目经理姓名" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="formData.startDate" type="datetime" placeholder="请选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="formData.endDate" type="datetime" placeholder="请选择结束日期" />
        </el-form-item>
        <el-form-item label="项目预算" prop="budget">
          <el-input v-model="formData.budget" placeholder="请输入项目预算" />
        </el-form-item>
        <el-form-item label="项目进度" prop="progress">
          <el-input v-model="formData.progress" placeholder="请输入项目进度（0-100）" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="formData.description" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div class="attachment-section">
        <AttachmentManager biz-type="test:testProject" :biz-id="formData.id || null" title="项目附件" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { listTestProject, getTestProject, addTestProject, updateTestProject, deleteTestProject } from '@/api/testProject'
import AttachmentManager from '@/components/AttachmentManager/index.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const queryRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  projectName: undefined,
  projectCode: undefined,
  managerName: undefined,
})

const formData = reactive({
  id: undefined,
  projectName: undefined,
  projectCode: undefined,
  projectType: undefined,
  managerId: undefined,
  managerName: undefined,
  deptId: undefined,
  startDate: undefined,
  endDate: undefined,
  budget: undefined,
  progress: undefined,
  status: undefined,
  priority: undefined,
  description: undefined,
  remark: undefined,
})

const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
}

const getList = async () => {
  loading.value = true
  try {
    const { data } = await listTestProject(queryParams)
    tableData.value = data.list
    total.value = data.total
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

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增示例_项目表'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const { data } = await getTestProject(row.id)
  Object.assign(formData, data)
  dialogTitle.value = '编辑示例_项目表'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (formData.id) {
    await updateTestProject(formData)
    ElMessage.success('修改成功')
  } else {
    await addTestProject(formData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该记录吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteTestProject(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中记录吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteTestProject(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.projectName = undefined
  formData.projectCode = undefined
  formData.projectType = undefined
  formData.managerId = undefined
  formData.managerName = undefined
  formData.deptId = undefined
  formData.startDate = undefined
  formData.endDate = undefined
  formData.budget = undefined
  formData.progress = undefined
  formData.status = undefined
  formData.priority = undefined
  formData.description = undefined
  formData.remark = undefined
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.attachment-section {
  margin-top: 12px;
}
</style>

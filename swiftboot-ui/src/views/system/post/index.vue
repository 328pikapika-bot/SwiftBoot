<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="queryParams.postCode" placeholder="请输入岗位编码" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="queryParams.postName" placeholder="请输入岗位名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 100px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
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
          <span>岗位列表</span>
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
        <el-table-column label="岗位编码" prop="postCode" min-width="150" />
        <el-table-column label="岗位名称" prop="postName" min-width="150" />
        <el-table-column label="岗位顺序" prop="postSort" min-width="100" align="center" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="formData.postCode" placeholder="如: CEO, MANAGER" :disabled="!!formData.postId" />
        </el-form-item>
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="formData.postName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="postSort">
          <el-input-number v-model="formData.postSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
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
import { listPost, getPost, addPost, updatePost, deletePost } from '@/api/system/post'

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
  postCode: '',
  postName: '',
  status: ''
})

const formData = reactive({
  postId: undefined as number | undefined,
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: ''
})

const rules: FormRules = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listPost()
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
  selectedIds.value = selection.map(item => item.postId)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增岗位'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getPost(row.postId)
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑岗位'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  if (formData.postId) {
    await updatePost(formData)
    ElMessage.success('修改成功')
  } else {
    await addPost(formData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该岗位吗?', '提示', { type: 'warning' }).then(async () => {
    await deletePost(String(row.postId))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的岗位吗?', '提示', { type: 'warning' }).then(async () => {
    await deletePost(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.postId = undefined
  formData.postCode = ''
  formData.postName = ''
  formData.postSort = 0
  formData.status = '0'
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

<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="消息标题" prop="msgTitle">
          <el-input v-model="queryParams.msgTitle" placeholder="请输入消息标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="接收人" prop="receiver">
          <el-input v-model="queryParams.receiver" placeholder="请输入接收人" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="消息类型" prop="msgType">
          <el-select v-model="queryParams.msgType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="系统消息" :value="1" />
            <el-option label="通知" :value="2" />
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
          <span>站内消息列表</span>
          <div>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>发送消息</el-button>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column type="selection" width="55" />
        <el-table-column label="消息标题" prop="msgTitle" min-width="200" show-overflow-tooltip />
        <el-table-column label="消息类型" prop="msgType" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.msgType === 1 ? 'primary' : 'success'">
              {{ row.msgType === 1 ? '系统消息' : '通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送人" prop="sender" min-width="120" />
        <el-table-column label="接收人" prop="receiver" min-width="120" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">
              {{ row.status === 0 ? '已读' : '未读' }}
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="消息标题" prop="msgTitle">
          <el-input v-model="formData.msgTitle" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="消息类型" prop="msgType">
          <el-radio-group v-model="formData.msgType">
            <el-radio :value="1">系统消息</el-radio>
            <el-radio :value="2">通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收人" prop="receiver">
          <el-input v-model="formData.receiver" placeholder="请输入接收人账号" />
        </el-form-item>
        <el-form-item label="消息内容" prop="msgContent">
          <el-input v-model="formData.msgContent" type="textarea" :rows="6" placeholder="请输入消息内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">已读</el-radio>
            <el-radio :value="1">未读</el-radio>
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
import { listMessage, getMessage, addMessage, updateMessage, deleteMessage } from '@/api/system/message'

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
  msgTitle: '',
  receiver: '',
  msgType: undefined as number | undefined
})

const formData = reactive({
  msgId: undefined as number | undefined,
  msgTitle: '',
  msgType: 1,
  sender: 'admin',
  receiver: '',
  msgContent: '',
  status: 1,
  remark: ''
})

const rules: FormRules = {
  msgTitle: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  receiver: [{ required: true, message: '请输入接收人', trigger: 'blur' }],
  msgContent: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listMessage()
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
  selectedIds.value = selection.map(item => item.msgId)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '发送消息'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getMessage(row.msgId)
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑消息'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  if (formData.msgId) {
    await updateMessage(formData)
    ElMessage.success('修改成功')
  } else {
    await addMessage(formData)
    ElMessage.success('发送成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该消息吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteMessage(String(row.msgId))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的消息吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteMessage(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.msgId = undefined
  formData.msgTitle = ''
  formData.msgType = 1
  formData.sender = 'admin'
  formData.receiver = ''
  formData.msgContent = ''
  formData.status = 1
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

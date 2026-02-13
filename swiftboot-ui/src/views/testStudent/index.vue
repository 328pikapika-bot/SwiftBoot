<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="学生名称" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="请输入学生名称" clearable />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="queryParams.age" placeholder="请输入年龄" clearable />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="queryParams.sex" placeholder="请选择性别" clearable>
            <el-option
              v-for="dict in sys_user_gender"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
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
          <span>测试学生表列表</span>
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
        <el-table-column label="学生名称" prop="studentName" />
        <el-table-column label="年龄" prop="age" />
        <el-table-column label="性别" prop="sex">
          <template #default="{ row }">
            <dict-tag :options="sys_user_gender" :value="row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="生日" prop="birthday" />
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
        <el-form-item label="学生ID" prop="id">
          <el-input v-model="formData.id" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="学生名称" prop="studentName">
          <el-input v-model="formData.studentName" placeholder="请输入学生名称" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="formData.age" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="formData.sex" placeholder="请选择性别">
            <el-option
              v-for="dict in sys_user_gender"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择生日"
          />
        </el-form-item>
        <el-form-item label="删除标志（0存在 1删除）" prop="deleted">
          <el-input v-model="formData.deleted" placeholder="请输入删除标志（0存在 1删除）" />
        </el-form-item>
        <el-form-item label="创建者" prop="createBy">
          <el-input v-model="formData.createBy" placeholder="请输入创建者" />
        </el-form-item>
        <el-form-item label="创建时间" prop="createTime">
          <el-date-picker
            v-model="formData.createTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择创建时间"
          />
        </el-form-item>
        <el-form-item label="更新者" prop="updateBy">
          <el-input v-model="formData.updateBy" placeholder="请输入更新者" />
        </el-form-item>
        <el-form-item label="更新时间" prop="updateTime">
          <el-date-picker
            v-model="formData.updateTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择更新时间"
          />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { listTestStudent, getTestStudent, addTestStudent, updateTestStudent, deleteTestStudent } from '@/api/testStudent'
import { useDict } from '@/hooks/useDict'

const { sys_user_gender,  } = useDict('sys_user_gender', )

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
  studentName: undefined,
  age: undefined,
  sex: undefined,
  birthday: undefined,
})

const formData = reactive({
  id: undefined,
  id: undefined,
  studentName: undefined,
  age: undefined,
  sex: undefined,
  birthday: undefined,
  deleted: undefined,
  createBy: undefined,
  createTime: undefined,
  updateBy: undefined,
  updateTime: undefined,
})

const rules = {
}

const getList = async () => {
  loading.value = true
  try {
    const { data } = await listTestStudent(queryParams)
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
  dialogTitle.value = '新增测试学生表'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const { data } = await getTestStudent(row.id)
  Object.assign(formData, data)
  dialogTitle.value = '编辑测试学生表'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (formData.id) {
    await updateTestStudent(formData)
    ElMessage.success('修改成功')
  } else {
    await addTestStudent(formData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该记录吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteTestStudent(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中记录吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteTestStudent(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.id = undefined
  formData.studentName = undefined
  formData.age = undefined
  formData.sex = undefined
  formData.birthday = undefined
  formData.deleted = undefined
  formData.createBy = undefined
  formData.createTime = undefined
  formData.updateBy = undefined
  formData.updateTime = undefined
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

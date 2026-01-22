<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="字典名称">
          <el-input v-model="queryParams.dictName" placeholder="请输入字典名称" clearable />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-input v-model="queryParams.dictType" placeholder="请输入字典类型" clearable />
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
          <span>字典列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData">
        <el-table-column label="字典名称" prop="dictName" width="200" />
        <el-table-column label="字典类型" prop="dictType" width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDictData(row)">{{ row.dictType }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="formData.dictType" placeholder="请输入字典类型" />
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

    <!-- 字典数据弹窗 -->
    <el-drawer v-model="dataDrawerVisible" :title="`字典数据 - ${currentDictType}`" size="800px">
      <div class="drawer-content">
        <div class="drawer-toolbar">
          <el-button type="primary" @click="handleAddData"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
        <el-table :data="dictDataList" v-loading="dataLoading">
          <el-table-column label="字典标签" prop="dictLabel" />
          <el-table-column label="字典值" prop="dictValue" />
          <el-table-column label="排序" prop="sort" width="80" />
          <el-table-column label="状态" prop="status" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditData(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteData(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 字典数据编辑弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px" destroy-on-close>
      <el-form :model="dataFormData" ref="dataFormRef" label-width="90px">
        <el-form-item label="字典标签" prop="dictLabel" :rules="[{ required: true, message: '请输入字典标签' }]">
          <el-input v-model="dataFormData.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue" :rules="[{ required: true, message: '请输入字典值' }]">
          <el-input v-model="dataFormData.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataFormData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataFormData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataFormData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dictName: '',
  dictType: ''
})

const formData = reactive({
  id: undefined as number | undefined,
  dictName: '',
  dictType: '',
  status: 0,
  remark: ''
})

const rules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

// 字典数据
const dataDrawerVisible = ref(false)
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataLoading = ref(false)
const dictDataList = ref<any[]>([])
const currentDictType = ref('')
const dataFormRef = ref<FormInstance>()
const dataFormData = reactive({
  id: undefined as number | undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 0,
  remark: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/dict/type/list', method: 'get', params: queryParams })
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
  queryParams.dictName = ''
  queryParams.dictType = ''
  handleQuery()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增字典类型'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await request({ url: '/system/dict/type/' + row.id, method: 'get' })
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑字典类型'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (formData.id) {
    await request({ url: '/system/dict/type', method: 'put', data: formData })
    ElMessage.success('修改成功')
  } else {
    await request({ url: '/system/dict/type', method: 'post', data: formData })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该字典类型吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/system/dict/type/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.dictName = ''
  formData.dictType = ''
  formData.status = 0
  formData.remark = ''
}

// 字典数据相关方法
const handleDictData = (row: any) => {
  currentDictType.value = row.dictType
  dataDrawerVisible.value = true
  getDictDataList()
}

const getDictDataList = async () => {
  dataLoading.value = true
  try {
    const res = await request({ url: '/system/dict/data/list', method: 'get', params: { dictType: currentDictType.value, pageNum: 1, pageSize: 100 } })
    dictDataList.value = res.data.list
  } finally {
    dataLoading.value = false
  }
}

const handleAddData = () => {
  resetDataForm()
  dataFormData.dictType = currentDictType.value
  dataDialogTitle.value = '新增字典数据'
  dataDialogVisible.value = true
}

const handleEditData = async (row: any) => {
  resetDataForm()
  const res = await request({ url: '/system/dict/data/' + row.id, method: 'get' })
  Object.assign(dataFormData, res.data)
  dataDialogTitle.value = '编辑字典数据'
  dataDialogVisible.value = true
}

const handleDataSubmit = async () => {
  const valid = await dataFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (dataFormData.id) {
    await request({ url: '/system/dict/data', method: 'put', data: dataFormData })
    ElMessage.success('修改成功')
  } else {
    await request({ url: '/system/dict/data', method: 'post', data: dataFormData })
    ElMessage.success('新增成功')
  }
  dataDialogVisible.value = false
  getDictDataList()
}

const handleDeleteData = (row: any) => {
  ElMessageBox.confirm('确定删除该字典数据吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/system/dict/data/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getDictDataList()
  })
}

const resetDataForm = () => {
  dataFormData.id = undefined
  dataFormData.dictType = ''
  dataFormData.dictLabel = ''
  dataFormData.dictValue = ''
  dataFormData.sort = 0
  dataFormData.status = 0
  dataFormData.remark = ''
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.drawer-content {
  padding: 0 20px;
}
.drawer-toolbar {
  margin-bottom: 16px;
}
</style>

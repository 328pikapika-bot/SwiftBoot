<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>部门列表</span>
          <el-button type="primary" @click="handleAdd()"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id" :tree-props="{ children: 'children' }" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column label="部门名称" prop="deptName" min-width="200" />
        <el-table-column label="排序" prop="sort" min-width="80" align="center" />
        <el-table-column label="默认角色" prop="defaultRoleName" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.defaultRoleName" type="info">{{ row.defaultRoleName }}</el-tag>
            <span v-else class="text-gray-400">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="负责人" prop="leader" min-width="120" />
        <el-table-column label="联系电话" prop="phone" min-width="130" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row.id)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.id === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptOptions"
            :props="({ label: 'deptName', children: 'children', value: 'id' } as any)"
            placeholder="选择上级部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="formData.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="默认角色">
          <el-select v-model="formData.defaultRoleId" placeholder="选择默认角色" clearable style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
          <div class="text-xs text-gray-400 mt-1">新增用户时自动分配的默认角色</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const roleList = ref<any[]>([])

// 系统默认角色ID（普通用户）
const DEFAULT_ROLE_ID = 2

const queryParams = reactive({
  deptName: '',
  status: undefined as number | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  parentId: 0 as number,
  deptName: '',
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  defaultRoleId: DEFAULT_ROLE_ID as number | undefined,
  status: 0
})

const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const deptOptions = computed(() => {
  return [{ id: 0, deptName: '顶级部门', children: tableData.value }]
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/dept/tree', method: 'get', params: queryParams })
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.deptName = ''
  queryParams.status = undefined
  getList()
}

const handleAdd = (parentId?: number) => {
  resetForm()
  formData.parentId = parentId || 0
  dialogTitle.value = '新增部门'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await request({ url: '/system/dept/' + row.id, method: 'get' })
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑部门'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (formData.id) {
    await request({ url: '/system/dept', method: 'put', data: formData })
    ElMessage.success('修改成功')
  } else {
    await request({ url: '/system/dept', method: 'post', data: formData })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该部门吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/system/dept/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.parentId = 0
  formData.deptName = ''
  formData.sort = 0
  formData.leader = ''
  formData.phone = ''
  formData.email = ''
  formData.defaultRoleId = DEFAULT_ROLE_ID // 新增时默认选中普通用户
  formData.status = 0
}

// 获取角色列表
const getRoleList = async () => {
  const res = await request({ url: '/system/role/all', method: 'get' })
  roleList.value = res.data
}

onMounted(() => {
  getList()
  getRoleList()
})
</script>

<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="queryParams.deptId"
            :data="deptOptions"
            :props="{ label: 'deptName', children: 'children', value: 'id' }"
            placeholder="选择部门"
            check-strictly
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
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
          <span>用户列表</span>
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
        <el-table-column label="用户名" prop="username" min-width="120" />
        <el-table-column label="昵称" prop="nickname" min-width="120" />
        <el-table-column label="部门" prop="deptName" min-width="120" />
        <el-table-column label="手机号" prop="phone" min-width="130" />
        <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.id === 1">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="所属部门" prop="deptId">
          <el-tree-select
            v-model="formData.deptId"
            :data="deptOptions"
            :props="{ label: 'deptName', children: 'children', value: 'id' }"
            placeholder="选择所属部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item v-if="!formData.id" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
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

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" ref="pwdFormRef" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { listUser, getUser, addUser, updateUser, deleteUser, changeStatus, resetPassword } from '@/api/system/user'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const queryRef = ref<FormInstance>()
const roleList = ref<any[]>([])
const deptList = ref<any[]>([])

// 系统默认角色ID（普通用户）
const DEFAULT_ROLE_ID = 2

// 部门树选项
const deptOptions = computed(() => {
  return deptList.value
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: '',
  status: undefined as number | undefined,
  deptId: undefined as number | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  deptId: undefined as number | undefined,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  status: 0,
  remark: '',
  roleIds: [] as number[]
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 重置密码表单
const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({
  id: undefined as number | undefined,
  password: ''
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listUser(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const getRoleList = async () => {
  const res = await request({ url: '/system/role/all', method: 'get' })
  roleList.value = res.data
}

// 获取部门树
const getDeptTree = async () => {
  const res = await request({ url: '/system/dept/tree', method: 'get' })
  deptList.value = res.data
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
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getUser(row.id)
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  // 角色去重
  const submitData = {
    ...formData,
    roleIds: [...new Set(formData.roleIds)]
  }
  
  if (formData.id) {
    await updateUser(submitData)
    ElMessage.success('修改成功')
  } else {
    await addUser(submitData)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该用户吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteUser(String(row.id))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的用户吗?', '提示', { type: 'warning' }).then(async () => {
    await deleteUser(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

const handleStatusChange = async (row: any) => {
  const text = row.status === 0 ? '启用' : '禁用'
  try {
    await changeStatus({ id: row.id, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

const handleResetPwd = (row: any) => {
  pwdForm.id = row.id
  pwdForm.password = ''
  pwdDialogVisible.value = true
}

const handleResetPwdSubmit = async () => {
  if (!pwdForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  await resetPassword({ id: pwdForm.id!, password: pwdForm.password })
  ElMessage.success('重置成功')
  pwdDialogVisible.value = false
}

const resetForm = () => {
  formData.id = undefined
  formData.deptId = undefined
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.phone = ''
  formData.email = ''
  formData.gender = 0
  formData.status = 0
  formData.remark = ''
  formData.roleIds = [DEFAULT_ROLE_ID] // 默认选中普通用户角色
  formRef.value?.resetFields()
}

// 监听部门变化，自动带出部门默认角色（仅新增用户时）
watch(() => formData.deptId, async (newDeptId) => {
  // 仅新增用户时自动设置默认角色
  if (!formData.id && dialogVisible.value) {
    if (newDeptId) {
      try {
        // 获取部门详情
        const res = await request({ url: '/system/dept/' + newDeptId, method: 'get' })
        const dept = res.data
        if (dept.defaultRoleId) {
          formData.roleIds = [dept.defaultRoleId]
        } else {
          // 部门没设置默认角色，使用系统默认
          formData.roleIds = [DEFAULT_ROLE_ID]
        }
      } catch {
        formData.roleIds = [DEFAULT_ROLE_ID]
      }
    } else {
      // 没选部门，使用系统默认角色
      formData.roleIds = [DEFAULT_ROLE_ID]
    }
  }
})

onMounted(() => {
  getList()
  getRoleList()
  getDeptTree()
})
</script>

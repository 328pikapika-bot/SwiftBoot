<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
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

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column label="角色名称" prop="roleName" min-width="150" />
        <el-table-column label="角色标识" prop="roleKey" min-width="150" />
        <el-table-column label="排序" prop="sort" min-width="80" align="center" />
        <el-table-column label="状态" prop="status" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.id === 1">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="formData.roleKey" placeholder="请输入角色标识" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限">
          <div style="width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; max-height: 300px; overflow: auto;">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :default-checked-keys="formData.menuIds"
              :props="{ label: 'menuName', children: 'children' }"
            />
          </div>
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
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const menuTreeRef = ref()
const menuTree = ref<any[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  status: undefined as number | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 0,
  remark: '',
  menuIds: [] as number[]
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/role/list', method: 'get', params: queryParams })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const getMenuTree = async () => {
  const res = await request({ url: '/system/menu/tree', method: 'get' })
  menuTree.value = res.data
}

// 过滤出叶子节点ID（用于回显，避免设置父节点ID导致自动勾选所有子节点）
const getLeafMenuIds = (menuIds: number[], tree: any[]): number[] => {
  const parentIds = new Set<number>()
  
  // 递归查找所有父节点ID（有children的节点）
  const findParentIds = (nodes: any[]) => {
    for (const node of nodes) {
      if (node.children && node.children.length > 0) {
        parentIds.add(node.id)
        findParentIds(node.children)
      }
    }
  }
  
  findParentIds(tree)
  
  // 返回非父节点的ID（即叶子节点）
  return menuIds.filter(id => !parentIds.has(id))
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.roleName = ''
  queryParams.status = undefined
  handleQuery()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await request({ url: '/system/role/' + row.id, method: 'get' })
  Object.assign(formData, res.data)
  // 获取角色菜单
  const menuRes = await request({ url: '/system/menu/roleMenuIds/' + row.id, method: 'get' })
  // 只设置叶子节点ID，避免设置父节点ID导致自动勾选所有子节点
  formData.menuIds = getLeafMenuIds(menuRes.data, menuTree.value)
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 获取选中的菜单ID
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  formData.menuIds = [...checkedKeys, ...halfCheckedKeys]

  if (formData.id) {
    await request({ url: '/system/role', method: 'put', data: formData })
    ElMessage.success('修改成功')
  } else {
    await request({ url: '/system/role', method: 'post', data: formData })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该角色吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/system/role/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.roleName = ''
  formData.roleKey = ''
  formData.sort = 0
  formData.status = 0
  formData.remark = ''
  formData.menuIds = []
}

onMounted(() => {
  getList()
  getMenuTree()
})
</script>

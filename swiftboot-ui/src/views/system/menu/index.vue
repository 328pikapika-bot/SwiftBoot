<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable />
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
          <span>菜单列表</span>
          <el-button type="primary" @click="handleAdd()"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" :key="tableKey" row-key="id" :tree-props="{ children: 'children' }" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column label="菜单名称" prop="menuName" min-width="200" />
        <el-table-column label="图标" prop="icon" min-width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="getIconName(row.icon)" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" min-width="80" align="center" />
        <el-table-column label="权限标识" prop="perms" min-width="180" show-overflow-tooltip />
        <el-table-column label="路径" prop="path" min-width="150" show-overflow-tooltip />
        <el-table-column label="组件" prop="component" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" prop="menuType" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="primary">目录</el-tag>
            <el-tag v-else type="success">菜单</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.menuType === 'M'" link type="primary" @click="handleAdd(row.id)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuOptions"
            :props="{ label: 'menuName', children: 'children' }"
            placeholder="选择上级菜单"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="formData.menuType" :disabled="!!formData.id">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 'F'" label="图标">
          <el-input v-model="formData.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 'F'" label="路由地址">
          <el-input v-model="formData.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item v-if="formData.menuType === 'C'" label="组件路径">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 'M'" label="权限标识">
          <el-input v-model="formData.perms" placeholder="请输入权限标识" />
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
import { ref, reactive, onMounted, computed, inject } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

// 刷新导航栏菜单
const refreshMenu = inject<() => Promise<void>>('refreshMenu')

// 图标映射（后端图标名 -> Element Plus 图标名）
const iconMap: Record<string, string> = {
  'setting': 'Setting',
  'user': 'User',
  'peoples': 'UserFilled',
  'menu': 'Menu',
  'tree': 'OfficeBuilding',
  'dict': 'Collection',
  'monitor': 'Monitor',
  'form': 'Document',
  'logininfor': 'Tickets',
  'tool': 'Tools',
  'code': 'Document'
}

// 获取图标名称
const getIconName = (icon: string) => {
  return iconMap[icon] || icon || 'Document'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const tableKey = ref(0) // 用于强制刷新表格
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive({
  menuName: '',
  status: undefined as number | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  parentId: 0 as number,
  menuName: '',
  menuType: 'M',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  visible: 0,
  status: 0
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const menuOptions = computed(() => {
  return [{ id: 0, menuName: '顶级菜单', children: tableData.value }]
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/menu/list', method: 'get', params: queryParams })
    tableData.value = buildTree(res.data)
    tableKey.value++ // 强制刷新表格
  } finally {
    loading.value = false
  }
}

// 构建树形结构（过滤掉按钮类型）
const buildTree = (data: any[]) => {
  // 只保留目录(M)和菜单(C)类型
  const filteredData = data.filter(item => item.menuType !== 'F')
  
  const map: any = {}
  const roots: any[] = []
  
  filteredData.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  filteredData.forEach(item => {
    if (item.parentId === 0 || !map[item.parentId]) {
      roots.push(map[item.id])
    } else {
      map[item.parentId].children.push(map[item.id])
    }
  })
  
  return roots
}

const handleReset = () => {
  queryParams.menuName = ''
  queryParams.status = undefined
  getList()
}

const handleAdd = (parentId?: number) => {
  resetForm()
  formData.parentId = parentId || 0
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await request({ url: '/system/menu/' + row.id, method: 'get' })
  Object.assign(formData, res.data)
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (formData.id) {
    await request({ url: '/system/menu', method: 'put', data: formData })
    ElMessage.success('修改成功')
  } else {
    await request({ url: '/system/menu', method: 'post', data: formData })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
  // 刷新导航栏菜单
  refreshMenu?.()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该菜单吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/system/menu/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getList()
    // 刷新导航栏菜单
    refreshMenu?.()
  })
}

const resetForm = () => {
  formData.id = undefined
  formData.parentId = 0
  formData.menuName = ''
  formData.menuType = 'M'
  formData.path = ''
  formData.component = ''
  formData.perms = ''
  formData.icon = ''
  formData.sort = 0
  formData.visible = 0
  formData.status = 0
}

onMounted(() => {
  getList()
})
</script>

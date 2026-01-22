<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="queryParams.loginIp" placeholder="请输入登录IP" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
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
          <span>登录日志列表</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button type="danger" @click="handleClean">
              <el-icon><Delete /></el-icon>清空
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column label="日志ID" prop="id" min-width="180" />
        <el-table-column label="用户名" prop="username" min-width="120" />
        <el-table-column label="登录IP" prop="loginIp" min-width="140" />
        <el-table-column label="登录地点" prop="loginLocation" min-width="150" show-overflow-tooltip />
        <el-table-column label="浏览器" prop="browser" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作系统" prop="os" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提示消息" prop="msg" min-width="200" show-overflow-tooltip />
        <el-table-column label="登录时间" prop="loginTime" min-width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  loginIp: '',
  status: undefined as number | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/monitor/loginlog/list', method: 'get', params: queryParams })
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
  queryParams.username = ''
  queryParams.loginIp = ''
  queryParams.status = undefined
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/loginlog/' + selectedIds.value.join(','), method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const handleClean = () => {
  ElMessageBox.confirm('确定清空所有登录日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/loginlog/clean', method: 'delete' })
    ElMessage.success('清空成功')
    getList()
  })
}

onMounted(() => {
  getList()
})
</script>

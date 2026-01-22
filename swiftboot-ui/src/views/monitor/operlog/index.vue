<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="模块名称">
          <el-input v-model="queryParams.title" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="异常" :value="1" />
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
          <span>操作日志列表</span>
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
        <el-table-column label="模块" prop="title" min-width="120" />
        <el-table-column label="操作类型" prop="businessType" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ operTypeMap[row.businessType] || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" prop="requestMethod" min-width="100" align="center" />
        <el-table-column label="操作人员" prop="operName" min-width="120" />
        <el-table-column label="主机" prop="operIp" min-width="140" />
        <el-table-column label="状态" prop="status" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" prop="costTime" min-width="100" align="center">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operTime" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块名称">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ operTypeMap[detailData.businessType] }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detailData.operName }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detailData.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作方法" :span="2">{{ detailData.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div style="max-height: 200px; overflow: auto;">{{ detailData.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回参数" :span="2">
          <div style="max-height: 200px; overflow: auto;">{{ detailData.jsonResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detailData.status === 0 ? 'success' : 'danger'">{{ detailData.status === 0 ? '正常' : '异常' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detailData.operTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.errorMsg" label="错误信息" :span="2">
          <div style="color: #f56c6c;">{{ detailData.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const operTypeMap: Record<number, string> = {
  0: '其他',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '查询',
  5: '导出',
  6: '导入'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const detailVisible = ref(false)
const detailData = ref<any>({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  operName: '',
  status: undefined as number | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/monitor/operlog/list', method: 'get', params: queryParams })
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
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.status = undefined
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDetail = (row: any) => {
  detailData.value = row
  detailVisible.value = true
}

const handleBatchDelete = () => {
  ElMessageBox.confirm('确定删除选中的日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/operlog/' + selectedIds.value.join(','), method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

const handleClean = () => {
  ElMessageBox.confirm('确定清空所有操作日志吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/monitor/operlog/clean', method: 'delete' })
    ElMessage.success('清空成功')
    getList()
  })
}

onMounted(() => {
  getList()
})
</script>

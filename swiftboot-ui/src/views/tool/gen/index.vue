<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="表名称">
          <el-input v-model="queryParams.tableName" placeholder="请输入表名称" clearable />
        </el-form-item>
        <el-form-item label="表描述">
          <el-input v-model="queryParams.tableComment" placeholder="请输入表描述" clearable />
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
          <span>代码生成列表</span>
          <div>
            <el-button type="info" @click="handleDownloadDoc"><el-icon><Download /></el-icon>下载文档说明</el-button>
            <el-button type="primary" @click="handleImport"><el-icon><Upload /></el-icon>导入</el-button>
            <el-button type="success" :disabled="!selectedIds.length" @click="handleBatchGenerate">
              <el-icon><Download /></el-icon>批量生成
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column type="selection" width="55" />
        <el-table-column label="表名称" prop="tableName" min-width="200" />
        <el-table-column label="表描述" prop="tableComment" min-width="200" />
        <el-table-column label="实体类名" prop="className" min-width="180" />
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
        <el-table-column label="更新时间" prop="updateTime" min-width="180" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleSync(row)">同步</el-button>
            <el-button link type="success" @click="handleGenerate(row)">
              {{ row.genType === '0' ? '下载' : (row.genPath && row.genPath.trim() ? '生成' : '下载') }}
            </el-button>
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

    <!-- 导入表弹窗 -->
    <el-dialog v-model="importVisible" title="导入表" width="800px">
      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="表名称">
          <el-input v-model="importQueryParams.tableName" placeholder="请输入表名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getDbTableList">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="importLoading" :data="dbTableList" @selection-change="handleDbTableSelection" height="400">
        <el-table-column type="selection" width="55" />
        <el-table-column label="表名称" prop="tableName" />
        <el-table-column label="表描述" prop="tableComment" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
      </el-table>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedDbTables.length" @click="handleImportSubmit">导入</el-button>
      </template>
    </el-dialog>

    <!-- 预览代码弹窗 -->
    <el-dialog v-model="previewVisible" title="代码预览" width="80%" top="5vh">
      <el-tabs v-model="previewActiveTab">
        <el-tab-pane v-for="(code, name) in previewData" :key="name" :label="getTabLabel(name)" :name="name">
          <pre class="code-preview">{{ code }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 下载文档格式选择弹窗 -->
    <el-dialog v-model="docFormatVisible" title="选择文档格式" width="400px">
      <div style="text-align: center; padding: 20px;">
        <el-radio-group v-model="selectedDocFormat">
          <el-radio value="md" size="large">📄 Markdown (.md)</el-radio><br><br>
          <el-radio value="doc" size="large">📝 Word文档 (.doc)</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="docFormatVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDocDownload" :disabled="!selectedDocFormat">
          <el-icon><Download /></el-icon>下载文档
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑配置弹窗 -->
    <el-drawer v-model="editVisible" title="代码生成配置" size="1200px">
      <el-tabs v-model="editActiveTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="editFormData" label-width="120px" style="padding: 20px;">
            <el-form-item label="表名称">
              <el-input v-model="editFormData.tableName" disabled />
            </el-form-item>
            <el-form-item label="表描述">
              <el-input v-model="editFormData.tableComment" />
            </el-form-item>
            <el-form-item label="实体类名称">
              <el-input v-model="editFormData.className" />
            </el-form-item>
            <el-form-item label="生成包路径">
              <el-input v-model="editFormData.packageName" />
            </el-form-item>
            <el-form-item label="生成模块名">
              <el-input v-model="editFormData.moduleName" />
            </el-form-item>
            <el-form-item label="生成业务名">
              <el-input v-model="editFormData.businessName" />
            </el-form-item>
            <el-form-item label="生成功能名">
              <el-input v-model="editFormData.functionName" />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="editFormData.author" />
            </el-form-item>
            <el-form-item label="生成方式">
              <el-radio-group v-model="editFormData.genType">
                <el-radio value="0">zip压缩包</el-radio>
                <el-radio value="1">自定义路径</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="editFormData.genType === '1'" label="生成路径">
              <el-input v-model="editFormData.genPath" placeholder="请输入生成路径" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="字段信息" name="columns">
          <el-table :data="editFormData.columns" style="margin: 20px; width: calc(100% - 40px);">
            <el-table-column label="字段名" prop="columnName" min-width="130" />
            <el-table-column label="字段描述" prop="columnComment" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.columnComment" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="物理类型" prop="columnType" min-width="110" />
            <el-table-column label="Java类型" prop="javaType" min-width="130">
              <template #default="{ row }">
                <el-select v-model="row.javaType" size="small">
                  <el-option label="String" value="String" />
                  <el-option label="Long" value="Long" />
                  <el-option label="Integer" value="Integer" />
                  <el-option label="BigDecimal" value="BigDecimal" />
                  <el-option label="LocalDateTime" value="LocalDateTime" />
                  <el-option label="LocalDate" value="LocalDate" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="Java字段" prop="javaField" min-width="120" />
            <el-table-column label="插入" prop="isInsert" width="55" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isInsert" true-value="1" false-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="编辑" prop="isEdit" width="55" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isEdit" true-value="1" false-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="列表" prop="isList" width="55" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isList" true-value="1" false-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="查询" prop="isQuery" width="55" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isQuery" true-value="1" false-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="查询方式" prop="queryType" min-width="100">
              <template #default="{ row }">
                <el-select v-model="row.queryType" size="small">
                  <el-option label="=" value="EQ" />
                  <el-option label="!=" value="NE" />
                  <el-option label="LIKE" value="LIKE" />
                  <el-option label=">" value="GT" />
                  <el-option label="<" value="LT" />
                  <el-option label=">=" value="GE" />
                  <el-option label="<=" value="LE" />
                  <el-option label="BETWEEN" value="BETWEEN" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="显示类型" prop="htmlType" min-width="110">
              <template #default="{ row }">
                <el-select v-model="row.htmlType" size="small">
                  <el-option label="文本框" value="input" />
                  <el-option label="文本域" value="textarea" />
                  <el-option label="下拉框" value="select" />
                  <el-option label="单选框" value="radio" />
                  <el-option label="复选框" value="checkbox" />
                  <el-option label="日期控件" value="datetime" />
                  <el-option label="图片上传" value="imageUpload" />
                  <el-option label="文件上传" value="fileUpload" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="配置" width="60" align="center">
              <template #default="{ row }">
                <el-button 
                  v-if="['select', 'radio', 'checkbox'].includes(row.htmlType)"
                  type="primary" 
                  link 
                  icon="Setting" 
                  @click="handleDictConfig(row)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-drawer>

    <!-- 字典配置弹窗 -->
    <el-dialog v-model="dictVisible" title="选择字典" width="500px" append-to-body>
      <el-form :model="currentDictRow" label-width="80px">
        <el-form-item label="字典类型">
          <el-select 
            v-model="currentDictRow.dictType" 
            clearable 
            filterable 
            placeholder="请选择字典类型" 
            style="width: 100%"
          >
            <el-option
              v-for="dict in dictOptions"
              :key="dict.dictType"
              :label="dict.dictName"
              :value="dict.dictType"
            >
              <span style="float: left">{{ dict.dictName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDictSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Search, Refresh, Plus, Delete, Download, Upload, Setting } from '@element-plus/icons-vue'

import { listType } from '@/api/system/dict/type'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const selectedTableNames = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  tableName: '',
  tableComment: ''
})

// 导入相关
const importVisible = ref(false)
const importLoading = ref(false)
const dbTableList = ref<any[]>([])
const selectedDbTables = ref<string[]>([])
const importQueryParams = reactive({
  tableName: '',
  tableComment: ''
})

// 预览相关
const previewVisible = ref(false)
const previewData = ref<Record<string, string>>({})
const previewActiveTab = ref('')

// 文档下载相关
const docFormatVisible = ref(false)
const selectedDocFormat = ref('')

// 编辑相关
const editVisible = ref(false)
const editActiveTab = ref('basic')
const dictOptions = ref<any[]>([])

const editFormData = reactive({
  id: undefined as number | undefined,
  tableName: '',
  tableComment: '',
  className: '',
  packageName: '',
  moduleName: '',
  businessName: '',
  functionName: '',
  author: '',
  genType: '0',
  genPath: '',
  columns: [] as any[]
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/tool/gen/list', method: 'get', params: queryParams })
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
  queryParams.tableName = ''
  queryParams.tableComment = ''
  handleQuery()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
  selectedTableNames.value = selection.map(item => item.tableName)
}

// 导入表
const handleImport = () => {
  importVisible.value = true
  getDbTableList()
}

const getDbTableList = async () => {
  importLoading.value = true
  try {
    const res = await request({ url: '/tool/gen/db/list', method: 'get', params: importQueryParams })
    dbTableList.value = res.data
  } finally {
    importLoading.value = false
  }
}

const handleDbTableSelection = (selection: any[]) => {
  selectedDbTables.value = selection.map(item => item.tableName)
}

const handleImportSubmit = async () => {
  await request({ url: '/tool/gen/importTable', method: 'post', data: selectedDbTables.value })
  ElMessage.success('导入成功')
  importVisible.value = false
  getList()
}

// 预览代码
const handlePreview = async (row: any) => {
  const res = await request({ url: '/tool/gen/preview/' + row.id, method: 'get' })
  previewData.value = res.data
  previewActiveTab.value = Object.keys(res.data)[0]
  previewVisible.value = true
}

const getTabLabel = (name: string) => {
  const labels: Record<string, string> = {
    'vm/java/entity.java.vm': 'Entity',
    'vm/java/mapper.java.vm': 'Mapper',
    'vm/java/service.java.vm': 'Service',
    'vm/java/serviceImpl.java.vm': 'ServiceImpl',
    'vm/java/controller.java.vm': 'Controller',
    'vm/xml/mapper.xml.vm': 'Mapper.xml',
    'vm/vue/index.vue.vm': 'Vue',
    'vm/vue/router.ts.vm': 'router',
    'vm/vue/api.ts.vm': 'API',
    'vm/sql/sql.vm': 'SQL'
  }
  return labels[name] || name
}

// 编辑配置
const handleEdit = async (row: any) => {
  const res = await request({ url: '/tool/gen/' + row.id, method: 'get' })
  Object.assign(editFormData, res.data)
  // 获取字典列表
  listType({ pageNum: 1, pageSize: 100 }).then(response => {
    dictOptions.value = response.data.list
  })
  editActiveTab.value = 'basic'
  editVisible.value = true
}

const handleEditSubmit = async () => {
  await request({ url: '/tool/gen', method: 'put', data: editFormData })
  ElMessage.success('保存成功')
  editVisible.value = false
  getList()
}

// 同步数据库
const handleSync = async (row: any) => {
  await request({ url: '/tool/gen/syncDb/' + row.tableName, method: 'get' })
  ElMessage.success('同步成功')
}

// 生成代码
const handleGenerate = async (row: any) => {
  // 根据生成类型决定生成方式
  if (row.genType === '0') {
    // zip压缩包：直接下载
    window.location.href = `/api/tool/gen/download/${row.tableName}`
  } else if (row.genType === '1' && row.genPath && row.genPath.trim()) {
    // 自定义路径：生成到指定目录
    try {
      await request({
        url: `/tool/gen/generate/${row.tableName}`,
        method: 'get'
      })
      ElMessage.success(`代码已生成到: ${row.genPath}`)
    } catch (error) {
      ElMessage.error('生成失败: ' + error.message)
    }
  } else {
    // 默认情况：下载压缩包
    window.location.href = `/api/tool/gen/download/${row.tableName}`
  }
}

const handleBatchGenerate = async () => {
  // 检查选中的表是否都选择了zip压缩包生成
  const zipTables = tableData.value.filter(table =>
    selectedTableNames.value.includes(table.tableName) && table.genType === '0'
  )

  // 检查选中的表是否都选择了自定义路径且设置了路径
  const customPathTables = tableData.value.filter(table =>
    selectedTableNames.value.includes(table.tableName) &&
    table.genType === '1' &&
    table.genPath &&
    table.genPath.trim()
  )

  if (zipTables.length === selectedTableNames.value.length) {
    // 所有选中的表都选择了zip压缩包，批量下载
    window.location.href = `/api/tool/gen/batchDownload?tableNames=${selectedTableNames.value.join(',')}`
  } else if (customPathTables.length === selectedTableNames.value.length) {
    // 所有选中的表都选择了自定义路径且设置了路径，批量生成到目录
    try {
      for (const tableName of selectedTableNames.value) {
        await request({
          url: `/tool/gen/generate/${tableName}`,
          method: 'get'
        })
      }
      ElMessage.success('批量生成完成')
    } catch (error) {
      ElMessage.error('批量生成失败: ' + error.message)
    }
  } else {
    // 混合情况或其他情况，默认下载压缩包
    window.location.href = `/api/tool/gen/batchDownload?tableNames=${selectedTableNames.value.join(',')}`
  }
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该记录吗?', '提示', { type: 'warning' }).then(async () => {
    await request({ url: '/tool/gen/' + row.id, method: 'delete' })
    ElMessage.success('删除成功')
    getList()
  })
}

// 文档下载相关方法
const handleDownloadDoc = () => {
  selectedDocFormat.value = ''
  docFormatVisible.value = true
}

const handleDocDownload = () => {
  if (!selectedDocFormat.value) {
    ElMessage.warning('请选择文档格式')
    return
  }

  const fileMap: Record<string, string> = {
    md: '/docs/SwiftBoot.md',
    doc: '/docs/SwiftBoot.doc'
  }

  const fileNameMap: Record<string, string> = {
    md: 'SwiftBoot-代码生成器使用指南.md',
    doc: 'SwiftBoot-代码生成器使用指南.doc'
  }

  const link = document.createElement('a')
  link.href = fileMap[selectedDocFormat.value]
  link.download = fileNameMap[selectedDocFormat.value]
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  docFormatVisible.value = false
}



// 字典配置相关
const dictVisible = ref(false)
const currentDictRow = ref<any>({})

const handleDictConfig = (row: any) => {
  currentDictRow.value = row
  dictVisible.value = true
}

const handleDictSubmit = () => {
  dictVisible.value = false
}

onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
.code-preview {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow: auto;
  max-height: 60vh;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre;
}
</style>

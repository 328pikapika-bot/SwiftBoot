<template>
  <div class="file-page">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="left">
        <el-input
          v-model="searchText"
          placeholder="搜索文件名..."
          clearable
          @input="handleSearch"
          style="width: 260px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="right">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleUploadChange"
          multiple
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>
        <el-button type="success" @click="submitUpload" :loading="uploading">
          <el-icon><Upload /></el-icon>
          上传
        </el-button>
      </div>
    </div>

    <!-- 文件列表 -->
    <el-table :data="tableData" v-loading="loading" class="file-table">
      <el-table-column label="文件名" min-width="320">
        <template #default="{ row }">
          <div class="file-info">
            <el-icon :size="22" class="file-icon">
              <Document v-if="!isImage(row.originalName)" />
              <Picture v-else />
            </el-icon>
            <span class="file-name">{{ row.originalName || row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="100" align="center">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="上传时间" width="180" prop="createTime" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="previewFile(row)">预览</el-button>
          <el-button link type="success" @click="downloadFile(row)">下载</el-button>
          <el-button link type="danger" @click="deleteFile(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="65%" destroy-on-close>
      <div class="preview-content">
        <img v-if="previewIsImage" :src="previewUrl" class="preview-img" />
        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-pdf" />
        <div v-else class="preview-other">
          <el-icon :size="60"><Document /></el-icon>
          <p>暂不支持此格式在线预览，请下载查看</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload, Document, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const uploading = ref(false)
const files = ref<any[]>([])
const uploadRef = ref()
const uploadFiles = ref<any[]>([])

const searchText = ref('')
const pageNum = ref(1)
const pageSize = ref(10)

const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('')

// 筛选
const filteredFiles = computed(() => {
  if (!searchText.value) return files.value
  const kw = searchText.value.toLowerCase()
  return files.value.filter(f => 
    (f.originalName || f.fileName || '').toLowerCase().includes(kw)
  )
})

// 表格数据
const tableData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredFiles.value.slice(start, start + pageSize.value)
})

const total = computed(() => filteredFiles.value.length)

const previewIsImage = computed(() => previewType.value === 'image')

// 判断是否为图片
const isImage = (name: string) => {
  const ext = (name || '').split('.').pop()?.toLowerCase() || ''
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/file/list', method: 'get' })
    files.value = (res.data || []).sort((a: any, b: any) => 
      new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    )
  } catch (e) {
    files.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
}

// 文件选择
const handleUploadChange = (file: any, fileList: any) => {
  uploadFiles.value = fileList
}

// 提交上传
const submitUpload = async () => {
  if (!uploadFiles.value.length) {
    ElMessage.warning('请选择文件')
    return
  }
  
  uploading.value = true
  let success = 0
  
  for (const item of uploadFiles.value) {
    const formData = new FormData()
    formData.append('file', item.raw)
    try {
      const res: any = await request({
        url: '/system/file/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      if (res.code === 200) success++
    } catch (e) {}
  }
  
  uploading.value = false
  uploadRef.value?.clearFiles()
  uploadFiles.value = []
  loadData()
  ElMessage.success(`上传成功 ${success} 个文件`)
}

// 预览
const previewFile = (row: any) => {
  const ext = (row.originalName || row.fileName || '').split('.').pop()?.toLowerCase() || ''
  previewUrl.value = `/system/file/preview/${row.fileName}`
  
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)) {
    previewType.value = 'image'
  } else if (ext === 'pdf') {
    previewType.value = 'pdf'
  } else {
    previewType.value = 'other'
  }
  previewVisible.value = true
}

// 下载
const downloadFile = (row: any) => {
  const url = `/system/file/download/${row.fileName}`
  const link = document.createElement('a')
  link.href = url
  link.download = row.originalName || row.fileName
  link.click()
}

// 删除
const deleteFile = (row: any) => {
  ElMessageBox.confirm('确定删除该文件吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request({ url: `/system/file/${row.fileName}`, method: 'delete' })
    } catch (e) {}
    ElMessage.success('删除成功')
    loadData()
  })
}

// 格式化大小
const formatSize = (size: any) => {
  if (!size) return '0 B'
  let s = Number(size)
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (s >= 1024 && i < units.length - 1) {
    s /= 1024
    i++
  }
  return `${s.toFixed(1)} ${units[i]}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-page {
  padding: 20px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar .right {
  display: flex;
  gap: 10px;
}
.file-table {
  border-radius: 8px;
}
.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.file-icon {
  color: #409eff;
}
.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.preview-content {
  text-align: center;
  min-height: 500px;
}
.preview-img {
  max-width: 100%;
  max-height: 75vh;
}
.preview-pdf {
  width: 100%;
  height: 75vh;
  border: none;
}
.preview-other {
  padding: 80px;
  color: #909399;
}
</style>

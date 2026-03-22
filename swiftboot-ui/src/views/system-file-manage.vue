<template>
  <div class="file-page">
    <el-card shadow="never" class="filter-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索文件名" clearable style="width: 240px" @keyup.enter="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-input v-model="query.bizType" placeholder="业务类型 bizType" clearable style="width: 180px" />
        <el-input-number v-model="query.bizId" placeholder="业务ID" :min="1" controls-position="right" style="width: 160px" />
        <el-select v-model="query.storageType" placeholder="存储类型" clearable style="width: 150px">
          <el-option label="本地" value="local" />
          <el-option label="MinIO" value="minio" />
          <el-option label="阿里云 OSS" value="oss" />
          <el-option label="腾讯 COS" value="cos" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <div class="upload-row">
        <el-input v-model="uploadExtra.bizType" placeholder="上传绑定 bizType，可留空" clearable style="width: 220px" />
        <el-input-number v-model="uploadExtra.bizId" placeholder="上传绑定 bizId" :min="1" controls-position="right" style="width: 180px" />
        <el-select v-model="uploadExtra.visibility" style="width: 150px">
          <el-option label="跟随存储默认" value="" />
          <el-option label="私有" value="private" />
          <el-option label="公开" value="public" />
        </el-select>
        <el-upload ref="uploadRef" :auto-upload="false" :show-file-list="false" :on-change="handleUploadChange" multiple>
          <el-button type="primary" plain>
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>
        <el-button type="success" :loading="uploading" @click="submitUpload">
          <el-icon><Upload /></el-icon>
          上传
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column label="文件名称" min-width="260">
          <template #default="{ row }">
            <div class="file-name-cell">
              <el-icon :size="20" class="file-icon">
                <Picture v-if="isImage(row.originalName)" />
                <Document v-else />
              </el-icon>
              <div class="file-meta">
                <span class="file-name">{{ row.originalName || row.fileName }}</span>
                <span class="file-key">{{ row.fileName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="110" align="center">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="存储" prop="storageType" width="120" align="center" />
        <el-table-column label="可见性" prop="visibility" width="110" align="center" />
        <el-table-column label="业务类型" prop="bizType" min-width="160" />
        <el-table-column label="业务ID" prop="bizId" width="120" />
        <el-table-column label="上传时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="previewFile(row)">预览</el-button>
            <el-button link type="success" @click="downloadFile(row)">下载</el-button>
            <el-button link type="warning" @click="renameFileAction(row)">重命名</el-button>
            <el-button link type="danger" @click="deleteFileAction(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>

    <el-dialog v-model="previewVisible" title="文件预览" width="70%" destroy-on-close>
      <div class="preview-content">
        <img v-if="previewType === 'image'" :src="previewUrl" class="preview-img" />
        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-pdf" />
        <div v-else class="preview-placeholder">
          <el-icon :size="60"><Document /></el-icon>
          <p>当前格式不支持在线预览，请下载查看。</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload, Document, Picture } from '@element-plus/icons-vue'
import { deleteFile, getDownloadUrl, getPreviewUrl, listFiles, renameFile, type SysFile, uploadFile } from '@/api/system/file'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<SysFile[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const uploadRef = ref()
const uploadFiles = ref<any[]>([])

const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'pdf' | 'other'>('other')

const query = reactive<{ keyword: string; bizType: string; bizId: number | null; storageType: string }>({
  keyword: '',
  bizType: '',
  bizId: null,
  storageType: ''
})

const uploadExtra = reactive<{ bizType: string; bizId: number | null; visibility: '' | 'private' | 'public' }>({
  bizType: '',
  bizId: null,
  visibility: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await listFiles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: query.keyword || undefined,
      bizType: query.bizType || undefined,
      bizId: query.bizId || undefined,
      storageType: query.storageType || undefined
    })
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  query.keyword = ''
  query.bizType = ''
  query.bizId = null
  query.storageType = ''
  pageNum.value = 1
  loadData()
}

const handleUploadChange = (_file: any, fileList: any[]) => {
  uploadFiles.value = fileList
}

const submitUpload = async () => {
  if (!uploadFiles.value.length) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  let success = 0
  for (const item of uploadFiles.value) {
    try {
      await uploadFile(item.raw, {
        bizType: uploadExtra.bizType || undefined,
        bizId: uploadExtra.bizId || undefined,
        visibility: uploadExtra.visibility || undefined
      })
      success++
    } catch (error) {
      // request interceptor handles errors
    }
  }
  uploading.value = false
  uploadRef.value?.clearFiles()
  uploadFiles.value = []
  await loadData()
  ElMessage.success(`成功上传 ${success} 个文件`)
}

const previewFile = async (row: SysFile) => {
  const ext = (row.originalName || row.fileName || '').split('.').pop()?.toLowerCase() || ''
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)) {
    previewType.value = 'image'
  } else if (ext === 'pdf') {
    previewType.value = 'pdf'
  } else {
    previewType.value = 'other'
  }

  if (previewType.value === 'other') {
    previewUrl.value = ''
    previewVisible.value = true
    return
  }

  const res = await getPreviewUrl(row.id)
  previewUrl.value = res.data
  previewVisible.value = true
}

const downloadFile = async (row: SysFile) => {
  const res = await getDownloadUrl(row.id)
  const link = document.createElement('a')
  link.href = res.data
  link.target = '_blank'
  link.click()
}

const renameFileAction = async (row: SysFile) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的显示名称', '重命名文件', {
      inputValue: row.originalName || row.fileName,
      confirmButtonText: '保存',
      cancelButtonText: '取消'
    })
    if (value) {
      await renameFile(row.id, value)
      ElMessage.success('重命名成功')
      loadData()
    }
  } catch (error) {
    // canceled
  }
}

const deleteFileAction = async (row: SysFile) => {
  try {
    await ElMessageBox.confirm('确定删除该文件吗？', '提示', { type: 'warning' })
    await deleteFile(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // canceled
  }
}

const isImage = (name?: string) => {
  const ext = (name || '').split('.').pop()?.toLowerCase() || ''
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)
}

const formatSize = (size?: number) => {
  if (!size) return '0 B'
  let value = Number(size)
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024
    index++
  }
  return `${value.toFixed(1)} ${units[index]}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-page {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar,
.upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.upload-row {
  margin-top: 12px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon {
  color: #409eff;
}

.file-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 600;
}

.file-key {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.preview-content {
  min-height: 420px;
  text-align: center;
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

.preview-placeholder {
  padding: 80px 0;
  color: var(--el-text-color-secondary);
}
</style>

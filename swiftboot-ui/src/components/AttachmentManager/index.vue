<template>
  <div class="attachment-manager">
    <div class="attachment-toolbar">
      <div class="attachment-title">
        <span>{{ title }}</span>
        <el-tag size="small" type="info" effect="plain">{{ files.length }} 个文件</el-tag>
      </div>
      <div class="attachment-actions" v-if="!readonly">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleUploadChange"
          :accept="accept"
          :multiple="multiple"
        >
          <el-button type="primary" plain size="small">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>
        <el-button size="small" type="success" :loading="uploading" @click="submitUpload">上传</el-button>
      </div>
    </div>

    <el-alert
      v-if="!bizId"
      type="warning"
      :closable="false"
      show-icon
      class="attachment-alert"
      title="请先保存主数据，再上传附件。保存后该组件会自动按业务类型和业务ID加载附件。"
    />

    <div v-else class="attachment-list">
      <div v-if="files.length === 0" class="attachment-empty">
        <el-empty description="暂无附件" :image-size="72" />
      </div>

      <div v-else class="attachment-grid">
        <div v-for="file in files" :key="file.id" class="attachment-card">
          <div class="attachment-main">
            <el-icon :size="18" class="attachment-icon">
              <Picture v-if="isImage(file.originalName)" />
              <Document v-else />
            </el-icon>
            <div class="attachment-meta">
              <div class="attachment-name" :title="file.originalName">{{ file.originalName || file.fileName }}</div>
              <div class="attachment-desc">
                <span>{{ formatSize(file.fileSize) }}</span>
                <span>{{ file.storageType }}</span>
                <span>{{ file.createTime || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="attachment-ops">
            <el-button link type="primary" size="small" @click="previewFile(file)">预览</el-button>
            <el-button link type="success" size="small" @click="downloadFile(file)">下载</el-button>
            <el-button v-if="!readonly" link type="danger" size="small" @click="deleteFileAction(file)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="previewVisible" title="附件预览" width="70%" destroy-on-close>
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
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Picture, Upload } from '@element-plus/icons-vue'
import { deleteFile, getDownloadUrl, getPreviewUrl, listFiles, type SysFile, uploadFile } from '@/api/system/file'

interface Props {
  bizType: string
  bizId?: number | null
  title?: string
  readonly?: boolean
  multiple?: boolean
  accept?: string
  visibility?: '' | 'private' | 'public'
}

const props = withDefaults(defineProps<Props>(), {
  title: '附件',
  bizId: null,
  readonly: false,
  multiple: true,
  accept: '',
  visibility: ''
})

const emit = defineEmits<{
  refreshed: [files: SysFile[]]
}>()

const files = ref<SysFile[]>([])
const uploading = ref(false)
const uploadRef = ref()
const pendingFiles = ref<any[]>([])
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'pdf' | 'other'>('other')

const loadFiles = async () => {
  if (!props.bizId) {
    files.value = []
    emit('refreshed', [])
    return
  }
  const res = await listFiles({
    pageNum: 1,
    pageSize: 200,
    bizType: props.bizType,
    bizId: props.bizId
  })
  files.value = res.data?.list || []
  emit('refreshed', files.value)
}

const handleUploadChange = (_file: any, fileList: any[]) => {
  pendingFiles.value = fileList
}

const submitUpload = async () => {
  if (!props.bizId) {
    ElMessage.warning('请先保存主数据，再上传附件')
    return
  }
  if (!pendingFiles.value.length) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  let success = 0
  for (const item of pendingFiles.value) {
    try {
      await uploadFile(item.raw, {
        bizType: props.bizType,
        bizId: props.bizId,
        visibility: props.visibility || undefined
      })
      success++
    } catch (error) {
      // handled globally
    }
  }
  uploading.value = false
  pendingFiles.value = []
  uploadRef.value?.clearFiles()
  await loadFiles()
  ElMessage.success(`成功上传 ${success} 个附件`)
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

const deleteFileAction = async (row: SysFile) => {
  await ElMessageBox.confirm('确定删除该附件吗？', '提示', { type: 'warning' })
  await deleteFile(row.id)
  ElMessage.success('附件已删除')
  loadFiles()
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

watch(
  () => [props.bizType, props.bizId],
  () => {
    loadFiles()
  },
  { immediate: true }
)
</script>

<style scoped>
.attachment-manager {
  width: 100%;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 14px;
  background: var(--el-bg-color);
}

.attachment-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.attachment-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.attachment-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.attachment-alert {
  margin-top: 12px;
}

.attachment-list {
  margin-top: 12px;
}

.attachment-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
}

.attachment-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.attachment-icon {
  color: #409eff;
}

.attachment-meta {
  min-width: 0;
}

.attachment-name {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-weight: 600;
}

.attachment-desc {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attachment-ops {
  flex-shrink: 0;
}

.attachment-empty {
  padding: 12px 0;
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

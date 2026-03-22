<template>
  <div class="storage-panel">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span class="panel-title">存储配置</span>
          <el-tag type="success">当前生效: {{ typeLabelMap[form.activeType] }}</el-tag>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #default>
          <p>1. 这里配置的是框架统一文件存储，附件、图片、导入、导出都可以复用这套能力。</p>
          <p>2. 切换存储类型后，新上传文件会走新的存储；已上传文件仍按记录中的存储类型读取。</p>
          <p>3. 云存储配置建议先填 endpoint、凭证和 bucket，再切换为生效存储。</p>
        </template>
      </el-alert>

      <el-form label-width="140px">
        <el-form-item label="当前存储类型">
          <el-select v-model="form.activeType" style="width: 260px">
            <el-option label="本地存储" value="local" />
            <el-option label="MinIO" value="minio" />
            <el-option label="阿里云 OSS" value="oss" />
            <el-option label="腾讯云 COS" value="cos" />
          </el-select>
        </el-form-item>

        <el-form-item label="访问令牌有效期">
          <el-input-number v-model="form.accessUrlExpireSeconds" :min="60" :max="86400" controls-position="right" />
          <span class="form-tip">单位：秒。用于图片预览、PDF 预览、下载访问链接。</span>
        </el-form-item>
      </el-form>

      <el-tabs v-model="providerTab" class="provider-tabs">
        <el-tab-pane label="本地存储" name="local">
          <el-form label-width="140px">
            <el-form-item label="本地目录">
              <el-input v-model="form.local.basePath" placeholder="例如 D:/upload" />
            </el-form-item>
            <el-form-item label="公开读">
              <el-switch v-model="form.local.publicRead" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="MinIO" name="minio">
          <el-form label-width="140px">
            <el-form-item label="Endpoint">
              <el-input v-model="form.minio.endpoint" placeholder="http://127.0.0.1:9000" />
            </el-form-item>
            <el-form-item label="Access Key">
              <el-input v-model="form.minio.accessKey" />
            </el-form-item>
            <el-form-item label="Secret Key">
              <el-input v-model="form.minio.secretKey" type="password" show-password />
            </el-form-item>
            <el-form-item label="Bucket">
              <el-input v-model="form.minio.bucket" />
            </el-form-item>
            <el-form-item label="外部域名">
              <el-input v-model="form.minio.domain" placeholder="可选，用于公开访问" />
            </el-form-item>
            <el-form-item label="公开读">
              <el-switch v-model="form.minio.publicRead" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="阿里云 OSS" name="oss">
          <el-form label-width="140px">
            <el-form-item label="Endpoint">
              <el-input v-model="form.oss.endpoint" placeholder="https://oss-cn-hangzhou.aliyuncs.com" />
            </el-form-item>
            <el-form-item label="Access Key ID">
              <el-input v-model="form.oss.accessKeyId" />
            </el-form-item>
            <el-form-item label="Access Key Secret">
              <el-input v-model="form.oss.accessKeySecret" type="password" show-password />
            </el-form-item>
            <el-form-item label="Bucket">
              <el-input v-model="form.oss.bucket" />
            </el-form-item>
            <el-form-item label="外部域名">
              <el-input v-model="form.oss.domain" placeholder="可选，用于 CDN / 公开访问" />
            </el-form-item>
            <el-form-item label="公开读">
              <el-switch v-model="form.oss.publicRead" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="腾讯云 COS" name="cos">
          <el-form label-width="140px">
            <el-form-item label="Region">
              <el-input v-model="form.cos.region" placeholder="ap-shanghai" />
            </el-form-item>
            <el-form-item label="Secret ID">
              <el-input v-model="form.cos.secretId" />
            </el-form-item>
            <el-form-item label="Secret Key">
              <el-input v-model="form.cos.secretKey" type="password" show-password />
            </el-form-item>
            <el-form-item label="Bucket">
              <el-input v-model="form.cos.bucket" placeholder="bucket-1250000000" />
            </el-form-item>
            <el-form-item label="外部域名">
              <el-input v-model="form.cos.domain" placeholder="可选，用于 CDN / 公开访问" />
            </el-form-item>
            <el-form-item label="公开读">
              <el-switch v-model="form.cos.publicRead" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="action-row">
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存配置</el-button>
        <el-button @click="loadConfig">重载</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorageConfig, updateStorageConfig, type StorageConfig } from '@/api/system/storage'

const loading = ref(false)
const providerTab = ref<'local' | 'minio' | 'oss' | 'cos'>('local')

const typeLabelMap: Record<string, string> = {
  local: '本地存储',
  minio: 'MinIO',
  oss: '阿里云 OSS',
  cos: '腾讯云 COS'
}

const createDefaultForm = (): StorageConfig => ({
  activeType: 'local',
  accessUrlExpireSeconds: 900,
  local: {
    basePath: './upload',
    publicRead: false
  },
  minio: {
    endpoint: '',
    accessKey: '',
    secretKey: '',
    bucket: '',
    domain: '',
    publicRead: false
  },
  oss: {
    endpoint: '',
    accessKeyId: '',
    accessKeySecret: '',
    bucket: '',
    domain: '',
    publicRead: false
  },
  cos: {
    region: '',
    secretId: '',
    secretKey: '',
    bucket: '',
    domain: '',
    publicRead: false
  }
})

const form = reactive<StorageConfig>(createDefaultForm())

const assignForm = (target: StorageConfig) => {
  Object.assign(form, createDefaultForm(), target)
  form.local = { ...createDefaultForm().local, ...(target.local || {}) }
  form.minio = { ...createDefaultForm().minio, ...(target.minio || {}) }
  form.oss = { ...createDefaultForm().oss, ...(target.oss || {}) }
  form.cos = { ...createDefaultForm().cos, ...(target.cos || {}) }
  providerTab.value = form.activeType
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getStorageConfig()
    assignForm(res.data || createDefaultForm())
  } catch (error) {
    ElMessage.error('获取存储配置失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await updateStorageConfig({
      ...form,
      local: { ...form.local },
      minio: { ...form.minio },
      oss: { ...form.oss },
      cos: { ...form.cos }
    })
    providerTab.value = form.activeType
    ElMessage.success('存储配置已保存')
  } catch (error) {
    ElMessage.error('保存存储配置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.storage-panel {
  padding: 24px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
}

.mb-4 {
  margin-bottom: 16px;
}

.provider-tabs {
  margin-top: 8px;
}

.action-row {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.form-tip {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

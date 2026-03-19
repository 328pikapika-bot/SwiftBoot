<template>
  <div class="page-container">
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <span class="material-symbols-outlined text-primary text-xl mr-2">settings_suggest</span>
            <span class="font-bold text-lg">AI 引擎与模型配置</span>
          </div>
          <div class="header-actions">
            <div v-if="currentConfig.provider" class="current-model-badge">
              <div class="badge-dot"></div>
              <span class="badge-label">当前运行:</span>
              <span class="badge-value">{{ getProviderLabel(currentConfig.provider) }} / {{ currentConfig.model }}</span>
            </div>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px" style="max-width: 800px">
        <el-alert
          title="配置说明"
          type="info"
          :closable="false"
          class="mb-6"
          show-icon
        >
          <template #default>
            <p>1. 选择大模型厂商后，系统会自动切换到对应的官方 API 地址。</p>
            <p>2. API Key 将以脱敏方式显示，如需修改请直接覆盖输入。</p>
            <p>3. 切换配置后，所有新建对话将立即生效。</p>
          </template>
        </el-alert>

        <el-form-item label="大模型厂商" prop="provider">
          <el-select 
            v-model="formData.provider" 
            placeholder="请选择大模型厂商" 
            @change="handleProviderChange"
            style="width: 100%"
          >
            <el-option
              v-for="item in providerOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span style="float: left">{{ item.label }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ item.desc }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="模型名称" prop="model">
          <el-select 
            v-model="formData.model" 
            placeholder="请选择具体模型" 
            :disabled="!formData.provider"
            style="width: 100%"
          >
            <el-option
              v-for="item in currentModelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            placeholder="请输入 API Key (仅修改时填写)"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">当前状态: {{ apiKeyStatus }}</div>
        </el-form-item>

        <el-form-item label="API 地址">
          <el-input v-model="formData.apiUrl" placeholder="支持自定义，留空则使用默认地址">
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">默认: {{ currentApiUrl }}</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" :icon="Check">保存配置</el-button>
          <el-button type="success" @click="handleTestConnection" :loading="testing" :icon="Link">测试连接</el-button>
          <el-button @click="resetForm" :icon="RefreshRight">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Check, RefreshRight, Key, Link } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 模型配置定义
const providerOptions = [
  { label: 'DeepSeek', value: 'deepseek', desc: '深度求索 (推荐)' },
  { label: 'Gemini', value: 'gemini', desc: 'Google Gemini (需魔法)' },
  { label: 'MiniMax', value: 'minimax', desc: '海螺 AI (国产)' }
]

const modelMap: Record<string, { label: string; value: string }[]> = {
  deepseek: [
    { label: 'DeepSeek Chat (V3)', value: 'deepseek-chat' },
    { label: 'DeepSeek Coder', value: 'deepseek-coder' }
  ],
  gemini: [
    { label: 'Gemini 2.0 Flash', value: 'gemini-2.0-flash' },
    { label: 'Gemini 1.5 Pro', value: 'gemini-1.5-pro' }
  ],
  minimax: [
    { label: 'MiniMax-M2.7 (推荐)', value: 'MiniMax-M2.7' },
    { label: 'MiniMax-M2.7-highspeed', value: 'MiniMax-M2.7-highspeed' },
    { label: 'MiniMax-M2.5', value: 'MiniMax-M2.5' },
    { label: 'MiniMax-M2.5-highspeed', value: 'MiniMax-M2.5-highspeed' },
    { label: 'MiniMax-M2.1', value: 'MiniMax-M2.1' },
    { label: 'MiniMax-M2.1-highspeed', value: 'MiniMax-M2.1-highspeed' },
    { label: 'MiniMax-M2', value: 'MiniMax-M2' }
  ]
}

const apiUrlMap: Record<string, string> = {
  deepseek: 'https://api.deepseek.com/chat/completions',
  gemini: 'https://generativelanguage.googleapis.com/v1beta/openai/chat/completions',
  minimax: 'https://api.minimaxi.com/anthropic/v1/messages'
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const testing = ref(false)
const hasApiKey = ref(false)

// 当前生效的配置（用于头部展示）
const currentConfig = reactive({
  provider: '',
  model: '',
  apiUrl: ''
})

const formData = reactive({
  provider: '',
  model: '',
  apiKey: '',
  apiUrl: ''
})

const rules: FormRules = {
  provider: [{ required: true, message: '请选择大模型厂商', trigger: 'change' }],
  model: [{ required: true, message: '请选择具体模型', trigger: 'change' }],
  apiKey: [{ 
    validator: (rule: any, value: any, callback: any) => {
      // 如果已有 Key 且未修改（为空），则校验通过
      if (hasApiKey.value && !value) {
        callback()
      } else if (!value) {
        callback(new Error('请输入 API Key'))
      } else {
        callback()
      }
    }, 
    trigger: 'blur' 
  }]
}

// 计算属性
const currentModelOptions = computed(() => {
  return formData.provider ? modelMap[formData.provider] || [] : []
})

const currentApiUrl = computed(() => {
  return formData.provider ? apiUrlMap[formData.provider] : ''
})

const apiKeyStatus = computed(() => {
  if (formData.apiKey) return '已输入新 Key'
  return hasApiKey.value ? '已配置 (安全隐藏)' : '未配置'
})

// 方法
const getProviderLabel = (value: string) => {
  const found = providerOptions.find(p => p.value === value)
  return found ? found.label : value
}

const handleProviderChange = () => {
  formData.model = ''
  // 切换厂商时，自动填充默认 URL 到输入框
  formData.apiUrl = apiUrlMap[formData.provider] || ''
  
  // 切换厂商时，通常需要重新输入 Key，除非巧合
  if (formData.provider !== currentConfig.provider) {
    hasApiKey.value = false
    formData.apiKey = ''
  }
}

const getConfig = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/ai/config', method: 'get' })
    if (res.data) {
      // 更新当前生效配置
      currentConfig.provider = res.data.provider || 'deepseek'
      currentConfig.model = res.data.model || 'deepseek-chat'
      currentConfig.apiUrl = res.data.apiUrl || ''
      
      // 更新表单数据
      formData.provider = res.data.provider || 'deepseek'
      formData.model = res.data.model || 'deepseek-chat'
      formData.apiUrl = res.data.apiUrl || ''
      
      // 处理 API Key
      if (res.data.apiKey && res.data.apiKey.includes('****')) {
        hasApiKey.value = true
        formData.apiKey = '' // 不回显脱敏的 Key，避免误保存
      } else {
        hasApiKey.value = !!res.data.apiKey
        formData.apiKey = res.data.apiKey || ''
      }
    }
  } catch (error) {
    console.error('获取配置失败:', error)
    ElMessage.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

const handleTestConnection = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 如果需要重输 key 但没输
      if (!formData.apiKey && (!hasApiKey.value || formData.provider !== currentConfig.provider)) {
        ElMessage.warning('测试连接需要有效的 API Key，请重新输入')
        return
      }
      
      testing.value = true
      try {
        const data: any = {
          provider: formData.provider,
          model: formData.model,
          apiUrl: formData.apiUrl
        }
        
        if (formData.apiKey) {
          data.apiKey = formData.apiKey
        }
        
        const res = await request({
          url: '/system/ai/config/test-connection',
          method: 'post',
          data
        })
        
        ElMessage.success(res.data || '连接成功！模型响应正常。')
      } catch (error: any) {
        ElMessage.error(error.message || '连接失败，请检查配置')
      } finally {
        testing.value = false
      }
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data: any = {
          provider: formData.provider,
          model: formData.model,
          apiUrl: formData.apiUrl
        }
        
        if (formData.apiKey) {
          data.apiKey = formData.apiKey
        } else if (hasApiKey.value && formData.provider === currentConfig.provider) {
           // 这是一个潜在问题：后端目前的 updateConfig 是全量覆盖
           ElMessage.warning('为保证安全，修改配置时请重新输入 API Key')
           loading.value = false
           return
        }

        await request({
          url: '/system/ai/config',
          method: 'put',
          data
        })
        
        ElMessage.success('保存成功')
        // 更新当前配置状态
        currentConfig.provider = formData.provider
        currentConfig.model = formData.model
        currentConfig.apiUrl = formData.apiUrl
        hasApiKey.value = true
        formData.apiKey = '' // 清空输入框
        
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  getConfig()
}

onMounted(() => {
  getConfig()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 24px;
  background-color: var(--el-bg-color-page);
  min-height: calc(100vh - 84px);
}

.config-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--el-border-color-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    display: flex;
    align-items: center;
    color: var(--el-text-color-primary);
  }
}

.current-model-badge {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(43, 43, 238, 0.1), rgba(79, 70, 229, 0.1));
  border: 1px solid rgba(43, 43, 238, 0.2);
  padding: 6px 16px;
  border-radius: 20px;
  
  .badge-dot {
    width: 8px;
    height: 8px;
    background-color: #10b981;
    border-radius: 50%;
    margin-right: 8px;
    box-shadow: 0 0 8px #10b981;
    animation: pulse 2s infinite;
  }
  
  .badge-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-right: 6px;
  }
  
  .badge-value {
    font-size: 13px;
    font-weight: 600;
    color: #2b2bee;
  }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin-top: 4px;
}

.mb-6 {
  margin-bottom: 24px;
}

.mr-2 {
  margin-right: 8px;
}
</style>

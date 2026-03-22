<template>
  <div class="config-page">
    <el-tabs v-model="activeTab" class="config-tabs">
      <el-tab-pane label="AI 配置" name="ai">
        <AiConfigPage />
      </el-tab-pane>
      <el-tab-pane label="存储配置" name="storage">
        <StorageConfigPanel />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiConfigPage from '@/views/tool/config/index.vue'
import StorageConfigPanel from '@/views/storage-config-panel.vue'

const activeTab = ref<'ai' | 'storage'>('ai')
</script>

<style scoped>
.config-page {
  min-height: calc(100vh - 84px);
}

:deep(.config-tabs > .el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
}
</style>

<template>
  <div class="config-page">
    <section class="config-center-hero">
      <div>
        <span class="hero-kicker">Configuration Center</span>
        <h1>平台配置管理</h1>
        <p>统一管理 AI 引擎、文件存储和治理策略，保持系统运行参数、基础能力和安全策略在同一处协同配置。</p>
      </div>
      <div class="hero-tags">
        <span class="hero-tag">AI 引擎</span>
        <span class="hero-tag">存储能力</span>
        <span class="hero-tag">治理策略</span>
      </div>
    </section>

    <el-tabs v-model="activeTab" class="config-tabs" stretch>
      <el-tab-pane label="AI 配置" name="ai" lazy>
        <AiConfigPage />
      </el-tab-pane>
      <el-tab-pane label="存储配置" name="storage" lazy>
        <StorageConfigPanel />
      </el-tab-pane>
      <el-tab-pane label="屏蔽词与回答规则" name="adminRule" lazy>
        <AdminPreRulePanel />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiConfigPage from '@/views/tool/config/index.vue'
import StorageConfigPanel from '@/views/storage-config-panel.vue'
import AdminPreRulePanel from '@/views/ai-admin-pre-rule-panel.vue'

const activeTab = ref<'ai' | 'storage' | 'adminRule'>('ai')
</script>

<style scoped>
.config-page {
  min-height: calc(100vh - 84px);
  background:
    radial-gradient(circle at top right, rgba(255, 245, 200, 0.68), transparent 20%),
    radial-gradient(circle at 8% 10%, rgba(219, 234, 254, 0.82), transparent 28%),
    linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
}

.config-center-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 24px 18px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #92400e;
  font-weight: 700;
}

.config-center-hero h1 {
  margin: 8px 0 10px;
  font-size: 32px;
  line-height: 1.08;
  color: #0f172a;
}

.config-center-hero p {
  margin: 0;
  max-width: 760px;
  color: #475569;
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(148, 163, 184, 0.18);
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

:deep(.config-tabs > .el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: transparent;
  border-bottom: 0;
}

:deep(.config-tabs > .el-tabs__header .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.config-tabs > .el-tabs__header .el-tabs__item) {
  height: 48px;
  border-radius: 14px 14px 0 0;
  color: #64748b;
  font-weight: 600;
}

:deep(.config-tabs > .el-tabs__header .el-tabs__item.is-active) {
  color: #0f172a;
  background: rgba(255, 255, 255, 0.72);
}

:deep(.config-tabs > .el-tabs__content) {
  padding-bottom: 24px;
}

:deep(.page-container),
:deep(.storage-panel),
:deep(.pre-rule-panel) {
  padding: 24px;
  background: transparent !important;
  min-height: auto !important;
}

:deep(.page-container > .config-card),
:deep(.storage-panel > .el-card),
:deep(.pre-rule-panel .hero-card),
:deep(.pre-rule-panel .module-card) {
  border-radius: 24px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
}

:deep(.page-container .config-card .el-card__body),
:deep(.storage-panel > .el-card .el-card__body),
:deep(.pre-rule-panel .module-card .el-card__body) {
  padding: 24px;
}

:deep(.page-container .el-alert),
:deep(.storage-panel .el-alert),
:deep(.pre-rule-panel .el-alert) {
  border-radius: 16px;
}

:deep(.page-container .el-button),
:deep(.storage-panel .el-button),
:deep(.pre-rule-panel .el-button) {
  border-radius: 14px;
}

:deep(.page-container .el-input__wrapper),
:deep(.page-container .el-select__wrapper),
:deep(.storage-panel .el-input__wrapper),
:deep(.storage-panel .el-select__wrapper),
:deep(.pre-rule-panel .el-input__wrapper),
:deep(.pre-rule-panel .el-select__wrapper) {
  border-radius: 14px;
}

:deep(.storage-panel .provider-tabs > .el-tabs__header),
:deep(.pre-rule-panel .module-card .el-tabs__header) {
  margin-bottom: 16px;
}

:deep(.storage-panel .action-row),
:deep(.page-container .config-footer),
:deep(.pre-rule-panel .footer-actions) {
  margin-top: 20px;
}

@media (max-width: 900px) {
  .config-center-hero {
    flex-direction: column;
  }

  .hero-tags {
    justify-content: flex-start;
  }

  :deep(.page-container),
  :deep(.storage-panel),
  :deep(.pre-rule-panel) {
    padding: 16px;
  }
}
</style>

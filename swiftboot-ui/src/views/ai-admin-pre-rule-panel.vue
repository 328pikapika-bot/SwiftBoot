<template>
  <div class="pre-rule-panel">
    <div class="hero-shell">
      <section class="hero-card">
        <div class="hero-copy">
          <span class="eyebrow">AI Governance</span>
          <h2>管理员治理规则</h2>
          <p>
            这里写下的规则会直接参与 AI 回答行为治理。既可以作为前置拦截条件，也可以作为回答约束，例如要求每次回答结尾追加固定信息。
          </p>
        </div>
        <div class="hero-metrics">
          <div class="metric-card">
            <span class="metric-label">治理开关</span>
            <strong>{{ form.enabled ? '已启用' : '已停用' }}</strong>
          </div>
          <div class="metric-card">
            <span class="metric-label">已启用规则</span>
            <strong>{{ enabledRuleCount }}</strong>
          </div>
          <div class="metric-card">
            <span class="metric-label">剩余容量</span>
            <strong>{{ remainingSlots }}</strong>
          </div>
        </div>
      </section>

      <section class="governance-grid">
        <el-card shadow="never" class="config-card stage-card">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-kicker">Runtime Policy</span>
                <h3>运行时治理开关</h3>
              </div>
              <el-switch v-model="form.enabled" inline-prompt active-text="开" inactive-text="关" />
            </div>
          </template>

          <div class="stage-fields">
            <div class="field-block">
              <label>统一拦截文案</label>
              <el-input
                v-model="form.interceptionMessage"
                type="textarea"
                :rows="3"
                maxlength="120"
                show-word-limit
                placeholder="当规则被用于拦截时，直接返回给用户的文案"
              />
            </div>
            <div class="limit-strip">
              <span>最多 {{ form.maxRules }} 条规则</span>
              <span>单条内容最多 {{ form.maxRuleLength }} 字</span>
              <span>优先级数值越大越先执行</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="config-card guidance-card">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-kicker">Authoring Guide</span>
                <h3>编写方式</h3>
              </div>
            </div>
          </template>

          <div class="guide-list">
            <div class="guide-item">
              <strong>直接写规则</strong>
              <p>可以直接写自然语言规则，例如“每个问题回答结尾添加 联系方式：17334981104”。</p>
            </div>
            <div class="guide-item">
              <strong>`regex:` / `keyword:`</strong>
              <p>当你确实想做匹配拦截时，再使用 `regex:` 或 `keyword:` 前缀；中文冒号 `：` 也支持。</p>
            </div>
            <div class="guide-item">
              <strong>治理优先级</strong>
              <p>管理员规则会先参与运行时治理，再进入系统内置安全基线，适合临时收紧或追加统一答复要求。</p>
            </div>
          </div>
        </el-card>
      </section>
    </div>

    <el-card shadow="never" class="config-card rules-card">
      <template #header>
        <div class="rules-head">
          <div>
            <span class="section-kicker">Rule Ledger</span>
            <h3>规则列表</h3>
          </div>
          <div class="rules-actions">
            <el-tag type="info">总数 {{ form.rules.length }} / {{ form.maxRules }}</el-tag>
            <el-button type="primary" plain :disabled="form.rules.length >= form.maxRules" @click="handleAddRule">
              新增规则
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="!form.rules.length" class="empty-state">
        <el-empty description="还没有管理员前置规则">
          <el-button type="primary" @click="handleAddRule">创建第一条规则</el-button>
        </el-empty>
      </div>

      <div v-else class="rule-stack">
        <article v-for="(rule, index) in sortedRules" :key="rule.id" class="rule-card">
          <div class="rule-card-top">
            <div class="rule-index">#{{ index + 1 }}</div>
            <div class="rule-main">
              <div class="rule-header">
                <el-input v-model="rule.ruleName" maxlength="40" placeholder="规则名称，例如：统一附加联系方式" />
                <el-tag :type="rule.enabled ? 'success' : 'info'" effect="plain">
                  {{ rule.enabled ? '启用中' : '已停用' }}
                </el-tag>
              </div>
              <div class="rule-meta">
                <div class="meta-field">
                  <span>优先级</span>
                  <el-input-number v-model="rule.priority" :min="0" :max="999" controls-position="right" />
                </div>
                <div class="meta-field switch-field">
                  <span>是否启用</span>
                  <el-switch v-model="rule.enabled" inline-prompt active-text="开" inactive-text="关" />
                </div>
                <el-button text type="danger" @click="handleRemoveRule(rule.id)">删除</el-button>
              </div>
            </div>
          </div>

          <div class="rule-editor">
            <label>规则内容</label>
            <el-input
              v-model="rule.ruleContent"
              type="textarea"
              :rows="4"
              :maxlength="form.maxRuleLength"
              show-word-limit
              placeholder="直接写规则，例如：
每个问题回答结尾添加 联系方式：17334981104
keyword:删库
regex:忽略(之前|上述).*(规则|限制)"
            />
          </div>
        </article>
      </div>

      <div class="footer-actions">
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存规则</el-button>
        <el-button :loading="loading" @click="loadConfig">重载</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminPreRuleConfig,
  updateAdminPreRuleConfig,
  type AdminPreRuleConfig,
  type AdminPreRuleItem
} from '@/api/system/adminPreRules'

const loading = ref(false)

const createRule = (): AdminPreRuleItem => ({
  id: `rule_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
  ruleName: '',
  ruleContent: '',
  enabled: true,
  priority: 100
})

const createDefaultForm = (): AdminPreRuleConfig => ({
  enabled: true,
  interceptionMessage: '⚠️ 管理员安全前置规则已命中，当前问题不允许直接回答，请调整问题内容后重试。',
  rules: [],
  maxRules: 10,
  maxRuleLength: 200
})

const form = reactive<AdminPreRuleConfig>(createDefaultForm())

const assignForm = (target?: Partial<AdminPreRuleConfig>) => {
  Object.assign(form, createDefaultForm(), target)
  form.rules = [...(target?.rules || [])]
}

const enabledRuleCount = computed(() => form.rules.filter(rule => rule.enabled).length)
const remainingSlots = computed(() => Math.max((form.maxRules || 10) - form.rules.length, 0))
const sortedRules = computed(() => [...form.rules].sort((a, b) => (b.priority || 0) - (a.priority || 0)))

const validateBeforeSubmit = () => {
  if (form.rules.length > form.maxRules) {
    ElMessage.warning(`最多只能配置 ${form.maxRules} 条规则`)
    return false
  }
  for (const rule of form.rules) {
    if (!rule.ruleName.trim()) {
      ElMessage.warning('规则名称不能为空')
      return false
    }
    if (!rule.ruleContent.trim()) {
      ElMessage.warning(`规则「${rule.ruleName || '未命名规则'}」内容不能为空`)
      return false
    }
    if (rule.ruleContent.length > form.maxRuleLength) {
      ElMessage.warning(`规则「${rule.ruleName}」内容不能超过 ${form.maxRuleLength} 字`)
      return false
    }
  }
  return true
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getAdminPreRuleConfig()
    assignForm(res.data || createDefaultForm())
  } catch (error) {
    ElMessage.error('获取管理员前置规则失败')
  } finally {
    loading.value = false
  }
}

const handleAddRule = () => {
  if (form.rules.length >= form.maxRules) {
    ElMessage.warning(`最多只能配置 ${form.maxRules} 条规则`)
    return
  }
  form.rules.unshift(createRule())
}

const handleRemoveRule = async (id: string) => {
  const target = form.rules.find(rule => rule.id === id)
  if (!target) return
  try {
    await ElMessageBox.confirm(`确定删除规则「${target.ruleName || '未命名规则'}」吗？`, '删除规则', {
      type: 'warning'
    })
  } catch {
    return
  }
  form.rules = form.rules.filter(rule => rule.id !== id)
  ElMessage.success('规则已删除')
}

const handleSubmit = async () => {
  if (!validateBeforeSubmit()) return
  loading.value = true
  try {
    await updateAdminPreRuleConfig({
      enabled: form.enabled,
      interceptionMessage: form.interceptionMessage,
      rules: [...sortedRules.value]
    })
    await loadConfig()
    ElMessage.success('管理员前置规则已保存')
  } catch (error: any) {
    ElMessage.error(error?.message || '保存管理员前置规则失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.pre-rule-panel {
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(207, 250, 254, 0.95), transparent 28%),
    linear-gradient(180deg, #f7fafc 0%, #edf4f7 100%);
  min-height: calc(100vh - 84px);
}

.hero-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 20px;
}

.hero-card,
.config-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
}

.hero-card {
  display: grid;
  grid-template-columns: 1.7fr 1fr;
  gap: 20px;
  padding: 28px;
  position: relative;
  overflow: hidden;
}

.hero-card::after {
  content: '';
  position: absolute;
  inset: auto -60px -80px auto;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(20, 184, 166, 0.22), transparent 68%);
}

.eyebrow,
.section-kicker {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #0f766e;
  font-weight: 700;
}

.hero-copy h2,
.section-head h3,
.rules-head h3 {
  margin: 8px 0 10px;
  font-size: 26px;
  line-height: 1.1;
  color: #0f172a;
}

.hero-copy p {
  margin: 0;
  max-width: 680px;
  color: #475569;
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-content: start;
}

.metric-card {
  padding: 18px 16px;
  border-radius: 18px;
  background: linear-gradient(160deg, #0f172a, #164e63);
  color: #f8fafc;
}

.metric-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: rgba(248, 250, 252, 0.72);
}

.metric-card strong {
  font-size: 24px;
}

.governance-grid {
  display: grid;
  grid-template-columns: 1.3fr 0.9fr;
  gap: 18px;
}

.section-head,
.rules-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.stage-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-block label,
.rule-editor label {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.limit-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.limit-strip span {
  padding: 8px 12px;
  border-radius: 999px;
  background: #ecfeff;
  color: #155e75;
  font-size: 12px;
}

.guide-list {
  display: grid;
  gap: 14px;
}

.guide-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fcfffe, #f4f9f8);
  border: 1px solid rgba(15, 118, 110, 0.08);
}

.guide-item strong {
  display: block;
  margin-bottom: 6px;
  color: #134e4a;
}

.guide-item p {
  margin: 0;
  color: #51606f;
  line-height: 1.7;
}

.rules-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.empty-state {
  padding: 24px 0 8px;
}

.rule-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rule-card {
  border-radius: 22px;
  padding: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(244, 249, 248, 0.94)),
    linear-gradient(90deg, rgba(20, 184, 166, 0.22), rgba(15, 23, 42, 0.04));
  border: 1px solid rgba(15, 23, 42, 0.07);
}

.rule-card-top {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 16px;
  margin-bottom: 14px;
}

.rule-index {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f766e, #164e63);
  color: #fff;
  font-weight: 700;
}

.rule-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-header {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.rule-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
}

.meta-field {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-size: 13px;
}

.switch-field {
  margin-left: auto;
}

.rule-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

@media (max-width: 1100px) {
  .hero-card,
  .governance-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .pre-rule-panel {
    padding: 16px;
  }

  .hero-card {
    padding: 20px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .rule-card-top,
  .rule-header {
    grid-template-columns: 1fr;
  }

  .rule-index {
    width: 48px;
    height: 48px;
  }

  .switch-field {
    margin-left: 0;
  }
}
</style>

<template>
  <div class="pre-rule-panel">
    <section class="hero-card">
      <div class="hero-copy">
        <span class="eyebrow">AI Governance Console</span>
        <h2>屏蔽词与回答规则</h2>
        <p>
          屏蔽词模块负责硬拦截，命中后直接截断且不调用 LLM；规则列表模块负责回答治理，用来统一附加说明、限制回答风格和输出格式。
        </p>
      </div>
      <div class="hero-metrics">
        <button type="button" class="metric-card metric-button" @click="scrollToSection('category')">
          <span class="metric-label">启用分类</span>
          <strong>{{ overview.enabledCategoryCount }}</strong>
        </button>
        <button type="button" class="metric-card metric-button" @click="scrollToSection('word')">
          <span class="metric-label">启用屏蔽词</span>
          <strong>{{ overview.enabledWordCount }}</strong>
        </button>
        <button type="button" class="metric-card metric-button" @click="scrollToSection('rule')">
          <span class="metric-label">启用规则</span>
          <strong>{{ enabledRuleCount }}</strong>
        </button>
      </div>
    </section>

    <el-card ref="blockModuleRef" shadow="never" class="module-card">
      <template #header>
        <div class="section-head">
          <div>
            <span class="section-kicker">Hard Blocking</span>
            <h3>屏蔽词模块</h3>
          </div>
          <div class="head-actions">
            <el-button plain @click="openCategoryDialog()">新增分类</el-button>
            <el-button plain @click="router.push('/monitor/ai-block-hit')">前往命中日志</el-button>
            <el-button type="primary" :disabled="!overview.categories.length" @click="openBatchDrawer()">
              批量新增屏蔽词
            </el-button>
          </div>
        </div>
      </template>

      <el-alert type="warning" :closable="false" show-icon class="mb-4">
        <template #default>
          <p>1. 屏蔽词命中后会直接截断回答，不调用 LLM、RAG 和后续问答链路。</p>
          <p>2. 当前 1.0 版本只保留“包含匹配”，降低配置心智负担。</p>
          <p>3. 每个分类卡片只展示前 3 个屏蔽词预览，完整词库通过“查看全部”管理。</p>
        </template>
      </el-alert>

      <div class="toolbar-row">
        <el-input v-model="categoryKeyword" clearable placeholder="搜索分类名称或编码" style="width: 280px" />
        <el-button :loading="blockLoading" @click="loadBlockOverview">重载数据</el-button>
      </div>

      <div v-if="!filteredCategories.length" class="empty-state">
        <el-empty description="还没有屏蔽词分类">
          <el-button type="primary" @click="openCategoryDialog()">创建第一个分类</el-button>
        </el-empty>
      </div>

      <div ref="categorySectionRef" v-else class="category-grid">
        <article v-for="category in filteredCategories" :key="category.id" class="category-card">
          <div class="category-top">
            <div>
              <h4>{{ category.categoryName }}</h4>
              <p class="category-subline">
                <span class="subline-code">{{ category.categoryCode }}</span>
                <span v-if="category.dictDataId" class="subline-dot">·</span>
                <span v-if="category.dictDataId">关联字典</span>
              </p>
            </div>
            <el-tag :type="category.status === 0 ? 'success' : 'info'" effect="plain">
              {{ category.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </div>

          <div class="meta-strip">
            <span>词数 {{ category.wordCount }}</span>
            <span>排序 {{ category.sort }}</span>
          </div>

          <div class="preview-shell">
            <template v-if="category.previewWords.length">
              <el-tag
                v-for="word in category.previewWords"
                :key="word"
                class="preview-tag"
                effect="plain"
              >
                {{ word }}
              </el-tag>
            </template>
            <span v-else class="preview-empty">当前分类还没有屏蔽词</span>
          </div>

          <div class="card-actions">
            <el-button class="view-button" type="primary" @click="openWordListDialog(category)">查看词库</el-button>
            <div class="card-link-group">
              <el-button text @click="openWordDialog(category)">新增屏蔽词</el-button>
              <el-button text type="primary" @click="openCategoryDialog(category)">编辑分类</el-button>
              <el-button text type="danger" @click="handleDeleteCategory(category)">删除分类</el-button>
            </div>
          </div>
        </article>
      </div>
    </el-card>

    <div id="rule-section" class="section-anchor">
    <el-card shadow="never" class="module-card rules-card">
      <template #header>
        <div class="section-head">
          <div>
            <span class="section-kicker">Soft Governance</span>
            <h3>规则列表模块</h3>
          </div>
          <div class="head-actions">
            <el-tag type="info">总数 {{ ruleForm.rules.length }} / {{ ruleForm.maxRules }}</el-tag>
            <el-button type="primary" plain :disabled="ruleForm.rules.length >= ruleForm.maxRules" @click="handleAddRule">
              新增规则
            </el-button>
          </div>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #default>
          <p>1. 规则列表模块只做回答治理，不承担屏蔽词硬拦截职责。</p>
          <p>2. 直接写自然语言规则即可，例如“每个回答结尾添加 联系方式：17334981104”。</p>
          <p>3. 规则按优先级排序执行，数值越大越靠前。</p>
        </template>
      </el-alert>

      <div ref="ruleSectionRef"></div>

      <div v-if="!ruleForm.rules.length" class="empty-state">
        <el-empty description="还没有回答治理规则">
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
              :maxlength="ruleForm.maxRuleLength"
              show-word-limit
              placeholder="直接写规则，例如：
每个回答结尾添加 联系方式：17334981104
回答控制在三段以内"
            />
          </div>
        </article>
      </div>

      <div class="footer-actions">
        <el-button type="primary" :loading="ruleLoading" @click="handleSubmitRules">保存规则</el-button>
        <el-button :loading="ruleLoading" @click="loadRuleConfig">重载</el-button>
      </div>
    </el-card>
    </div>

    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.isEdit ? '编辑分类' : '新增分类'" width="820px">
      <div class="dialog-lead">
        <p>分类编码由字典统一维护，这里只负责选择字典项并挂到屏蔽词模块中，避免页面里再手填编码。</p>
      </div>
      <el-form label-width="90px" class="dialog-form">
        <el-form-item label="分类字典" required>
          <el-select
            v-model="categoryDialog.form.dictDataId"
            placeholder="请选择字典项"
            style="width: 100%"
            @change="handleCategoryDictChange"
          >
            <el-option
              v-for="item in categoryDictOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="categoryDialog.form.status"
            :active-value="0"
            :inactive-value="1"
            inline-prompt
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryDialog.form.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="categoryDialog.form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="categoryDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="blockLoading" @click="handleSaveCategory">保存分类</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="singleWordDialog.visible" :title="singleWordDialog.isEdit ? '编辑屏蔽词' : '新增屏蔽词'" width="820px">
      <div class="dialog-lead">
        <p>单条新增适合补一个临时词；大批量维护时，建议回到分类卡片使用“批量添加屏蔽词”。</p>
      </div>
      <el-form label-width="90px" class="dialog-form">
        <el-form-item label="所属分类" required>
          <el-select v-model="singleWordDialog.form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in overview.categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="屏蔽词" required>
          <el-input v-model="singleWordDialog.form.wordText" maxlength="80" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="singleWordDialog.form.status"
            :active-value="0"
            :inactive-value="1"
            inline-prompt
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="singleWordDialog.form.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="singleWordDialog.form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="singleWordDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="blockLoading" @click="handleSaveSingleWord">保存词条</el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="batchDrawer.visible" title="新增屏蔽词" size="820px">
      <div class="drawer-tip">
        <p>这里是一口气录入多个屏蔽词的主入口。一行一个，保存时会自动去空行、去重，并跳过已存在的词条。</p>
      </div>
      <el-form label-width="90px">
        <el-form-item label="所属分类" required>
          <el-select v-model="batchDrawer.form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in overview.categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="屏蔽词列表" required>
          <el-input
            v-model="batchDrawer.form.wordLines"
            type="textarea"
            :rows="14"
            placeholder="一行一个，例如：
删库
删表
drop database
truncate table"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchDrawer.form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="batchDrawer.visible = false">取消</el-button>
          <el-button type="primary" :loading="blockLoading" @click="handleBatchSaveWords">批量保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="wordListDialog.visible" :title="wordListDialog.title" width="820px">
      <div class="toolbar-row">
        <el-input v-model="wordListDialog.keyword" clearable placeholder="搜索当前分类下的屏蔽词" style="width: 260px" />
        <el-button type="primary" @click="openSingleWordFromList">新增屏蔽词</el-button>
        <el-button plain @click="openBatchDrawer(wordListDialog.category)">批量新增</el-button>
        <el-button
          type="danger"
          plain
          :disabled="selectedWordIds.length === 0"
          @click="handleBatchDeleteWords"
        >
          一键删除（{{ selectedWordIds.length }}）
        </el-button>
      </div>
      <el-table
        :data="pagedDialogWords"
        class="govern-table"
        max-height="420"
        @selection-change="handleWordSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="屏蔽词" prop="wordText" min-width="200" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" effect="plain">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="90" align="center" />
        <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button text type="primary" @click="openWordDialog(wordListDialog.category, row)">编辑</el-button>
              <el-button text type="danger" @click="handleDeleteWord(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:page="wordListPage.pageNum"
        v-model:limit="wordListPage.pageSize"
        :total="dialogWords.length"
        :auto-scroll="false"
        layout="total, sizes, prev, pager, next"
        @pagination="handleWordDialogPagination"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDicts } from '@/api/system/dict/data'
import { useRouter } from 'vue-router'
import Pagination from '@/components/Pagination/index.vue'
import {
  batchCreateAiBlockWords,
  createAiBlockCategory,
  createAiBlockWord,
  deleteAiBlockCategory,
  deleteAiBlockWord,
  getAiBlockOverview,
  updateAiBlockCategory,
  updateAiBlockWord,
  type AiBlockCategory,
  type AiBlockCategoryPayload,
  type AiBlockOverview,
  type AiBlockWord,
  type AiBlockWordBatchPayload,
  type AiBlockWordPayload
} from '@/api/system/blockWords'
import {
  getAdminPreRuleConfig,
  updateAdminPreRuleConfig,
  type AdminPreRuleConfig,
  type AdminPreRuleItem
} from '@/api/system/adminPreRules'

const blockLoading = ref(false)
const ruleLoading = ref(false)
const categoryKeyword = ref('')
const categoryDictOptions = ref<Array<{ id: number; label: string; value: string }>>([])
const router = useRouter()
const categorySectionRef = ref<HTMLElement>()
const blockModuleRef = ref<any>()

const createDefaultOverview = (): AiBlockOverview => ({
  enabledCategoryCount: 0,
  enabledWordCount: 0,
  totalCategoryCount: 0,
  totalWordCount: 0,
  categories: [],
  words: []
})

const overview = reactive<AiBlockOverview>(createDefaultOverview())

const categoryDialog = reactive({
  visible: false,
  isEdit: false,
  form: {
    id: undefined as number | undefined,
    dictDataId: undefined as number | undefined,
    categoryName: '',
    categoryCode: '',
    status: 0,
    sort: 100,
    remark: ''
  }
})

const singleWordDialog = reactive({
  visible: false,
  isEdit: false,
  form: {
    id: undefined as number | undefined,
    categoryId: undefined as number | undefined,
    wordText: '',
    status: 0,
    sort: 100,
    remark: ''
  }
})

const batchDrawer = reactive({
  visible: false,
  form: {
    categoryId: undefined as number | undefined,
    wordLines: '',
    remark: ''
  }
})

const wordListDialog = reactive({
  visible: false,
  title: '',
  keyword: '',
  category: null as AiBlockCategory | null
})
const selectedWordIds = ref<number[]>([])

const wordListPage = reactive({
  pageNum: 1,
  pageSize: 10
})

const createRule = (): AdminPreRuleItem => ({
  id: `rule_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
  ruleName: '',
  ruleContent: '',
  enabled: true,
  priority: 100
})

const createDefaultRuleForm = (): AdminPreRuleConfig => ({
  enabled: true,
  interceptionMessage: '当前问题包含受限关键词，无法回答，请调整提问内容后重试。',
  rules: [],
  maxRules: 10,
  maxRuleLength: 200
})

const ruleForm = reactive<AdminPreRuleConfig>(createDefaultRuleForm())

const filteredCategories = computed(() => {
  const keyword = categoryKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return overview.categories
  }
  return overview.categories.filter(category =>
    category.categoryName.toLowerCase().includes(keyword) || category.categoryCode.toLowerCase().includes(keyword)
  )
})

const dialogWords = computed(() => {
  if (!wordListDialog.category) {
    return []
  }
  const keyword = wordListDialog.keyword.trim().toLowerCase()
  return overview.words.filter(word => {
    if (word.categoryId !== wordListDialog.category?.id) return false
    if (!keyword) return true
    return word.wordText.toLowerCase().includes(keyword)
  })
})

const pagedDialogWords = computed(() => {
  const start = (wordListPage.pageNum - 1) * wordListPage.pageSize
  return dialogWords.value.slice(start, start + wordListPage.pageSize)
})

const enabledRuleCount = computed(() => ruleForm.rules.filter(rule => rule.enabled).length)
const sortedRules = computed(() => [...ruleForm.rules].sort((a, b) => (b.priority || 0) - (a.priority || 0)))
const assignOverview = (target?: Partial<AiBlockOverview>) => {
  Object.assign(overview, createDefaultOverview(), target)
  overview.categories = [...(target?.categories || [])]
  overview.words = [...(target?.words || [])]
}

const assignRuleForm = (target?: Partial<AdminPreRuleConfig>) => {
  Object.assign(ruleForm, createDefaultRuleForm(), target)
  ruleForm.rules = [...(target?.rules || [])]
}

const resetCategoryForm = () => {
  categoryDialog.form = {
    id: undefined,
    dictDataId: undefined,
    categoryName: '',
    categoryCode: '',
    status: 0,
    sort: 100,
    remark: ''
  }
}

const resetSingleWordForm = () => {
  singleWordDialog.form = {
    id: undefined,
    categoryId: overview.categories[0]?.id,
    wordText: '',
    status: 0,
    sort: 100,
    remark: ''
  }
}

const resetBatchDrawer = () => {
  batchDrawer.form = {
    categoryId: overview.categories[0]?.id,
    wordLines: '',
    remark: ''
  }
}

const loadBlockOverview = async () => {
  blockLoading.value = true
  try {
    const res = await getAiBlockOverview()
    assignOverview(res.data || createDefaultOverview())
    if (!singleWordDialog.visible && !singleWordDialog.form.categoryId && overview.categories.length) {
      singleWordDialog.form.categoryId = overview.categories[0].id
    }
    if (!batchDrawer.visible && !batchDrawer.form.categoryId && overview.categories.length) {
      batchDrawer.form.categoryId = overview.categories[0].id
    }
  } catch {
    ElMessage.error('获取屏蔽词数据失败')
  } finally {
    blockLoading.value = false
  }
}

const loadRuleConfig = async () => {
  ruleLoading.value = true
  try {
    const res = await getAdminPreRuleConfig()
    assignRuleForm(res.data || createDefaultRuleForm())
  } catch {
    ElMessage.error('获取回答规则失败')
  } finally {
    ruleLoading.value = false
  }
}

const openCategoryDialog = (row?: AiBlockCategory) => {
  categoryDialog.visible = true
  categoryDialog.isEdit = !!row
  if (!row) {
    resetCategoryForm()
    return
  }
  categoryDialog.form = {
    id: row.id,
    dictDataId: row.dictDataId,
    categoryName: row.categoryName,
    categoryCode: row.categoryCode,
    status: row.status,
    sort: row.sort,
    remark: row.remark || ''
  }
}

const openWordDialog = (category?: AiBlockCategory | null, row?: AiBlockWord) => {
  singleWordDialog.visible = true
  singleWordDialog.isEdit = !!row
  if (!row) {
    resetSingleWordForm()
    if (category?.id) {
      singleWordDialog.form.categoryId = category.id
    }
    return
  }
  singleWordDialog.form = {
    id: row.id,
    categoryId: row.categoryId,
    wordText: row.wordText,
    status: row.status,
    sort: row.sort,
    remark: row.remark || ''
  }
}

const openBatchDrawer = (category?: AiBlockCategory | null) => {
  batchDrawer.visible = true
  resetBatchDrawer()
  if (category?.id) {
    batchDrawer.form.categoryId = category.id
  }
}

const openWordListDialog = (category: AiBlockCategory) => {
  wordListDialog.visible = true
  wordListDialog.keyword = ''
  wordListDialog.category = category
  wordListDialog.title = `${category.categoryName} · 全部屏蔽词`
  wordListPage.pageNum = 1
  selectedWordIds.value = []
}

const openSingleWordFromList = () => {
  openWordDialog(wordListDialog.category)
}

const handleWordDialogPagination = () => {
  if (wordListPage.pageNum < 1) {
    wordListPage.pageNum = 1
  }
}

const scrollToSection = async (type: 'category' | 'word' | 'rule') => {
  await nextTick()
  if (type === 'rule') {
    document.getElementById('rule-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return
  }
  if (type === 'category') {
    categorySectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return
  }
  const target = blockModuleRef.value?.$el ?? blockModuleRef.value
  target?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
}

watch(
  () => wordListDialog.keyword,
  () => {
    wordListPage.pageNum = 1
  }
)

const handleSaveCategory = async () => {
  if (!categoryDialog.form.dictDataId) {
    ElMessage.warning('请选择分类字典')
    return
  }
  blockLoading.value = true
  try {
    const payload: AiBlockCategoryPayload = {
      ...categoryDialog.form,
      categoryName: categoryDialog.form.categoryName.trim(),
      categoryCode: categoryDialog.form.categoryCode.trim()
    }
    if (categoryDialog.isEdit) {
      await updateAiBlockCategory(payload)
    } else {
      await createAiBlockCategory(payload)
    }
    categoryDialog.visible = false
    await loadBlockOverview()
    ElMessage.success('分类已保存')
  } catch (error: any) {
    ElMessage.error(error?.message || '保存分类失败')
  } finally {
    blockLoading.value = false
  }
}

const handleCategoryDictChange = (dictDataId?: number) => {
  if (!dictDataId) {
    categoryDialog.form.categoryName = ''
    categoryDialog.form.categoryCode = ''
    return
  }
  const target = categoryDictOptions.value.find(item => item.id === dictDataId)
  if (!target) return
  categoryDialog.form.categoryName = target.label
  categoryDialog.form.categoryCode = target.value
}

const loadCategoryDictOptions = async () => {
  try {
    const res: any = await getDicts('ai_block_category')
    categoryDictOptions.value = (res.data || []).map((item: any) => ({
      id: item.id,
      label: item.dictLabel,
      value: item.dictValue
    }))
  } catch {
    categoryDictOptions.value = []
  }
}

const handleSaveSingleWord = async () => {
  if (!singleWordDialog.form.categoryId || !singleWordDialog.form.wordText.trim()) {
    ElMessage.warning('请选择分类并输入屏蔽词')
    return
  }
  blockLoading.value = true
  try {
    const payload: AiBlockWordPayload = {
      ...singleWordDialog.form,
      wordText: singleWordDialog.form.wordText.trim()
    }
    if (singleWordDialog.isEdit) {
      await updateAiBlockWord(payload)
    } else {
      await createAiBlockWord(payload)
    }
    singleWordDialog.visible = false
    await loadBlockOverview()
    ElMessage.success('屏蔽词已保存')
  } catch (error: any) {
    ElMessage.error(error?.message || '保存屏蔽词失败')
  } finally {
    blockLoading.value = false
  }
}

const handleBatchSaveWords = async () => {
  if (!batchDrawer.form.categoryId || !batchDrawer.form.wordLines.trim()) {
    ElMessage.warning('请选择分类并输入屏蔽词列表')
    return
  }
  blockLoading.value = true
  try {
    const payload: AiBlockWordBatchPayload = {
      categoryId: batchDrawer.form.categoryId,
      wordLines: batchDrawer.form.wordLines,
      remark: batchDrawer.form.remark
    }
    await batchCreateAiBlockWords(payload)
    batchDrawer.visible = false
    await loadBlockOverview()
    ElMessage.success('屏蔽词已批量保存')
  } catch (error: any) {
    ElMessage.error(error?.message || '批量保存失败')
  } finally {
    blockLoading.value = false
  }
}

const handleDeleteCategory = async (row: AiBlockCategory) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？`, '删除分类', { type: 'warning' })
  } catch {
    return
  }
  blockLoading.value = true
  try {
    await deleteAiBlockCategory(row.id)
    await loadBlockOverview()
    ElMessage.success('分类已删除')
  } catch (error: any) {
    ElMessage.error(error?.message || '删除分类失败')
  } finally {
    blockLoading.value = false
  }
}

const handleDeleteWord = async (row: AiBlockWord) => {
  try {
    await ElMessageBox.confirm(`确定删除屏蔽词「${row.wordText}」吗？`, '删除屏蔽词', { type: 'warning' })
  } catch {
    return
  }
  blockLoading.value = true
  try {
    await deleteAiBlockWord(row.id)
    await loadBlockOverview()
    ElMessage.success('屏蔽词已删除')
  } catch (error: any) {
    ElMessage.error(error?.message || '删除屏蔽词失败')
  } finally {
    blockLoading.value = false
  }
}

const handleWordSelectionChange = (rows: AiBlockWord[]) => {
  selectedWordIds.value = rows.map(row => row.id)
}

const handleBatchDeleteWords = async () => {
  if (selectedWordIds.value.length === 0) {
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除已勾选的 ${selectedWordIds.value.length} 个屏蔽词吗？`, '批量删除', {
      type: 'warning'
    })
  } catch {
    return
  }
  blockLoading.value = true
  try {
    for (const id of selectedWordIds.value) {
      await deleteAiBlockWord(id)
    }
    selectedWordIds.value = []
    await loadBlockOverview()
    ElMessage.success('已删除勾选的屏蔽词')
  } catch (error: any) {
    ElMessage.error(error?.message || '批量删除失败')
  } finally {
    blockLoading.value = false
  }
}

const handleAddRule = () => {
  if (ruleForm.rules.length >= ruleForm.maxRules) {
    ElMessage.warning(`最多只能配置 ${ruleForm.maxRules} 条规则`)
    return
  }
  ruleForm.rules.unshift(createRule())
}

const handleRemoveRule = async (id: string) => {
  const target = ruleForm.rules.find(rule => rule.id === id)
  if (!target) return
  try {
    await ElMessageBox.confirm(`确定删除规则「${target.ruleName || '未命名规则'}」吗？`, '删除规则', { type: 'warning' })
  } catch {
    return
  }
  ruleForm.rules = ruleForm.rules.filter(rule => rule.id !== id)
  ElMessage.success('规则已删除')
}

const validateRules = () => {
  if (ruleForm.rules.length > ruleForm.maxRules) {
    ElMessage.warning(`最多只能配置 ${ruleForm.maxRules} 条规则`)
    return false
  }
  for (const rule of ruleForm.rules) {
    if (!rule.ruleName.trim()) {
      ElMessage.warning('规则名称不能为空')
      return false
    }
    if (!rule.ruleContent.trim()) {
      ElMessage.warning(`规则「${rule.ruleName}」内容不能为空`)
      return false
    }
  }
  return true
}

const handleSubmitRules = async () => {
  if (!validateRules()) return
  ruleLoading.value = true
  try {
    await updateAdminPreRuleConfig({
      enabled: true,
      interceptionMessage: ruleForm.interceptionMessage,
      rules: [...sortedRules.value]
    })
    await loadRuleConfig()
    ElMessage.success('回答规则已保存')
  } catch (error: any) {
    ElMessage.error(error?.message || '保存回答规则失败')
  } finally {
    ruleLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategoryDictOptions(), loadBlockOverview(), loadRuleConfig()])
})
</script>

<style scoped>
.pre-rule-panel {
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(255, 245, 200, 0.72), transparent 22%),
    radial-gradient(circle at 10% 10%, rgba(205, 236, 255, 0.82), transparent 30%),
    linear-gradient(180deg, #f7f7f2 0%, #edf2f7 100%);
  min-height: calc(100vh - 84px);
}

.hero-card,
.module-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
}

.hero-card {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 20px;
  padding: 28px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.hero-card::after {
  content: '';
  position: absolute;
  inset: auto -50px -90px auto;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.18), transparent 68%);
}

.eyebrow,
.section-kicker {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #92400e;
  font-weight: 700;
}

.hero-copy h2,
.section-head h3 {
  margin: 8px 0 10px;
  font-size: 28px;
  line-height: 1.1;
  color: #0f172a;
}

.hero-copy p {
  margin: 0;
  max-width: 700px;
  line-height: 1.8;
  color: #475569;
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
  background: linear-gradient(160deg, #1f2937, #7c2d12);
  color: #fff;
}

.metric-button {
  appearance: none;
  border: 0;
  outline: none;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(124, 45, 18, 0.18);
}

.metric-button:focus-visible {
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.22), 0 14px 30px rgba(124, 45, 18, 0.18);
}

.metric-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.metric-card strong {
  font-size: 24px;
}

.module-card {
  margin-bottom: 20px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.head-actions,
.toolbar-row,
.drawer-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.head-actions {
  flex-wrap: wrap;
}

.drawer-footer,
.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.drawer-footer :deep(.el-button),
.dialog-footer-actions :deep(.el-button) {
  min-width: 108px;
}

.toolbar-row {
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.category-card {
  border-radius: 22px;
  padding: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(250, 250, 248, 0.94)),
    linear-gradient(90deg, rgba(245, 158, 11, 0.18), rgba(15, 23, 42, 0.04));
  border: 1px solid rgba(15, 23, 42, 0.07);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.category-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.category-top h4 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.category-top p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.category-subline {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.subline-code {
  font-family: Consolas, Monaco, monospace;
}

.subline-dot {
  opacity: 0.55;
}

.meta-strip {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-strip span {
  padding: 7px 12px;
  border-radius: 999px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
}

.preview-shell {
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-tag {
  margin: 0;
}

.preview-empty {
  color: #94a3b8;
  font-size: 13px;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 8px;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.view-button {
  width: 100%;
  border-radius: 14px;
  height: 40px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.card-link-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.card-link-group :deep(.el-button) {
  margin: 0;
  width: 100%;
  min-width: 0;
  justify-content: center;
  padding-left: 8px;
  padding-right: 8px;
  font-size: 12px;
}

.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  white-space: nowrap;
}

.table-actions :deep(.el-button) {
  margin: 0;
  min-width: 40px;
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
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.94)),
    linear-gradient(90deg, rgba(59, 130, 246, 0.14), rgba(15, 23, 42, 0.04));
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
  background: linear-gradient(135deg, #1d4ed8, #0f766e);
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

.rule-editor label {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.drawer-tip {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff7ed;
  color: #9a3412;
  line-height: 1.7;
}

.dialog-lead {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  color: #475569;
  line-height: 1.7;
}

.dialog-lead p {
  margin: 0;
}

.dialog-form {
  margin-top: 4px;
}

.govern-table {
  border-radius: 18px;
  overflow: hidden;
}

.empty-state {
  padding: 24px 0 8px;
}

.footer-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.section-anchor {
  scroll-margin-top: 24px;
}

@media (max-width: 1100px) {
  .hero-card {
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

  .drawer-footer,
  .dialog-footer-actions {
    justify-content: stretch;
  }

  .card-link-group {
    grid-template-columns: 1fr;
  }
}
</style>

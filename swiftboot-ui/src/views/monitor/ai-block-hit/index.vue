<template>
  <div class="page-shell">
    <div class="hero-panel">
      <div>
        <span class="eyebrow">Monitor Stream</span>
        <h1>屏蔽词命中日志</h1>
        <p>查看哪些分类、哪些词、哪些用户最容易触发前置拦截，帮助你持续优化屏蔽词策略。</p>
      </div>
      <el-button type="primary" @click="loadData" :loading="loading">刷新数据</el-button>
    </div>

    <div class="stats-grid">
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">累计命中</span>
        <strong>{{ stats.totalCount || 0 }}</strong>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">今日命中</span>
        <strong>{{ stats.todayCount || 0 }}</strong>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">近7天命中</span>
        <strong>{{ stats.weekCount || 0 }}</strong>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">高频分类</span>
        <strong>{{ stats.topCategory || '-' }}</strong>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">高频词</span>
        <strong>{{ stats.topWord || '-' }}</strong>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <span class="stat-label">最近命中</span>
        <strong>{{ formatDateTime(stats.latestHitAt) }}</strong>
      </el-card>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-input v-model="query.categoryName" clearable placeholder="分类" style="width: 180px" />
        <el-input v-model="query.wordText" clearable placeholder="命中词" style="width: 180px" />
        <el-input v-model="query.username" clearable placeholder="用户名" style="width: 180px" />
        <el-input v-model="query.questionContent" clearable placeholder="问题内容" style="width: 260px" />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" class="monitor-table">
        <el-table-column label="命中时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" prop="categoryName" min-width="140" />
        <el-table-column label="命中词" prop="wordText" min-width="140" />
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">
            {{ row.nickname || row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="IP" prop="loginIp" min-width="130" />
        <el-table-column label="问题内容" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.questionContent || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="命中日志详情" width="720px">
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">命中时间</span>
          <strong>{{ formatDateTime(currentDetail?.createTime) }}</strong>
        </div>
        <div class="detail-item">
          <span class="detail-label">分类</span>
          <strong>{{ currentDetail?.categoryName || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span class="detail-label">命中词</span>
          <strong>{{ currentDetail?.wordText || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户</span>
          <strong>{{ currentDetail?.nickname || currentDetail?.username || '-' }}</strong>
        </div>
        <div class="detail-item full">
          <span class="detail-label">问题内容</span>
          <div class="question-box" v-html="highlightedQuestion"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getAiBlockHitStats, listAiBlockHitLogs, type AiBlockHitLog, type AiBlockHitStats } from '@/api/monitor/ai-block-hit'

const loading = ref(false)
const total = ref(0)
const rows = ref<AiBlockHitLog[]>([])
const stats = reactive<Partial<AiBlockHitStats>>({})
const detailVisible = ref(false)
const currentDetail = ref<AiBlockHitLog | null>(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryName: '',
  wordText: '',
  username: '',
  questionContent: ''
})

const formatDateTime = (value?: string) => {
  return value || '-'
}

const escapeHtml = (value?: string) => {
  return (value || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const highlightedQuestion = computed(() => {
  const content = currentDetail.value?.questionContent || ''
  const keyword = currentDetail.value?.wordText || ''
  const escapedContent = escapeHtml(content)
  if (!keyword) {
    return escapedContent || '-'
  }
  const escapedKeyword = escapeHtml(keyword).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return escapedContent.replace(new RegExp(escapedKeyword, 'gi'), '<mark>$&</mark>') || '-'
})

const loadStats = async () => {
  const res: any = await getAiBlockHitStats()
  Object.assign(stats, res.data || {})
}

const loadList = async () => {
  const res: any = await listAiBlockHitLogs({ ...query })
  rows.value = res.data?.rows || []
  total.value = res.data?.total || 0
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStats(), loadList()])
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  query.pageNum = 1
  loadData()
}

const handleReset = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.categoryName = ''
  query.wordText = ''
  query.username = ''
  query.questionContent = ''
  loadData()
}

const handleSizeChange = () => {
  query.pageNum = 1
  loadList()
}

const openDetail = (row: AiBlockHitLog) => {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-shell {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: linear-gradient(180deg, #f8fafc 0%, #eff6ff 100%);
}

.hero-panel,
.stat-card,
.table-card {
  border-radius: 24px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding: 28px;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #92400e;
  font-weight: 700;
}

.hero-panel h1 {
  margin: 8px 0 10px;
  font-size: 30px;
  line-height: 1.1;
  color: #0f172a;
}

.hero-panel p {
  margin: 0;
  color: #475569;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 18px;
}

.stat-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.stat-card strong {
  font-size: 24px;
  color: #0f172a;
}

.table-card {
  padding: 20px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.detail-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.detail-item.full {
  grid-column: 1 / -1;
}

.detail-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.detail-item strong {
  color: #0f172a;
}

.question-box {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #1e293b;
}

.question-box :deep(mark) {
  background: linear-gradient(120deg, rgba(245, 158, 11, 0.28), rgba(251, 191, 36, 0.52));
  color: #7c2d12;
  padding: 0 4px;
  border-radius: 6px;
  font-weight: 700;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-shell {
    padding: 16px;
  }

  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>

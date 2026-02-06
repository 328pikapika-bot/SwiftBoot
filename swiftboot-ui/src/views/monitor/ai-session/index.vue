<template>
  <div class="dashboard-container">
    <!-- 顶部数据卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="stat-card glass-panel group">
        <div class="stat-content">
          <div class="stat-header">
            <span class="stat-label">今日提问</span>
            <div class="stat-icon-wrapper bg-blue-500/10 text-blue-500 group-hover:bg-blue-500 group-hover:text-white">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="stat-value-group">
            <span class="stat-value">{{ stats.todayCount }}</span>
            <span class="stat-unit">次</span>
          </div>
          <div class="stat-trend text-blue-500">
            <span class="text-xs">实时更新</span>
            <el-icon class="animate-pulse"><Loading /></el-icon>
          </div>
        </div>
        <div class="stat-bg-decoration bg-blue-500/5"></div>
      </div>

      <div class="stat-card glass-panel group">
        <div class="stat-content">
          <div class="stat-header">
            <span class="stat-label">Token 消耗</span>
            <div class="stat-icon-wrapper bg-purple-500/10 text-purple-500 group-hover:bg-purple-500 group-hover:text-white">
              <el-icon :size="20"><Coin /></el-icon>
            </div>
          </div>
          <div class="stat-value-group">
            <span class="stat-value">{{ stats.todayTokens.toLocaleString() }}</span>
            <span class="stat-unit">Tokens</span>
          </div>
          <div class="stat-trend text-purple-500">
            <span class="text-xs">今日累计</span>
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="stat-bg-decoration bg-purple-500/5"></div>
      </div>

      <div class="stat-card glass-panel group">
        <div class="stat-content">
          <div class="stat-header">
            <span class="stat-label">平均响应</span>
            <div class="stat-icon-wrapper bg-emerald-500/10 text-emerald-500 group-hover:bg-emerald-500 group-hover:text-white">
              <el-icon :size="20"><Timer /></el-icon>
            </div>
          </div>
          <div class="stat-value-group">
            <span class="stat-value">{{ Math.round(stats.avgDuration) }}</span>
            <span class="stat-unit">ms</span>
          </div>
          <div class="stat-trend" :class="stats.avgDuration > 5000 ? 'text-orange-500' : 'text-emerald-500'">
            <span class="text-xs">{{ stats.avgDuration > 5000 ? '稍有延迟' : '状态极佳' }}</span>
            <el-icon><Connection /></el-icon>
          </div>
        </div>
        <div class="stat-bg-decoration bg-emerald-500/5"></div>
      </div>
    </div>

    <!-- 中部图表区 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
      <!-- 趋势图 -->
      <div class="glass-panel col-span-2 p-6">
        <div class="panel-header mb-6 flex justify-between items-center">
          <h3 class="panel-title">
            <span class="title-icon bg-indigo-500"></span>
            Token 消耗趋势
          </h3>
          <div class="text-xs text-gray-400">近 7 天数据</div>
        </div>
        <div ref="trendChartRef" class="h-80 w-full"></div>
      </div>

      <!-- 活跃用户榜 -->
      <div class="glass-panel p-6 flex flex-col">
        <div class="panel-header mb-6 flex justify-between items-center">
          <h3 class="panel-title">
            <span class="title-icon bg-orange-500"></span>
            活跃用户榜
          </h3>
          <div class="text-xs text-gray-400">Top 10</div>
        </div>
        <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar">
          <div v-for="(user, index) in stats.activeUsers" :key="index" 
               class="user-rank-item flex items-center justify-between p-3 mb-2 rounded-xl transition-all hover:bg-gray-50/80">
            <div class="flex items-center gap-4">
              <div class="rank-badge w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm shadow-sm" 
                   :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <div class="flex flex-col">
                <span class="font-medium text-gray-700 text-sm">{{ user.username || '未知用户' }}</span>
                <span class="text-xs text-gray-400">ID: {{ user.userId || '-' }}</span>
              </div>
            </div>
            <div class="flex flex-col items-end">
              <span class="font-bold text-indigo-600">{{ user.count }}</span>
              <span class="text-xs text-gray-400">次提问</span>
            </div>
          </div>
          <div v-if="!stats.activeUsers?.length" class="h-full flex flex-col items-center justify-center text-gray-400">
            <el-icon :size="48" class="mb-2 opacity-20"><User /></el-icon>
            <span class="text-sm">暂无活跃数据</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部实时列表 -->
    <div class="glass-panel p-6 flex-1 flex flex-col min-h-[400px]">
      <div class="panel-header mb-6 flex flex-wrap justify-between items-center gap-4">
        <h3 class="panel-title">
          <span class="title-icon bg-pink-500"></span>
          实时会话监控
        </h3>
        <div class="flex gap-3">
          <div class="search-input-wrapper relative">
            <el-icon class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"><Search /></el-icon>
            <input 
              v-model="queryParams.username" 
              placeholder="搜索用户..." 
              class="pl-9 pr-4 py-2 rounded-lg bg-gray-50 border-none outline-none focus:ring-2 focus:ring-indigo-500/20 text-sm w-48 transition-all"
              @keyup.enter="handleQuery"
            />
          </div>
          <button @click="handleQuery" class="action-btn btn-primary">
            <el-icon><Search /></el-icon>
          </button>
          <button @click="handleReset" class="action-btn btn-secondary">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
      </div>

      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%"
        :header-cell-style="{ background: 'transparent', color: '#64748b', fontWeight: '600' }"
        :row-class-name="'hover-row'"
      >
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 text-white flex items-center justify-center text-xs font-bold shadow-md shadow-indigo-500/20">
                {{ (row.username || 'U').charAt(0).toUpperCase() }}
              </div>
              <span class="font-medium text-gray-700">{{ row.username || '未知用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="提问内容" prop="question" min-width="300">
          <template #default="{ row }">
            <div class="truncate text-gray-600 max-w-md">{{ row.question }}</div>
          </template>
        </el-table-column>
        <el-table-column label="模型" prop="model" width="140">
          <template #default="{ row }">
            <div class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-blue-50 text-blue-600 border border-blue-100">
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500 mr-1.5"></span>
              {{ row.model }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Token" prop="tokens" width="120" align="center">
          <template #default="{ row }">
            <span class="font-mono text-gray-600 font-medium">{{ row.tokens }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" prop="duration" width="120" align="center">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-1.5">
              <span class="font-mono font-medium" :class="getDurationColor(row.duration)">{{ row.duration }}ms</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="createTime" width="180" align="right">
          <template #default="{ row }">
            <span class="text-xs text-gray-400 font-mono">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="Number(total)"
          layout="prev, pager, next"
          @current-change="getList"
          background
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="会话回放" 
      width="800px" 
      :close-on-click-modal="false"
      class="glass-dialog"
      align-center
    >
      <div class="session-detail">
        <div class="grid grid-cols-4 gap-4 mb-6">
          <div class="detail-card">
            <span class="label">时间</span>
            <span class="value">{{ detailData.createTime }}</span>
          </div>
          <div class="detail-card">
            <span class="label">模型</span>
            <span class="value text-blue-600">{{ detailData.model }}</span>
          </div>
          <div class="detail-card">
            <span class="label">消耗</span>
            <span class="value text-purple-600">{{ detailData.tokens }} Tokens</span>
          </div>
          <div class="detail-card">
            <span class="label">耗时</span>
            <span class="value" :class="getDurationColor(detailData.duration)">{{ detailData.duration }} ms</span>
          </div>
        </div>
        
        <div class="chat-container bg-gray-50/50 rounded-2xl border border-gray-100 overflow-hidden flex flex-col h-[500px]">
          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
            <!-- User Message -->
            <div class="flex gap-4 mb-6 flex-row-reverse">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white shadow-lg shadow-indigo-500/20 shrink-0">
                <el-icon><User /></el-icon>
              </div>
              <div class="max-w-[80%] bg-white rounded-2xl rounded-tr-sm p-4 shadow-sm border border-gray-100 text-gray-800 leading-relaxed text-sm">
                {{ detailData.question }}
              </div>
            </div>
            
            <!-- AI Message -->
            <div class="flex gap-4">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center text-white shadow-lg shadow-emerald-500/20 shrink-0">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="max-w-[90%] bg-white rounded-2xl rounded-tl-sm p-5 shadow-sm border border-gray-100">
                <div class="markdown-body text-sm leading-relaxed text-gray-700" v-html="renderMarkdown(detailData.answer || '')"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { listAiSession, getDashboardStats } from '@/api/monitor/ai-session'
import MarkdownIt from 'markdown-it'
import * as echarts from 'echarts'
import { ChatDotRound, Coin, Timer, Search, Refresh, View, User, Cpu, Loading, TrendCharts, Connection } from '@element-plus/icons-vue'

const md = new MarkdownIt()
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref<any>({})
const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const stats = reactive({
  todayCount: 0,
  todayTokens: 0,
  avgDuration: 0,
  tokenTrend: [] as any[],
  activeUsers: [] as any[]
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  question: '',
  model: ''
})

const getRankClass = (index: number) => {
  switch(index) {
    case 0: return 'bg-yellow-100 text-yellow-700 border border-yellow-200'
    case 1: return 'bg-gray-200 text-gray-700 border border-gray-300'
    case 2: return 'bg-orange-100 text-orange-700 border border-orange-200'
    default: return 'bg-gray-50 text-gray-500 border border-gray-100'
  }
}

const getDurationColor = (duration: number) => {
  if (duration > 5000) return 'text-orange-500'
  if (duration > 2000) return 'text-yellow-600'
  return 'text-emerald-600'
}

// ... existing logic methods ...
const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      Object.assign(stats, res.data)
      initChart()
    }
  } catch (err) {
    console.error(err)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listAiSession(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.username = ''
  handleQuery()
}

const handleDetail = (row: any) => {
  detailData.value = row
  detailVisible.value = true
}

const renderMarkdown = (content: string) => {
  return md.render(content)
}

const initChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  
  trendChart = echarts.init(trendChartRef.value)
  const dates = stats.tokenTrend.map(item => item.date)
  const values = stats.tokenTrend.map(item => item.tokens)
  
  const option = {
    grid: { left: '2%', right: '2%', bottom: '5%', top: '10%', containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#e2e8f0',
      textStyle: { color: '#1e293b' },
      formatter: (params: any) => {
        return `<div class="font-bold mb-1">${params[0].name}</div>
                <div class="text-indigo-600">● 消耗: ${params[0].value} Tokens</div>`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    series: [{
      data: values,
      type: 'line',
      smooth: true,
      showSymbol: false,
      symbolSize: 8,
      lineStyle: { width: 3, color: '#6366f1', shadowColor: 'rgba(99, 102, 241, 0.3)', shadowBlur: 10 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0)' }
        ])
      }
    }]
  }
  trendChart.setOption(option)
}

const handleResize = () => trendChart?.resize()

onMounted(() => {
  loadStats()
  getList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.1);
    background: rgba(255, 255, 255, 0.9);
  }
}

.stat-card {
  padding: 24px;
  
  .stat-content {
    position: relative;
    z-index: 2;
  }

  .stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .stat-label {
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
    letter-spacing: 0.5px;
  }

  .stat-icon-wrapper {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
  }

  .stat-value-group {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 32px;
    font-weight: 800;
    color: #1e293b;
    line-height: 1;
    letter-spacing: -1px;
  }

  .stat-unit {
    font-size: 13px;
    color: #94a3b8;
    font-weight: 500;
  }

  .stat-trend {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 500;
  }

  .stat-bg-decoration {
    position: absolute;
    right: -20px;
    bottom: -20px;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    filter: blur(40px);
    z-index: 1;
    opacity: 0;
    transition: opacity 0.4s ease;
  }

  &:hover .stat-bg-decoration {
    opacity: 1;
  }
}

.panel-header {
  .panel-title {
    font-size: 16px;
    font-weight: 700;
    color: #334155;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .title-icon {
    width: 4px;
    height: 16px;
    border-radius: 2px;
    display: block;
  }
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  cursor: pointer;
  border: none;

  &.btn-primary {
    background: #4f46e5;
    color: white;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);

    &:hover { background: #4338ca; transform: scale(1.05); }
  }

  &.btn-secondary {
    background: #f1f5f9;
    color: #64748b;

    &:hover { background: #e2e8f0; color: #334155; }
  }
}

.custom-scrollbar {
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 2px; }
  &::-webkit-scrollbar-track { background: transparent; }
}

.detail-card {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border: 1px solid #f1f5f9;

  .label { font-size: 12px; color: #94a3b8; font-weight: 600; }
  .value { font-size: 15px; font-weight: 700; color: #334155; font-family: monospace; }
}

/* Markdown 样式覆盖 */
:deep(.markdown-body) {
  font-size: 14px;
  background: transparent !important;
  
  p { margin-bottom: 0.8em; }
  pre { background: #f8fafc !important; border-radius: 8px; border: 1px solid #e2e8f0; }
}
</style>

<template>
  <div class="chart-page page-container font-display">
    <section class="hero-panel">
      <div class="hero-copy">
        <div class="hero-badge">
          <span class="hero-badge-dot"></span>
          Chart Menu Design
        </div>
        <div class="hero-heading">
          <h1>图表设计工作台</h1>
          <p>
            把图表图标的选择、预览和菜单落地放在一个页面里，帮助你更快判断
            Element Plus 图表图标该怎么用于系统菜单。
          </p>
        </div>

        <div class="hero-toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图标名、图表类型或适用场景"
            clearable
            class="hero-search"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" class="hero-button" @click="copyText(selectedChart.name, '图标名')">
            <el-icon><CopyDocument /></el-icon>
            复制当前图标名
          </el-button>
          <el-button class="hero-button is-soft" @click="openIconLibrary">
            <el-icon><Link /></el-icon>
            查看完整图标库
          </el-button>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-label">图表图标</span>
            <strong>{{ chartItems.length }}</strong>
            <span class="stat-meta">覆盖常见数据场景</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">分类数量</span>
            <strong>{{ categoryOptions.length - 1 }}</strong>
            <span class="stat-meta">趋势、分析、占比、总览</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">筛选结果</span>
            <strong>{{ filteredCharts.length }}</strong>
            <span class="stat-meta">按名称与场景实时过滤</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">推荐场景</span>
            <strong>{{ selectedChart.scenes.length }}</strong>
            <span class="stat-meta">来自当前选中图标</span>
          </div>
        </div>
      </div>

      <div class="hero-preview">
        <div class="preview-card" :style="previewGlowStyle">
          <div class="preview-card__top">
            <span class="preview-chip">{{ selectedChart.categoryLabel }}</span>
            <button class="preview-copy" type="button" @click="copyText(selectedChart.name, '图标名')">
              复制
            </button>
          </div>

          <div class="preview-visual">
            <div class="preview-orb" :style="previewOrbStyle"></div>
            <div class="preview-icon" :style="previewIconStyle">
              <el-icon :size="52">
                <component :is="selectedChart.component" />
              </el-icon>
            </div>
          </div>

          <div class="preview-content">
            <div>
              <h2>{{ selectedChart.label }}</h2>
              <p>{{ selectedChart.description }}</p>
            </div>

            <div class="menu-mock">
              <div class="menu-mock__icon" :style="previewIconStyle">
                <el-icon :size="18">
                  <component :is="selectedChart.component" />
                </el-icon>
              </div>
              <div class="menu-mock__text">
                <span class="menu-mock__title">{{ selectedChart.menuLabel }}</span>
                <span class="menu-mock__sub">{{ selectedChart.name }}</span>
              </div>
            </div>

            <div class="code-preview">
              <span>菜单配置名</span>
              <code>{{ selectedChart.name }}</code>
            </div>

            <div class="scene-tags">
              <button
                v-for="scene in selectedChart.scenes"
                :key="scene"
                type="button"
                class="scene-tag"
                @click="searchKeyword = scene"
              >
                {{ scene }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="filter-panel">
      <div class="filter-header">
        <div>
          <span class="filter-eyebrow">Icon Curation</span>
          <h3>图表图标筛选</h3>
        </div>
        <el-button text class="reset-button" @click="resetFilters">
          <el-icon><RefreshRight /></el-icon>
          重置筛选
        </el-button>
      </div>

      <div class="category-list">
        <button
          v-for="item in categoryOptions"
          :key="item.key"
          type="button"
          class="category-pill"
          :class="{ 'is-active': activeCategory === item.key }"
          @click="activeCategory = item.key"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.count }}</strong>
        </button>
      </div>
    </section>

    <section class="gallery-section">
      <div class="gallery-header">
        <div>
          <span class="filter-eyebrow">Gallery</span>
          <h3>图表图标卡片</h3>
        </div>
        <p>单击卡片切换当前推荐图标，点击卡片内按钮可以直接复制图标名称。</p>
      </div>

      <div v-if="filteredCharts.length" class="chart-grid">
        <article
          v-for="item in filteredCharts"
          :key="item.key"
          class="chart-card"
          :class="{ 'is-selected': selectedChartKey === item.key }"
          @click="selectChart(item.key)"
        >
          <div class="chart-card__top">
            <span class="preview-chip">{{ item.categoryLabel }}</span>
            <button type="button" class="card-copy" @click.stop="copyText(item.name, '图标名')">
              复制名称
            </button>
          </div>

          <div class="chart-stage" :style="{ background: item.surface }">
            <div class="chart-stage__halo" :style="{ background: item.glow }"></div>
            <div class="chart-stage__icon" :style="{ color: item.accent }">
              <el-icon :size="46">
                <component :is="item.component" />
              </el-icon>
            </div>
          </div>

          <div class="chart-card__body">
            <div class="chart-title-row">
              <div>
                <h4>{{ item.label }}</h4>
                <span class="chart-name">{{ item.name }}</span>
              </div>
              <span class="chart-menu-label">{{ item.menuLabel }}</span>
            </div>

            <p class="chart-description">{{ item.description }}</p>

            <div class="chart-scenes">
              <span
                v-for="scene in item.scenes"
                :key="scene"
                class="chart-scene"
              >
                {{ scene }}
              </span>
            </div>

            <div class="chart-footer">
              <div class="chart-snippet">
                <span>示例</span>
                <code>{{ item.snippet }}</code>
              </div>
              <span class="chart-select-tip">
                {{ selectedChartKey === item.key ? '当前选中' : '点击设为当前推荐' }}
              </span>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-else description="没有找到匹配的图表图标，试试更短的关键词。" />
    </section>

    <section class="insight-section">
      <div class="insight-card">
        <div class="insight-card__header">
          <span class="filter-eyebrow">Usage Rules</span>
          <h3>选型建议</h3>
        </div>
        <div class="insight-list">
          <div class="insight-item">
            <strong>优先表达数据类型，而不是业务名词</strong>
            <p>例如“渠道分析”更适合用 <code>PieChart</code>，而不是随意使用通用文档图标。</p>
          </div>
          <div class="insight-item">
            <strong>一个菜单层级保持同类视觉语言</strong>
            <p>统计分析模块建议统一使用图表系图标，避免和用户、配置、文件类图标混杂。</p>
          </div>
          <div class="insight-item">
            <strong>用图标传达重点，不要让图标抢文案</strong>
            <p>后台菜单需要的是识别效率，所以保留强识别轮廓，少用含义模糊的装饰性图标。</p>
          </div>
        </div>
      </div>

      <div class="insight-card accent-card">
        <div class="insight-card__header">
          <span class="filter-eyebrow">Current Selection</span>
          <h3>{{ selectedChart.label }} 的落地场景</h3>
        </div>
        <div class="scenario-panel">
          <div class="scenario-row">
            <span class="scenario-label">适合菜单</span>
            <strong>{{ selectedChart.menuLabel }}</strong>
          </div>
          <div class="scenario-row">
            <span class="scenario-label">图标名称</span>
            <code>{{ selectedChart.name }}</code>
          </div>
          <div class="scenario-row is-column">
            <span class="scenario-label">推荐业务</span>
            <div class="scene-tags">
              <span v-for="scene in selectedChart.scenes" :key="scene" class="chart-scene">
                {{ scene }}
              </span>
            </div>
          </div>
          <div class="scenario-row is-column">
            <span class="scenario-label">配置提醒</span>
            <p>
              在菜单配置中直接填写 <code>{{ selectedChart.name }}</code>，
              图标会以一致的方式出现在导航栏与页面入口中。
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CopyDocument,
  DataAnalysis,
  DataBoard,
  DataLine,
  Histogram,
  Link,
  PieChart,
  RefreshRight,
  Search,
  TrendCharts
} from '@element-plus/icons-vue'

type ChartCategory = 'all' | 'trend' | 'analysis' | 'distribution' | 'overview'

interface ChartItem {
  key: string
  name: string
  label: string
  category: Exclude<ChartCategory, 'all'>
  categoryLabel: string
  component: any
  description: string
  scenes: string[]
  menuLabel: string
  snippet: string
  accent: string
  glow: string
  surface: string
}

const router = useRouter()

const chartItems: ChartItem[] = [
  {
    key: 'pie-chart',
    name: 'PieChart',
    label: '饼图',
    category: 'distribution',
    categoryLabel: '结构占比',
    component: PieChart,
    description: '适合表达份额、构成和来源分布，是最常见的结构型菜单图标。',
    scenes: ['渠道占比', '来源分析', '品类结构'],
    menuLabel: '渠道分析',
    snippet: '<PieChart />',
    accent: '#2563eb',
    glow: 'radial-gradient(circle, rgba(37,99,235,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(239,246,255,1) 0%, rgba(224,231,255,0.92) 100%)'
  },
  {
    key: 'histogram',
    name: 'Histogram',
    label: '柱状图',
    category: 'trend',
    categoryLabel: '对比趋势',
    component: Histogram,
    description: '更适合表达多维度对比和时间切片统计，适合运营、销售和报表入口。',
    scenes: ['月度统计', '销量对比', '绩效排行'],
    menuLabel: '经营报表',
    snippet: '<Histogram />',
    accent: '#059669',
    glow: 'radial-gradient(circle, rgba(5,150,105,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(236,253,245,1) 0%, rgba(209,250,229,0.92) 100%)'
  },
  {
    key: 'data-line',
    name: 'DataLine',
    label: '折线图',
    category: 'trend',
    categoryLabel: '走势追踪',
    component: DataLine,
    description: '适合表现变化趋势、实时波动和连续监控场景，识别感很强。',
    scenes: ['访问趋势', '监控曲线', '实时指标'],
    menuLabel: '趋势分析',
    snippet: '<DataLine />',
    accent: '#d97706',
    glow: 'radial-gradient(circle, rgba(217,119,6,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(255,247,237,1) 0%, rgba(254,243,199,0.96) 100%)'
  },
  {
    key: 'data-board',
    name: 'DataBoard',
    label: '数据面板',
    category: 'overview',
    categoryLabel: '总览看板',
    component: DataBoard,
    description: '适合承载系统首页、驾驶舱和综合看板类入口，整体感和平台感更强。',
    scenes: ['数据驾驶舱', '运营总览', '首页看板'],
    menuLabel: '数据总览',
    snippet: '<DataBoard />',
    accent: '#dc2626',
    glow: 'radial-gradient(circle, rgba(220,38,38,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(254,242,242,1) 0%, rgba(254,226,226,0.92) 100%)'
  },
  {
    key: 'trend-charts',
    name: 'TrendCharts',
    label: '趋势组合',
    category: 'analysis',
    categoryLabel: '复合分析',
    component: TrendCharts,
    description: '适合更偏分析平台、BI 模块或组合型洞察入口，层次感比单一图表更强。',
    scenes: ['商业智能', '多维洞察', '指标分析'],
    menuLabel: 'BI 分析',
    snippet: '<TrendCharts />',
    accent: '#7c3aed',
    glow: 'radial-gradient(circle, rgba(124,58,237,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(245,243,255,1) 0%, rgba(237,233,254,0.96) 100%)'
  },
  {
    key: 'data-analysis',
    name: 'DataAnalysis',
    label: '数据分析',
    category: 'analysis',
    categoryLabel: '策略分析',
    component: DataAnalysis,
    description: '更适合高级分析、策略决策和带判断意味的菜单入口，气质更偏专业分析。',
    scenes: ['策略洞察', '决策分析', '诊断中心'],
    menuLabel: '策略分析',
    snippet: '<DataAnalysis />',
    accent: '#ea580c',
    glow: 'radial-gradient(circle, rgba(234,88,12,0.22), transparent 70%)',
    surface: 'linear-gradient(135deg, rgba(255,247,237,1) 0%, rgba(254,215,170,0.96) 100%)'
  }
]

const searchKeyword = ref('')
const activeCategory = ref<ChartCategory>('all')
const selectedChartKey = ref(chartItems[0].key)

const filteredCharts = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return chartItems.filter(item => {
    const matchesCategory = activeCategory.value === 'all' || item.category === activeCategory.value
    if (!matchesCategory) return false
    if (!keyword) return true

    const haystack = [
      item.name,
      item.label,
      item.categoryLabel,
      item.menuLabel,
      item.description,
      ...item.scenes
    ]
      .join(' ')
      .toLowerCase()

    return haystack.includes(keyword)
  })
})

const categoryOptions = computed(() => {
  const base = [
    { key: 'all' as ChartCategory, label: '全部' },
    { key: 'overview' as ChartCategory, label: '总览看板' },
    { key: 'trend' as ChartCategory, label: '趋势对比' },
    { key: 'distribution' as ChartCategory, label: '结构占比' },
    { key: 'analysis' as ChartCategory, label: '分析决策' }
  ]

  return base.map(item => ({
    ...item,
    count: item.key === 'all' ? chartItems.length : chartItems.filter(chart => chart.category === item.key).length
  }))
})

const selectedChart = computed(() => {
  return chartItems.find(item => item.key === selectedChartKey.value) || chartItems[0]
})

const previewIconStyle = computed(() => ({
  color: selectedChart.value.accent
}))

const previewOrbStyle = computed(() => ({
  background: selectedChart.value.glow
}))

const previewGlowStyle = computed(() => ({
  '--preview-accent': selectedChart.value.accent,
  '--preview-surface': selectedChart.value.surface
}))

watch(
  filteredCharts,
  list => {
    if (!list.length) return
    if (!list.some(item => item.key === selectedChartKey.value)) {
      selectedChartKey.value = list[0].key
    }
  },
  { immediate: true }
)

const selectChart = (key: string) => {
  selectedChartKey.value = key
}

const resetFilters = () => {
  activeCategory.value = 'all'
  searchKeyword.value = ''
}

const copyText = async (text: string, label: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`${label}已复制：${text}`)
  } catch {
    const input = document.createElement('input')
    input.value = text
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success(`${label}已复制：${text}`)
  }
}

const openIconLibrary = () => {
  router.push('/tool/icon')
}
</script>

<style lang="scss" scoped>
.chart-page {
  --page-bg: #f8fafc;
  --card-bg: rgba(255, 255, 255, 0.92);
  --card-border: rgba(148, 163, 184, 0.18);
  --text-main: #0f172a;
  --text-secondary: #475569;
  --text-muted: #94a3b8;
  --shadow-soft: 0 24px 60px rgba(15, 23, 42, 0.08);
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 26%),
    radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.08), transparent 28%),
    var(--page-bg);
  min-height: calc(100vh - 64px);
  color: var(--text-main);
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.85fr);
  gap: 24px;
  margin-bottom: 24px;
}

.hero-copy,
.preview-card,
.filter-panel,
.gallery-section,
.insight-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(18px);
}

.hero-copy {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  padding: 32px;

  &::before {
    content: '';
    position: absolute;
    inset: auto -100px -120px auto;
    width: 280px;
    height: 280px;
    border-radius: 999px;
    background: radial-gradient(circle, rgba(96, 165, 250, 0.22), transparent 70%);
    pointer-events: none;
  }
}

.hero-badge,
.filter-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #2563eb;
}

.hero-badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #14b8a6);
  box-shadow: 0 0 0 6px rgba(37, 99, 235, 0.08);
}

.hero-heading {
  margin: 18px 0 28px;

  h1 {
    margin: 0;
    font-size: 38px;
    line-height: 1.08;
    font-weight: 800;
    letter-spacing: -0.04em;
  }

  p {
    max-width: 720px;
    margin: 14px 0 0;
    font-size: 15px;
    line-height: 1.8;
    color: var(--text-secondary);
  }
}

.hero-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.hero-search {
  flex: 1;
  min-width: 240px;
}

.hero-button {
  height: 42px;
  padding: 0 18px;
  border-radius: 14px;
  font-weight: 600;

  &.is-soft {
    border-color: rgba(148, 163, 184, 0.25);
    background: rgba(255, 255, 255, 0.7);
    color: var(--text-main);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(226, 232, 240, 0.9);

  strong {
    font-size: 30px;
    font-weight: 800;
    letter-spacing: -0.04em;
  }
}

.stat-label,
.stat-meta,
.chart-name,
.chart-select-tip,
.scenario-label,
.preview-chip,
.chart-scene {
  color: var(--text-secondary);
}

.stat-label,
.preview-chip,
.chart-menu-label,
.scenario-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.stat-meta {
  font-size: 12px;
  line-height: 1.5;
}

.hero-preview {
  min-width: 0;
}

.preview-card {
  position: sticky;
  top: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 100%;
  border-radius: 32px;
  padding: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.88)),
    var(--preview-surface);
}

.preview-card__top,
.filter-header,
.gallery-header,
.chart-card__top,
.chart-title-row,
.chart-footer,
.insight-card__header,
.scenario-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.preview-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.preview-copy,
.card-copy,
.scene-tag,
.category-pill,
.reset-button {
  transition: all 0.24s ease;
}

.preview-copy,
.card-copy {
  border: 0;
  background: transparent;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    color: #1d4ed8;
  }
}

.preview-visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.preview-orb {
  position: absolute;
  width: 220px;
  height: 220px;
  filter: blur(8px);
}

.preview-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 124px;
  height: 124px;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 24px 42px rgba(15, 23, 42, 0.1);
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 18px;

  h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 800;
    letter-spacing: -0.04em;
  }

  p {
    margin: 10px 0 0;
    line-height: 1.75;
    color: var(--text-secondary);
  }
}

.menu-mock {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.92);
}

.menu-mock__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.92);
}

.menu-mock__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-mock__title {
  font-weight: 700;
}

.menu-mock__sub,
.gallery-header p,
.scenario-row p {
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.code-preview,
.chart-snippet {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.04);

  span {
    font-size: 12px;
    color: var(--text-muted);
  }
}

code {
  font-family: 'Consolas', 'Monaco', monospace;
  color: #1d4ed8;
}

.scene-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.scene-tag,
.chart-scene {
  border: 1px solid rgba(191, 219, 254, 0.88);
  background: rgba(239, 246, 255, 0.78);
  color: #2563eb;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 12px;
  font-weight: 600;
}

.scene-tag {
  cursor: pointer;

  &:hover {
    transform: translateY(-1px);
    background: rgba(219, 234, 254, 0.98);
  }
}

.filter-panel,
.gallery-section,
.insight-card {
  border-radius: 28px;
  padding: 26px 28px;
}

.filter-panel {
  margin-bottom: 20px;
}

.filter-header h3,
.gallery-header h3,
.insight-card__header h3 {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.reset-button {
  padding: 0;
  color: #2563eb;
  font-weight: 600;

  &:hover {
    color: #1d4ed8;
  }
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.category-pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.72);
  color: var(--text-secondary);
  cursor: pointer;

  strong {
    color: var(--text-main);
  }

  &.is-active,
  &:hover {
    border-color: rgba(59, 130, 246, 0.28);
    background: rgba(239, 246, 255, 0.92);
    color: #1d4ed8;
  }
}

.gallery-section {
  margin-bottom: 20px;
}

.gallery-header {
  margin-bottom: 24px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.chart-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
  padding: 22px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.92);
  cursor: pointer;
  transition: transform 0.26s ease, box-shadow 0.26s ease, border-color 0.26s ease;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(96, 165, 250, 0.35);
    box-shadow: 0 18px 36px rgba(37, 99, 235, 0.08);
  }

  &.is-selected {
    border-color: rgba(37, 99, 235, 0.4);
    box-shadow: 0 20px 42px rgba(37, 99, 235, 0.12);
  }
}

.chart-stage {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 172px;
  border-radius: 24px;
  overflow: hidden;
}

.chart-stage__halo {
  position: absolute;
  inset: auto;
  width: 180px;
  height: 180px;
  filter: blur(8px);
}

.chart-stage__icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 94px;
  height: 94px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 28px rgba(15, 23, 42, 0.1);
}

.chart-card__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chart-title-row {
  align-items: flex-start;

  h4 {
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    letter-spacing: -0.03em;
  }
}

.chart-name {
  display: inline-block;
  margin-top: 6px;
  font-size: 13px;
}

.chart-menu-label {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(248, 250, 252, 1);
  border: 1px solid rgba(226, 232, 240, 1);
  white-space: nowrap;
}

.chart-description {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: var(--text-secondary);
}

.chart-scenes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chart-footer {
  align-items: flex-end;
}

.chart-select-tip {
  font-size: 12px;
}

.insight-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.insight-list,
.scenario-panel {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.insight-item,
.scenario-row {
  padding: 18px 18px 16px;
  border-radius: 22px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.92);

  strong {
    display: block;
    margin-bottom: 8px;
    font-size: 15px;
  }

  p {
    margin: 0;
    line-height: 1.75;
    color: var(--text-secondary);
  }
}

.accent-card {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.88)),
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 36%);
}

.scenario-row.is-column {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

:deep(.hero-search .el-input__wrapper) {
  height: 44px;
  border-radius: 16px;
  box-shadow: none;
  border: 1px solid rgba(226, 232, 240, 1);
  background: rgba(255, 255, 255, 0.9);
}

:deep(.hero-button.el-button--primary) {
  border: 0;
  background: linear-gradient(135deg, #2563eb, #14b8a6);
  box-shadow: 0 16px 28px rgba(37, 99, 235, 0.18);
}

@media (max-width: 1280px) {
  .hero-panel,
  .insight-section,
  .chart-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .hero-panel,
  .insight-section,
  .chart-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .hero-copy,
  .preview-card,
  .filter-panel,
  .gallery-section,
  .insight-card {
    padding: 22px;
    border-radius: 24px;
  }

  .hero-heading h1 {
    font-size: 32px;
  }

  .preview-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .chart-page {
    padding: 14px;
  }

  .stats-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .hero-toolbar {
    flex-direction: column;
  }

  .hero-search,
  .hero-button {
    width: 100%;
  }

  .chart-card,
  .scenario-row,
  .insight-item {
    padding: 16px;
  }

  .chart-title-row,
  .chart-footer,
  .filter-header,
  .gallery-header,
  .insight-card__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

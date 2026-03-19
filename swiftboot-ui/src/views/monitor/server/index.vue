<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900 p-6 transition-colors duration-300">
    <div v-loading="loading" element-loading-background="rgba(0, 0, 0, 0)" class="max-w-7xl mx-auto space-y-6">
      
      <!-- 顶部标题栏 -->
      <div class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 dark:text-white tracking-tight">基础资源监控</h1>
          <p class="text-slate-500 text-sm mt-1">实时监控服务器核心指标与运行状态</p>
        </div>
        <div class="flex items-center space-x-2 bg-white dark:bg-slate-800 px-3 py-1.5 rounded-full shadow-sm border border-slate-200 dark:border-slate-700">
          <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
          <span class="text-xs font-medium text-slate-600 dark:text-slate-300">实时更新中 (1s)</span>
        </div>
      </div>

      <!-- 核心指标 Bento Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- CPU Card -->
        <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm hover:shadow-md transition-all border border-slate-100 dark:border-slate-700">
          <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <el-icon :size="60" class="text-blue-500"><Cpu /></el-icon>
          </div>
          <div class="flex flex-col h-full justify-between relative z-10">
            <div>
              <p class="text-slate-500 dark:text-slate-400 text-sm font-medium uppercase tracking-wider">CPU 使用率</p>
              <h2 class="text-3xl font-bold text-slate-800 dark:text-white mt-2">{{ server.cpu?.used || 0 }}%</h2>
            </div>
            <div class="mt-4 h-16" ref="cpuChartRef"></div>
            <div class="mt-2 text-xs text-slate-400">{{ server.cpu?.cpuNum }} 核心 | Sys: {{ server.cpu?.sys }}%</div>
          </div>
        </div>

        <!-- Memory Card -->
        <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm hover:shadow-md transition-all border border-slate-100 dark:border-slate-700">
          <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <el-icon :size="60" class="text-purple-500"><Files /></el-icon>
          </div>
          <div class="flex flex-col h-full justify-between relative z-10">
            <div>
              <p class="text-slate-500 dark:text-slate-400 text-sm font-medium uppercase tracking-wider">系统内存</p>
              <h2 class="text-3xl font-bold text-slate-800 dark:text-white mt-2">{{ server.mem?.usage || 0 }}%</h2>
            </div>
             <div class="mt-4 h-16" ref="memChartRef"></div>
            <div class="mt-2 text-xs text-slate-400">{{ server.mem?.used }}GB / {{ server.mem?.total }}GB</div>
          </div>
        </div>

        <!-- JVM Card -->
        <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm hover:shadow-md transition-all border border-slate-100 dark:border-slate-700">
          <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <el-icon :size="60" class="text-teal-500"><Platform /></el-icon>
          </div>
          <div class="flex flex-col h-full justify-between relative z-10">
            <div>
              <p class="text-slate-500 dark:text-slate-400 text-sm font-medium uppercase tracking-wider">JVM 负载</p>
              <h2 class="text-3xl font-bold text-slate-800 dark:text-white mt-2">{{ server.jvm?.usage || 0 }}%</h2>
            </div>
             <div class="mt-4 h-16" ref="jvmChartRef"></div>
            <div class="mt-2 text-xs text-slate-400">已用: {{ server.jvm?.used }}MB</div>
          </div>
        </div>

        <!-- Disk Card -->
        <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm hover:shadow-md transition-all border border-slate-100 dark:border-slate-700">
          <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <el-icon :size="60" class="text-orange-500"><Coin /></el-icon>
          </div>
          <div class="flex flex-col h-full justify-between relative z-10">
            <div>
              <p class="text-slate-500 dark:text-slate-400 text-sm font-medium uppercase tracking-wider">磁盘均值</p>
              <h2 class="text-3xl font-bold text-slate-800 dark:text-white mt-2">{{ avgDiskUsage }}%</h2>
            </div>
            <!-- Progress Bar Style for Disk -->
            <div class="mt-6 w-full bg-slate-100 dark:bg-slate-700 rounded-full h-2.5 overflow-hidden">
              <div class="bg-gradient-to-r from-orange-400 to-red-500 h-2.5 rounded-full transition-all duration-500" :style="{ width: avgDiskUsage + '%' }"></div>
            </div>
            <div class="mt-4 text-xs text-slate-400">各盘符详情见下表</div>
          </div>
        </div>
      </div>

      <!-- Main Layout: Trend Chart & Details -->
      
      <!-- Trend Chart (Full Width) -->
      <div class="rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm border border-slate-100 dark:border-slate-700">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">实时流量趋势</h3>
            <div class="flex items-center space-x-4">
               <div class="flex space-x-4 text-sm mr-4">
                  <span class="flex items-center"><span class="w-3 h-3 rounded-full bg-blue-500 mr-2"></span>CPU</span>
                  <span class="flex items-center"><span class="w-3 h-3 rounded-full bg-purple-500 mr-2"></span>内存</span>
                  <span class="flex items-center"><span class="w-3 h-3 rounded-full bg-teal-500 mr-2"></span>JVM</span>
               </div>
               <el-button circle size="small" @click="handleExpand">
                 <el-icon><FullScreen /></el-icon>
               </el-button>
            </div>
          </div>
          <div ref="mainChartRef" class="w-full h-[350px]"></div>
      </div>

      <!-- History Dialog -->
      <el-dialog
        v-model="dialogVisible"
        title="历史资源监控趋势"
        width="80%"
        destroy-on-close
        align-center
        class="glass-dialog"
      >
        <div class="flex justify-end mb-4">
          <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
            <el-radio-button label="1h">1小时</el-radio-button>
            <el-radio-button label="3h">3小时</el-radio-button>
            <el-radio-button label="24h">24小时</el-radio-button>
            <el-radio-button label="7d">7天</el-radio-button>
          </el-radio-group>
        </div>
        <div v-loading="historyLoading" class="h-[500px] w-full" ref="historyChartRef"></div>
      </el-dialog>

      <!-- Bottom Info Grid (Server Info & Disk Status) -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Server Info -->
        <div class="rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm border border-slate-100 dark:border-slate-700 h-full">
             <h3 class="text-lg font-bold text-slate-800 dark:text-white mb-4">服务器信息</h3>
             <div class="space-y-4">
                <div class="flex justify-between border-b border-slate-100 dark:border-slate-700 pb-2">
                  <span class="text-slate-500 text-sm">主机名称</span>
                  <span class="text-slate-700 dark:text-slate-300 font-medium text-sm truncate max-w-[150px]">{{ server.sys?.computerName }}</span>
                </div>
                 <div class="flex justify-between border-b border-slate-100 dark:border-slate-700 pb-2">
                  <span class="text-slate-500 text-sm">操作系统</span>
                  <span class="text-slate-700 dark:text-slate-300 font-medium text-sm">{{ server.sys?.osName }}</span>
                </div>
                 <div class="flex justify-between border-b border-slate-100 dark:border-slate-700 pb-2">
                  <span class="text-slate-500 text-sm">系统架构</span>
                  <span class="text-slate-700 dark:text-slate-300 font-medium text-sm">{{ server.sys?.osArch }}</span>
                </div>
                 <div class="flex justify-between border-b border-slate-100 dark:border-slate-700 pb-2">
                  <span class="text-slate-500 text-sm">Java版本</span>
                  <span class="text-slate-700 dark:text-slate-300 font-medium text-sm">{{ server.jvm?.version }}</span>
                </div>
                 <div class="flex justify-between pt-1">
                  <span class="text-slate-500 text-sm">运行时长</span>
                  <span class="text-slate-700 dark:text-slate-300 font-medium text-sm">{{ server.jvm?.runTime }}</span>
                </div>
             </div>
          </div>

          <!-- Disk List -->
           <div class="rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden h-full">
             <h3 class="text-lg font-bold text-slate-800 dark:text-white mb-4">磁盘状态</h3>
             <div class="space-y-4 max-h-[200px] overflow-y-auto pr-2 custom-scrollbar">
                <div v-for="(disk, index) in server.sysFiles" :key="index" class="bg-slate-50 dark:bg-slate-700/50 p-3 rounded-lg">
                   <div class="flex justify-between items-center mb-2">
                      <span class="text-xs font-bold text-slate-700 dark:text-slate-300">{{ disk.dirName }}</span>
                      <span class="text-xs text-slate-500">{{ disk.typeName }}</span>
                   </div>
                   <div class="flex justify-between items-center text-xs text-slate-500 mb-1">
                      <span>{{ disk.used }} / {{ disk.total }}</span>
                      <span :class="{'text-red-500': disk.usage > 80, 'text-orange-500': disk.usage > 50 && disk.usage <= 80, 'text-green-500': disk.usage <= 50}">{{ disk.usage }}%</span>
                   </div>
                   <div class="w-full bg-slate-200 dark:bg-slate-600 rounded-full h-1.5 overflow-hidden">
                      <div 
                        class="h-1.5 rounded-full transition-all duration-500" 
                        :class="{'bg-red-500': disk.usage > 80, 'bg-orange-500': disk.usage > 50 && disk.usage <= 80, 'bg-green-500': disk.usage <= 50}"
                        :style="{ width: disk.usage + '%' }">
                      </div>
                   </div>
                </div>
             </div>
           </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { getServer, getServerHistory } from '@/api/monitor/server'
import * as echarts from 'echarts'
import { Cpu, Files, Platform, Coin, FullScreen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const server = ref<any>({})
const mainChartRef = ref<HTMLElement | null>(null)
const historyChartRef = ref<HTMLElement | null>(null)
const cpuChartRef = ref<HTMLElement | null>(null)
const memChartRef = ref<HTMLElement | null>(null)
const jvmChartRef = ref<HTMLElement | null>(null)

let mainChart: echarts.ECharts | null = null
let historyChart: echarts.ECharts | null = null
let cpuChart: echarts.ECharts | null = null
let memChart: echarts.ECharts | null = null
let jvmChart: echarts.ECharts | null = null
let timer: any = null

const dialogVisible = ref(false)
const historyLoading = ref(false)
const timeRange = ref('1h') // 1h, 3h, 24h, 7d

const handleExpand = () => {
  dialogVisible.value = true
  nextTick(() => {
    // 每次打开都重新初始化，因为 dialog 关闭可能导致 DOM 销毁
    if (historyChart) {
      historyChart.dispose()
      historyChart = null
    }
    initHistoryChart()
    fetchHistoryData()
  })
}

const handleTimeRangeChange = (val: string) => {
  timeRange.value = val
  fetchHistoryData()
}

const fetchHistoryData = async () => {
  historyLoading.value = true
  try {
    const end = new Date()
    let start = new Date()
    
    switch (timeRange.value) {
      case '1h':
        start.setHours(end.getHours() - 1)
        break
      case '3h':
        start.setHours(end.getHours() - 3)
        break
      case '24h':
        start.setHours(end.getHours() - 24)
        break
      case '7d':
        start.setDate(end.getDate() - 7)
        break
    }

    const res = await getServerHistory({
      startTime: formatDate(start),
      endTime: formatDate(end)
    })
    
    updateHistoryChart(res.data)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取历史数据失败')
  } finally {
    historyLoading.value = false
  }
}

const formatDate = (date: Date) => {
  const y = date.getFullYear()
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  const h = date.getHours().toString().padStart(2, '0')
  const min = date.getMinutes().toString().padStart(2, '0')
  const s = date.getSeconds().toString().padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

const initHistoryChart = () => {
  if (historyChartRef.value && !historyChart) {
    historyChart = echarts.init(historyChartRef.value)
  }
}

const updateHistoryChart = (data: any[]) => {
  if (!historyChart) return

  const times = data.map(item => item.createTime.replace('T', ' '))
  const cpuData = data.map(item => item.cpuUsage)
  const memData = data.map(item => item.memUsage)
  const jvmData = data.map(item => item.jvmUsage)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['CPU', '内存', 'JVM'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLabel: {
        show: false
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      max: 100
    },
    series: [
      {
        name: 'CPU',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: cpuData,
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.0)' }
          ])
        }
      },
      {
        name: '内存',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: memData,
        itemStyle: { color: '#a855f7' },
        areaStyle: {
           color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(168, 85, 247, 0.2)' },
            { offset: 1, color: 'rgba(168, 85, 247, 0.0)' }
          ])
        }
      },
      {
        name: 'JVM',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: jvmData,
        itemStyle: { color: '#14b8a6' }
      }
    ]
  }
  historyChart.setOption(option)
}

// 简单的平均磁盘使用率计算
const avgDiskUsage = computed(() => {
  if (!server.value.sysFiles || server.value.sysFiles.length === 0) return 0
  let totalUsage = 0
  server.value.sysFiles.forEach((file: any) => {
    totalUsage += file.usage
  })
  return Math.round(totalUsage / server.value.sysFiles.length)
})

// 趋势图数据缓存 (只保留最近30个点)
const trendData = {
  time: [] as string[],
  cpu: [] as number[],
  mem: [] as number[],
  jvm: [] as number[]
}

// 初始化迷你图表 (Sparklines)
const initMiniChart = (dom: HTMLElement, color: string, data: number[]) => {
  const chart = echarts.init(dom)
  const option = {
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, boundaryGap: false },
    yAxis: { type: 'value', show: false, min: 0, max: 100 },
    series: [{
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: data,
      lineStyle: { 
        width: 2, 
        color: color,
        shadowColor: color, 
        shadowBlur: 5
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color },
          { offset: 1, color: 'rgba(255,255,255,0)' } // 透明
        ]),
        opacity: 0.2
      }
    }]
  }
  chart.setOption(option as any)
  return chart
}

const updateMiniCharts = () => {
    // 构造一些假的历史数据用于填充mini图表的初始状态，或者直接使用trendData
    // 这里简单使用trendData的最后10个点，如果没有则填充0
    const len = trendData.cpu.length
    const sliceLen = 10
    const cpuData = len >= sliceLen ? trendData.cpu.slice(len - sliceLen) : Array(sliceLen).fill(0).map((_, i) => trendData.cpu[i] || 0)
    const memData = len >= sliceLen ? trendData.mem.slice(len - sliceLen) : Array(sliceLen).fill(0).map((_, i) => trendData.mem[i] || 0)
    const jvmData = len >= sliceLen ? trendData.jvm.slice(len - sliceLen) : Array(sliceLen).fill(0).map((_, i) => trendData.jvm[i] || 0)

    cpuChart?.setOption({ series: [{ data: cpuData }] })
    memChart?.setOption({ series: [{ data: memData }] })
    jvmChart?.setOption({ series: [{ data: jvmData }] })
}


const initMainChart = () => {
  if (mainChartRef.value) {
    mainChart = echarts.init(mainChartRef.value)
    const option = {
      animationDuration: 1000,
      animationEasing: 'cubicOut',
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderColor: '#e2e8f0',
        textStyle: { color: '#1e293b' },
        axisPointer: {
            lineStyle: {
                color: '#94a3b8',
                type: 'dashed'
            }
        }
      },
      grid: {
        left: '10px',
        right: '10px',
        bottom: '10px',
        top: '10px',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendData.time,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#94a3b8', margin: 15, fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        max: 100,
        splitLine: {
            lineStyle: {
                color: '#f1f5f9',
                type: 'dashed'
            }
        },
        axisLabel: { color: '#94a3b8' }
      },
      series: [
        {
          name: 'CPU',
          type: 'line',
          smooth: 0.4,
          showSymbol: false,
          data: trendData.cpu,
          itemStyle: { color: '#3b82f6' }, // Blue-500
          lineStyle: { 
            width: 3,
            shadowColor: 'rgba(59, 130, 246, 0.5)',
            shadowBlur: 10
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.0)' }
            ])
          }
        },
        {
          name: '内存',
          type: 'line',
          smooth: 0.4,
          showSymbol: false,
          data: trendData.mem,
          itemStyle: { color: '#a855f7' }, // Purple-500
          lineStyle: { 
            width: 3,
            shadowColor: 'rgba(168, 85, 247, 0.5)',
            shadowBlur: 10
          },
          areaStyle: {
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(168, 85, 247, 0.2)' },
              { offset: 1, color: 'rgba(168, 85, 247, 0.0)' }
            ])
          }
        },
        {
          name: 'JVM',
          type: 'line',
          smooth: 0.4,
          showSymbol: false,
          data: trendData.jvm,
          itemStyle: { color: '#14b8a6' }, // Teal-500
          lineStyle: { width: 3, type: 'dashed' },
          areaStyle: { opacity: 0 } // No area for JVM to reduce clutter
        }
      ]
    }
    mainChart.setOption(option as any)
  }
}

const updateMainChart = () => {
  mainChart?.setOption({
    xAxis: { data: trendData.time },
    series: [
      { data: trendData.cpu },
      { data: trendData.mem },
      { data: trendData.jvm }
    ]
  })
}

const getList = async () => {
  try {
    const res = await getServer()
    server.value = res.data
    loading.value = false

    // 更新趋势图数据
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
    
    if (trendData.time.length > 30) {
      trendData.time.shift()
      trendData.cpu.shift()
      trendData.mem.shift()
      trendData.jvm.shift()
    }
    
    trendData.time.push(timeStr)
    trendData.cpu.push(server.value.cpu?.used || 0)
    trendData.mem.push(server.value.mem?.usage || 0)
    trendData.jvm.push(server.value.jvm?.usage || 0)
    
    updateMainChart()
    updateMiniCharts()
  } catch (error) {
    console.error(error)
    // ElMessage.error('获取服务器监控信息失败') // 静默失败，避免刷屏
    loading.value = false
  }
}

const resizeAllCharts = () => {
    mainChart?.resize()
    cpuChart?.resize()
    memChart?.resize()
    jvmChart?.resize()
}

onMounted(() => {
  getList()
  nextTick(() => {
    initMainChart()
    // Init mini charts with empty data first
    if (cpuChartRef.value) cpuChart = initMiniChart(cpuChartRef.value, '#3b82f6', [])
    if (memChartRef.value) memChart = initMiniChart(memChartRef.value, '#a855f7', [])
    if (jvmChartRef.value) jvmChart = initMiniChart(jvmChartRef.value, '#14b8a6', [])
    
    window.addEventListener('resize', resizeAllCharts)
  })
  timer = setInterval(() => {
    getList()
  }, 1000) // 1s 刷新一次
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  mainChart?.dispose()
  cpuChart?.dispose()
  memChart?.dispose()
  jvmChart?.dispose()
  window.removeEventListener('resize', resizeAllCharts)
})
</script>

<style scoped lang="scss">
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 20px;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #475569;
}
</style>

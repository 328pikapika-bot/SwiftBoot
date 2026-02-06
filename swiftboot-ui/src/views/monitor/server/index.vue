<template>
  <div class="app-container p-4">
    <div v-loading="loading" element-loading-text="正在加载服务器监控信息...">
      
      <!-- 顶部核心指标 -->
      <el-row :gutter="20" class="mb-6">
        <el-col :xs="24" :sm="12" :md="6" class="mb-4 md:mb-0">
          <el-card shadow="hover" class="monitor-card glass-effect">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="text-gray-500 font-medium">CPU 使用率</span>
                <el-icon class="text-blue-500"><Cpu /></el-icon>
              </div>
            </template>
            <div class="text-center py-4">
              <el-progress type="dashboard" :percentage="server.cpu?.used || 0" :color="colors" />
              <div class="mt-2 text-sm text-gray-500">{{ server.cpu?.cpuNum }} 核心</div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6" class="mb-4 md:mb-0">
          <el-card shadow="hover" class="monitor-card glass-effect">
             <template #header>
              <div class="flex justify-between items-center">
                <span class="text-gray-500 font-medium">系统内存</span>
                <el-icon class="text-green-500"><Files /></el-icon>
              </div>
            </template>
            <div class="text-center py-4">
              <el-progress type="dashboard" :percentage="server.mem?.usage || 0" :color="colors" />
              <div class="mt-2 text-sm text-gray-500">{{ server.mem?.used }}GB / {{ server.mem?.total }}GB</div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6" class="mb-4 md:mb-0">
          <el-card shadow="hover" class="monitor-card glass-effect">
             <template #header>
              <div class="flex justify-between items-center">
                <span class="text-gray-500 font-medium">JVM 内存</span>
                <el-icon class="text-purple-500"><Platform /></el-icon>
              </div>
            </template>
            <div class="text-center py-4">
              <el-progress type="dashboard" :percentage="server.jvm?.usage || 0" :color="colors" />
              <div class="mt-2 text-sm text-gray-500">{{ server.jvm?.used }}MB / {{ server.jvm?.total }}MB</div>
            </div>
          </el-card>
        </el-col>

         <el-col :xs="24" :sm="12" :md="6" class="mb-4 md:mb-0">
          <el-card shadow="hover" class="monitor-card glass-effect">
             <template #header>
              <div class="flex justify-between items-center">
                <span class="text-gray-500 font-medium">磁盘总览</span>
                <el-icon class="text-orange-500"><Coin /></el-icon>
              </div>
            </template>
            <div class="text-center py-4">
               <!-- 简单的磁盘平均使用率 -->
               <el-progress type="dashboard" :percentage="avgDiskUsage" :color="colors" />
               <div class="mt-2 text-sm text-gray-500">平均使用率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 趋势图 -->
      <el-row :gutter="20" class="mb-6">
        <el-col :span="24">
          <el-card shadow="hover" class="glass-effect">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="font-bold">资源监控趋势</span>
              </div>
            </template>
            <div ref="chartRef" style="height: 350px; width: 100%;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细信息 -->
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12" class="mb-6 lg:mb-0">
          <el-card shadow="hover" class="glass-effect h-full">
            <template #header>
              <span class="font-bold">服务器信息</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="服务器名称">{{ server.sys?.computerName }}</el-descriptions-item>
              <el-descriptions-item label="服务器IP">{{ server.sys?.computerIp }}</el-descriptions-item>
              <el-descriptions-item label="操作系统">{{ server.sys?.osName }}</el-descriptions-item>
              <el-descriptions-item label="系统架构">{{ server.sys?.osArch }}</el-descriptions-item>
              <el-descriptions-item label="项目路径">{{ server.sys?.userDir }}</el-descriptions-item>
            </el-descriptions>
            
            <div class="mt-6 font-bold mb-4">JVM 信息</div>
             <el-descriptions :column="1" border>
              <el-descriptions-item label="Java名称">{{ server.jvm?.name || 'Java HotSpot(TM) 64-Bit Server VM' }}</el-descriptions-item>
              <el-descriptions-item label="Java版本">{{ server.jvm?.version }}</el-descriptions-item>
              <el-descriptions-item label="启动时间">{{ server.jvm?.startTime }}</el-descriptions-item>
              <el-descriptions-item label="运行时长">{{ server.jvm?.runTime }}</el-descriptions-item>
              <el-descriptions-item label="安装路径">{{ server.jvm?.home }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card shadow="hover" class="glass-effect h-full">
            <template #header>
              <span class="font-bold">磁盘状态</span>
            </template>
            <el-table :data="server.sysFiles" style="width: 100%">
              <el-table-column prop="dirName" label="盘符路径" />
              <el-table-column prop="typeName" label="文件系统" width="100" />
              <el-table-column prop="total" label="总大小" width="100" />
              <el-table-column prop="free" label="可用大小" width="100" />
              <el-table-column prop="used" label="已用大小" width="100" />
              <el-table-column label="使用率" width="180">
                <template #default="scope">
                  <el-progress 
                    :percentage="scope.row.usage" 
                    :color="scope.row.usage > 80 ? '#F56C6C' : (scope.row.usage > 50 ? '#E6A23C' : '#67C23A')"
                  />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { getServer } from '@/api/monitor/server'
import * as echarts from 'echarts'
import { Cpu, Files, Platform, Coin } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const server = ref<any>({})
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let timer: any = null

// 颜色配置
const colors = [
  { color: '#67C23A', percentage: 50 },
  { color: '#E6A23C', percentage: 80 },
  { color: '#F56C6C', percentage: 100 },
]

// 简单的平均磁盘使用率计算
const avgDiskUsage = computed(() => {
  if (!server.value.sysFiles || server.value.sysFiles.length === 0) return 0
  let totalUsage = 0
  server.value.sysFiles.forEach((file: any) => {
    totalUsage += file.usage
  })
  return Math.round(totalUsage / server.value.sysFiles.length)
})

// 趋势图数据缓存 (只保留最近20个点)
const trendData = {
  time: [] as string[],
  cpu: [] as number[],
  mem: [] as number[],
  jvm: [] as number[]
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    updateChart()
    window.addEventListener('resize', resizeChart)
  }
}

const resizeChart = () => {
  chartInstance?.resize()
}

const updateChart = () => {
  if (!chartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU使用率', '内存使用率', 'JVM使用率']
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
      data: trendData.time
    },
    yAxis: {
      type: 'value',
      max: 100
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        data: trendData.cpu,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.01)' }
          ])
        }
      },
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: trendData.mem,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
           color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.3)' },
            { offset: 1, color: 'rgba(103,194,58,0.01)' }
          ])
        }
      },
      {
        name: 'JVM使用率',
        type: 'line',
        smooth: true,
        data: trendData.jvm,
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }
  chartInstance.setOption(option)
}

const getList = async () => {
  try {
    const res = await getServer()
    server.value = res.data
    loading.value = false

    // 更新趋势图数据
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
    
    if (trendData.time.length > 20) {
      trendData.time.shift()
      trendData.cpu.shift()
      trendData.mem.shift()
      trendData.jvm.shift()
    }
    
    trendData.time.push(timeStr)
    trendData.cpu.push(server.value.cpu?.used || 0)
    trendData.mem.push(server.value.mem?.usage || 0)
    trendData.jvm.push(server.value.jvm?.usage || 0)
    
    updateChart()
  } catch (error) {
    console.error(error)
    ElMessage.error('获取服务器监控信息失败')
    loading.value = false
  }
}

onMounted(() => {
  getList()
  nextTick(() => {
    initChart()
  })
  timer = setInterval(() => {
    getList()
  }, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (chartInstance) {
    chartInstance.dispose()
    window.removeEventListener('resize', resizeChart)
  }
})
</script>

<style scoped lang="scss">
.monitor-card {
  transition: all 0.3s;
  &:hover {
    transform: translateY(-5px);
  }
}

.glass-effect {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
}

/* 适配暗黑模式 */
html.dark .glass-effect {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
</style>

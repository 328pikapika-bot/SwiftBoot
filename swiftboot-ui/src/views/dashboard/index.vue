<template>
  <div class="flex-1 overflow-y-auto p-8 font-display">
    <!-- Hero Section -->
    <div class="mb-10 relative overflow-hidden rounded-2xl bg-gradient-to-br from-white to-slate-50 dark:from-slate-900 dark:to-slate-800 border border-slate-200/60 dark:border-slate-700/60 p-8 lg:p-12 shadow-xl shadow-slate-200/40 dark:shadow-slate-900/40 transition-all hover:shadow-2xl hover:shadow-blue-500/10 group">
      <!-- Subtle Background Mesh -->
      <div class="absolute top-0 right-0 w-[600px] h-[600px] bg-gradient-to-br from-blue-400/10 to-emerald-400/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3 pointer-events-none"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-10">
        <!-- Text Content -->
        <div class="flex-1 space-y-6">
            <!-- Badges -->
             <div class="flex flex-wrap items-center gap-3">
               <span class="px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-900/30 border border-blue-100 dark:border-blue-800 text-blue-600 dark:text-blue-300 text-xs font-bold tracking-wider uppercase flex items-center gap-1">
                 <span class="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
                 v1.0.0 Release
               </span>
               <span class="px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-900/30 border border-emerald-100 dark:border-emerald-800 text-emerald-600 dark:text-emerald-300 text-xs font-bold tracking-wider uppercase flex items-center gap-1">
                 <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                 AI Native
               </span>
             </div>
             
             <!-- Heading -->
             <h1 class="text-4xl md:text-5xl lg:text-6xl font-black tracking-tight text-slate-900 dark:text-white leading-tight drop-shadow-sm">
               Swift<span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-emerald-500">Boot</span>
               <br/>
               <span class="text-2xl md:text-3xl font-light text-slate-400 dark:text-slate-500 block mt-2">下一代 AI 原生全栈框架</span>
             </h1>
             
             <!-- Desc -->
             <p class="text-slate-600 dark:text-slate-300 text-lg max-w-2xl leading-relaxed">
               重塑企业级开发体验。深度融合 <strong class="text-slate-900 dark:text-white">Spring Boot 3</strong> 与 <strong class="text-slate-900 dark:text-white">Vue 3</strong>,
               内置 <strong class="text-emerald-600 dark:text-emerald-400">本地 RAG 知识引擎</strong>，让每一行代码都充满智慧。
             </p>
             
             <!-- Feature Tags -->
             <div class="flex flex-wrap gap-4 pt-2">
               <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/50 backdrop-blur-sm shadow-sm">
                 <span class="material-icons-round text-sm text-emerald-500">check_circle</span>
                 <span class="text-sm font-medium text-slate-600 dark:text-slate-300">工程模板</span>
               </div>
               <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/50 backdrop-blur-sm shadow-sm">
                 <span class="material-icons-round text-sm text-blue-500">psychology</span>
                 <span class="text-sm font-medium text-slate-600 dark:text-slate-300">私有知识库</span>
               </div>
               <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/50 backdrop-blur-sm shadow-sm">
                 <span class="material-icons-round text-sm text-purple-500">speed</span>
                 <span class="text-sm font-medium text-slate-600 dark:text-slate-300">极致性能</span>
               </div>
             </div>
        </div>

        <!-- Visual Element -->
         <div class="hidden lg:block relative mr-8">
            <div class="relative w-32 h-32">
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500 to-emerald-500 rounded-3xl rotate-6 opacity-20 blur-sm"></div>
              <div class="absolute inset-0 bg-white dark:bg-slate-800 rounded-3xl border border-slate-100 dark:border-slate-700 shadow-xl flex items-center justify-center transform -rotate-3 transition-transform hover:rotate-0 duration-500">
                <span class="text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-br from-blue-600 to-emerald-500">SB</span>
              </div>
              <!-- Orbiting Elements -->
              <div class="absolute -top-4 -right-4 p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-lg border border-slate-100 dark:border-slate-700 animate-bounce" style="animation-duration: 3s;">
                 <span class="material-icons-round text-emerald-500 text-xl">smart_toy</span>
              </div>
              <div class="absolute -bottom-4 -left-4 p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-lg border border-slate-100 dark:border-slate-700 animate-bounce" style="animation-duration: 4s; animation-delay: 1s;">
                 <span class="material-icons-round text-blue-500 text-xl">code</span>
              </div>
            </div>
         </div>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all hover:shadow-md">
        <div class="flex justify-between items-start mb-4">
          <div class="p-2 bg-blue-50 dark:bg-blue-900/30 rounded-lg">
            <span class="material-icons-round text-blue-500">memory</span>
          </div>
        </div>
        <div class="text-sm text-slate-500 dark:text-slate-400">内存使用</div>
        <div class="text-2xl font-bold mt-1 text-slate-900 dark:text-white">
          {{ serverStats.mem?.total ? ((serverStats.mem.used / serverStats.mem.total) * 100).toFixed(0) + '%' : 'Loading...' }}
          <span class="text-xs text-slate-400 font-normal ml-1" v-if="serverStats.mem?.total">
            {{ Number(serverStats.mem.used).toFixed(1) }}G / {{ Number(serverStats.mem.total).toFixed(1) }}G
          </span>
        </div>
      </div>

      <div class="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all hover:shadow-md">
        <div class="flex justify-between items-start mb-4">
          <div class="p-2 bg-orange-50 dark:bg-orange-900/30 rounded-lg">
            <span class="material-icons-round text-orange-500">speed</span>
          </div>
          <span class="text-xs font-medium text-slate-400">实时</span>
        </div>
        <div class="text-sm text-slate-500 dark:text-slate-400">JVM负载</div>
        <div class="text-2xl font-bold mt-1 text-slate-900 dark:text-white">
          {{ serverStats.jvm?.total ? ((serverStats.jvm.total - serverStats.jvm.free) / serverStats.jvm.total * 100).toFixed(1) + '%' : 'Loading...' }}
        </div>
      </div>

      <div class="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all hover:shadow-md">
        <div class="flex justify-between items-start mb-4">
          <div class="p-2 bg-purple-50 dark:bg-purple-900/30 rounded-lg">
            <span class="material-icons-round text-purple-500">auto_awesome</span>
          </div>
          <span class="text-xs font-medium text-purple-500">RAG Ready</span>
        </div>
        <div class="text-sm text-slate-500 dark:text-slate-400">智能索引</div>
        <div class="text-2xl font-bold mt-1 text-slate-900 dark:text-white">{{ aiStats.knowledge_count ? aiStats.knowledge_count.toLocaleString() : '...' }} 件</div>
      </div>

      <div class="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all hover:shadow-md">
        <div class="flex justify-between items-start mb-4">
          <div class="p-2 bg-emerald-50 dark:bg-emerald-900/30 rounded-lg">
            <span class="material-icons-round text-emerald-500">public</span>
          </div>
          <span class="text-xs font-medium text-emerald-500">正常</span>
        </div>
        <div class="text-sm text-slate-500 dark:text-slate-400">系统运行时间</div>
        <div class="text-2xl font-bold mt-1 text-slate-900 dark:text-white">{{ uptime || 'Loading...' }}</div>
      </div>
    </div>

    <!-- Quick Access -->
    <div class="mb-8">
      <h2 class="text-sm font-semibold text-slate-500 dark:text-slate-400 mb-4 uppercase tracking-wider">基础模块快捷入口</h2>
      <div class="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-4">
        <router-link 
          v-for="item in quickLinks" 
          :key="item.path" 
          :to="item.path"
          class="group bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-4 rounded-xl flex flex-col items-center justify-center gap-3 hover:border-blue-500/50 hover:shadow-md transition-all"
        >
          <div class="w-10 h-10 rounded-full bg-slate-50 dark:bg-slate-800 flex items-center justify-center group-hover:bg-blue-500/10 transition-colors">
            <span class="material-symbols-outlined text-slate-600 dark:text-slate-300 group-hover:text-blue-500 transition-colors">{{ item.icon }}</span>
          </div>
          <span class="text-sm font-medium text-slate-700 dark:text-slate-300 group-hover:text-blue-500 transition-colors">{{ item.title }}</span>
        </router-link>
      </div>
    </div>

    <!-- Bottom Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
      <!-- RAG Insight Card -->
      <div class="lg:col-span-1 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden flex flex-col">
        <div class="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
          <div class="flex items-center gap-3">
            <span class="material-icons-round text-blue-500">psychology</span>
            <h2 class="text-lg font-bold text-slate-900 dark:text-white">RAG 智能系统洞察</h2>
          </div>
          <button class="px-5 py-2.5 bg-gradient-to-r from-emerald-500 to-green-500 hover:from-emerald-600 hover:to-green-600 text-white text-sm font-bold rounded-xl transition-all duration-300 flex items-center gap-2 shadow-lg shadow-emerald-500/30 hover:shadow-emerald-500/50 hover:-translate-y-0.5 border-none outline-none">
            <span class="material-icons-round text-lg">bolt</span>
            开启自诊断
          </button>
        </div>
        <div class="flex-1 p-6 flex flex-col justify-center items-center text-center space-y-4">
          <div class="relative">
            <div class="absolute -inset-4 bg-blue-500/20 blur-2xl rounded-full"></div>
            <span class="material-icons-round text-6xl text-blue-500 relative">hub</span>
          </div>
          <div class="max-w-md">
            <h3 class="font-semibold text-lg text-slate-900 dark:text-white">正在监控系统语境...</h3>
            <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm leading-relaxed">
              SwiftBoot 已通过内置 RAG 引擎分析了 20,491 条 system 日志。目前发现 2 个潜在的连接池配置优化点，可提升 15% 的查询性能。
            </p>
          </div>
          <div class="w-full grid grid-cols-3 gap-4 mt-4">
            <div class="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg">
              <div class="text-blue-500 font-bold text-xl leading-tight">98.2%</div>
              <div class="text-[11px] text-slate-500 uppercase font-semibold mt-1">理解准确度</div>
            </div>
            <div class="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg">
              <div class="text-blue-500 font-bold text-xl leading-tight">1.2s</div>
              <div class="text-[11px] text-slate-500 uppercase font-semibold mt-1">平均检索速度</div>
            </div>
            <div class="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg">
              <div class="text-blue-500 font-bold text-xl leading-tight">64MB</div>
              <div class="text-[11px] text-slate-500 uppercase font-semibold mt-1">向量库占用</div>
            </div>
          </div>
        </div>
      </div>

      <!-- System Dynamics -->
      <div class="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
        <div class="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
          <h2 class="text-lg font-bold text-slate-900 dark:text-white">系统动态</h2>
          <button 
            @click="handleRefreshLogs" 
            class="group w-8 h-8 flex items-center justify-center transition-all duration-300 outline-none focus:outline-none border-none bg-transparent hover:rotate-180"
            :title="isLogsLoading ? '加载中...' : '刷新动态'"
          >
            <span 
              class="material-icons-round text-[20px] text-slate-400 group-hover:text-blue-500 transition-all duration-500"
              :class="{ 'animate-spin text-blue-500': isLogsLoading }"
            >
              sync
            </span>
          </button>
        </div>
        <div class="flex-1 p-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Left: Vector Logs -->
          <div>
             <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                <span class="w-1.5 h-1.5 rounded-full bg-purple-500"></span>
                向量索引更新
             </h3>
             <div class="space-y-6 overflow-y-auto max-h-[300px] pr-2 custom-scrollbar">
                 <div v-for="log in vectorLogs" :key="log.id" class="flex gap-3">
                    <div class="mt-1 w-2 h-2 rounded-full shrink-0" :class="getBusinessTypeColor(log)"></div>
                    <div>
                      <div class="text-sm font-medium text-slate-900 dark:text-white truncate max-w-[280px]">
                        <span class="font-bold text-slate-700 dark:text-slate-200">{{ getDisplayOperName(log) }}</span> 
                        {{ getLogAction(log) }} 
                        <span v-html="formatLogTitle(log.title)"></span>
                      </div>
                      <div class="text-xs text-slate-500 mt-1">{{ formatTimeAgo(log.operTime) }}</div>
                    </div>
                  </div>
                  <div v-if="vectorLogs.length === 0" class="text-center text-slate-400 text-sm py-4">
                    暂无更新
                  </div>
             </div>
          </div>

          <!-- Right: User Logs -->
          <div class="border-l border-slate-100 dark:border-slate-800 pl-6 lg:block hidden">
             <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                用户操作动态
             </h3>
             <div class="space-y-6 overflow-y-auto max-h-[300px] pr-2 custom-scrollbar">
                 <div v-for="log in operLogs" :key="log.id" class="flex gap-3">
                    <div class="mt-1 w-2 h-2 rounded-full shrink-0" :class="getBusinessTypeColor(log)"></div>
                    <div>
                      <div class="text-sm font-medium text-slate-900 dark:text-white truncate max-w-[280px]">
                        <span class="font-bold text-slate-700 dark:text-slate-200">{{ getDisplayOperName(log) }}</span> 
                        {{ getLogAction(log) }} 
                        <span v-html="formatLogTitle(log.title)"></span>
                      </div>
                      <div class="text-xs text-slate-500 mt-1">{{ formatTimeAgo(log.operTime) }}</div>
                    </div>
                  </div>
                  <div v-if="operLogs.length === 0" class="text-center text-slate-400 text-sm py-4">
                    暂无动态
                  </div>
             </div>
          </div>
        </div>
        <router-link to="/monitor/operlog" class="p-4 text-center text-sm font-medium text-blue-500 hover:bg-slate-50 dark:hover:bg-slate-800 border-t border-slate-100 dark:border-slate-800 rounded-b-xl transition-colors">
          查看全部日志
        </router-link>
      </div>
    </div>

    <!-- Floating AI Pulse Effect (Fixed position) -->
    <!-- Note: Actual interaction is handled by global AiAssistant component -->
    <div class="fixed bottom-8 right-8 z-0 pointer-events-none">
       <div class="relative flex items-center justify-center">
         <div class="absolute w-20 h-20 rounded-full bg-blue-500/20 ai-pulse"></div>
         <div class="absolute w-16 h-16 rounded-full bg-blue-500/10 ai-pulse" style="animation-delay: 1.5s;"></div>
       </div>
    </div>

    <!-- Background Decoration -->
    <div class="fixed top-0 right-0 -z-50 w-[500px] h-[500px] bg-blue-500/5 rounded-full blur-[100px] pointer-events-none"></div>
    <div class="fixed bottom-0 left-0 -z-50 w-[400px] h-[400px] bg-blue-400/5 rounded-full blur-[80px] pointer-events-none"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'
import { getServer } from '@/api/monitor/server'
import { list as getOperLogs } from '@/api/monitor/operlog'
import request, { ApiResponse } from '@/utils/request'

const serverStats = ref<any>({})
const responseTime = ref(0)
const uptime = ref('')
const aiStats = ref({ knowledge_count: 0, memory_count: 0 })
const operLogs = ref<any[]>([])
const vectorLogs = ref<any[]>([])
const isLogsLoading = ref(false)
let timer: any = null
let statsTimer: any = null

const quickLinks = [
  { title: '用户管理', path: '/system/user', icon: 'person' },
  { title: '角色管理', path: '/system/role', icon: 'manage_accounts' },
  { title: '菜单管理', path: '/system/menu', icon: 'menu' },
  { title: '部门管理', path: '/system/dept', icon: 'corporate_fare' },
  { title: '字典管理', path: '/system/dict', icon: 'book' },
  { title: '代码生成', path: '/tool/gen', icon: 'terminal' },
  { title: '监控大屏', path: '/monitor/server', icon: 'monitoring' }
]

const loadOperLogs = async () => {
  isLogsLoading.value = true
  try {
    const [userRes, vectorRes] = await Promise.all([
      getOperLogs({ pageNum: 1, pageSize: 20, logType: 'user' }) as unknown as ApiResponse,
      getOperLogs({ pageNum: 1, pageSize: 20, logType: 'vector' }) as unknown as ApiResponse
    ])

    if (userRes.code === 200) {
      operLogs.value = userRes.data?.list || (userRes as any).rows || []
    }
    
    if (vectorRes.code === 200) {
      vectorLogs.value = vectorRes.data?.list || (vectorRes as any).rows || []
    }
  } catch (e) {
    console.error('Failed to load oper logs', e)
  } finally {
    // 延迟一点点以展示动画效果，防止闪烁
    setTimeout(() => {
      isLogsLoading.value = false
    }, 500)
  }
}

const handleRefreshLogs = () => {
  loadOperLogs()
}

const formatTimeAgo = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days} 天前`
  if (hours > 0) return `${hours} 小时前`
  if (minutes > 0) return `${minutes} 分钟前`
  return '刚刚'
}

const getBusinessTypeColor = (log: any) => {
  if (log.title && log.title.includes('检测到问题记忆持久化')) {
    return 'bg-orange-500' // AI 记忆同步显示橙色
  }
  
  if (log.title && log.title.includes('RAG 向量索引更新')) {
    return 'bg-purple-500' // RAG 更新显示紫色
  }
  
  if (log.title) {
      if (log.title.includes('后端变更')) return 'bg-blue-500'
      if (log.title.includes('前端变更')) return 'bg-orange-500'
      if (log.title.includes('文件变更')) return 'bg-slate-400'
  }
  
  // 兼容旧数据：如果 operName 是 '前端'/'后端'
  if (log.operName === '后端') return 'bg-blue-500'
  if (log.operName === '前端') return 'bg-orange-500'
  
  // 超级管理员操作显示绿色 (假设 admin 的 operName 为 'admin' 或 '超级管理员')
  // 也可以根据 log.operName === 'admin' 来判断
  if (log.operName === 'admin' || log.operName === '超级管理员') {
    return 'bg-green-500'
  }
  
  return 'bg-slate-400' // 其他默认为灰色
}

const getLogAction = (log: any) => {
  if (log.title && (log.title.includes('RAG 向量索引更新') || log.title.includes('检测到问题记忆持久化'))) {
    return '完成'
  }
  // AI Engine 或 兼容旧的 前端/后端 operName，都不显示动作（因为动作在 title 里或不需要）
  if (log.operName === 'AI Engine' || log.operName === '前端' || log.operName === '后端') {
      return ''
  }
  return getBusinessTypeAction(log.businessType)
}

const formatLogTitle = (title: string) => {
    if (!title) return ''
    let res = title.replace(/完成$/, '')
    
    // Replace keywords with colored spans
    res = res.replace(/(后端)/g, '<span class="text-blue-500 font-bold">$1</span>')
    res = res.replace(/(前端)/g, '<span class="text-orange-500 font-bold">$1</span>')
    res = res.replace(/(文件)/g, '<span class="text-slate-500 font-bold">$1</span>')
    res = res.replace(/【(.*?)】/g, '<span class="text-orange-500 font-bold">【$1】</span>')
    
    return res
}

const getDisplayOperName = (log: any) => {
    if (log.operName === '前端' || log.operName === '后端') {
        return 'AI Engine'
    }
    return log.operName
}


const getBusinessTypeAction = (type: number) => {
  switch (type) {
    case 1: return '新增了'
    case 2: return '修改了'
    case 3: return '删除了'
    default: return '操作了'
  }
}

const loadAiStats = async () => {
  try {
    const res = await request({
      url: '/system/ai/stats',
      method: 'get'
    }) as unknown as ApiResponse
    if (res.code === 200) {
      aiStats.value = res.data
    }
  } catch (e) {
    console.error('Failed to load AI stats', e)
  }
}

const updateUptime = () => {
  if (!serverStats.value.jvm?.startTimeLong) return
  
  const startTime = serverStats.value.jvm.startTimeLong
  const now = Date.now()
  const diff = now - startTime
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) uptime.value = `${days}天 ${hours % 24}小时`
  else if (hours > 0) uptime.value = `${hours}小时 ${minutes % 60}分`
  else uptime.value = `${minutes}分 ${seconds % 60}秒`
}

const isStatsLoading = ref(false)

const loadServerStats = async () => {
  if (isStatsLoading.value) return
  isStatsLoading.value = true
  const start = Date.now()
  try {
    const res = await getServer() as unknown as ApiResponse
    const end = Date.now()
    responseTime.value = end - start
    
    if (res.code === 200) {
      serverStats.value = res.data
      updateUptime()
    }
  } catch (error) {
    console.error('Failed to load server stats:', error)
  } finally {
    isStatsLoading.value = false
  }
}

const pollStats = async () => {
  if (statsTimer) clearTimeout(statsTimer)
  
  await loadServerStats()
  
  // 使用 setTimeout 确保上一次请求完成后再等待 3 秒发起下一次请求
  statsTimer = setTimeout(pollStats, 3000)
}

const startPolling = () => {
  pollStats()
  if (timer) clearInterval(timer)
  timer = setInterval(updateUptime, 1000)
}

const stopPolling = () => {
  if (timer) clearInterval(timer)
  if (statsTimer) clearTimeout(statsTimer)
  timer = null
  statsTimer = null
}

onMounted(() => {
  loadAiStats()
  loadOperLogs()
  // 首次挂载时启动
  startPolling()
})

onActivated(() => {
  // 从缓存恢复时启动
  startPolling()
})

onDeactivated(() => {
  // 进入缓存时停止
  stopPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style>
@keyframes pulse-ring {
  0% { transform: scale(0.95); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.3; }
  100% { transform: scale(0.95); opacity: 0.5; }
}
.ai-pulse {
  animation: pulse-ring 3s infinite ease-in-out;
}

/* Custom Scrollbar for Dashboard Lists */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1; /* slate-300 */
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; /* slate-400 */
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #334155; /* slate-700 */
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #475569; /* slate-600 */
}
</style>
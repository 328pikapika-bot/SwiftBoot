<template>
  <div class="min-h-screen p-6 bg-[#f8fafc] dark:bg-[#0f172a] font-display transition-colors duration-300">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div class="flex items-center gap-6">
        <div>
          <h1 class="text-2xl font-black tracking-tight text-slate-900 dark:text-white flex items-center gap-3">
            <span class="w-2 h-8 bg-blue-500 rounded-full"></span>
            SwiftBoot 智能中枢
            <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-500/10 text-blue-500 border border-blue-500/20 uppercase tracking-wider">Live</span>
          </h1>
          <p class="text-slate-500 dark:text-slate-400 mt-1 ml-5 text-sm">AI 大脑实时监控与认知分析系统</p>
        </div>

        <!-- 算力消耗榜 Widget (仅管理员) -->
        <div v-if="isAdmin && topUsers.length > 0" 
             @click="openUserSelect" 
             class="flex items-center bg-white dark:bg-slate-800 rounded-full border-2 border-orange-100 dark:border-orange-900/30 cursor-pointer hover:border-orange-300 dark:hover:border-orange-600 transition-all group overflow-visible relative py-2 px-6 h-16 shadow-lg shadow-orange-100/50 dark:shadow-orange-900/20"
        >
           <!-- Custom Tooltip -->
           <div class="absolute -bottom-12 left-1/2 -translate-x-1/2 px-4 py-2 bg-slate-900/90 dark:bg-white/90 backdrop-blur-md text-white dark:text-slate-900 text-xs font-bold rounded-xl opacity-0 group-hover:opacity-100 transition-all duration-300 pointer-events-none whitespace-nowrap z-50 shadow-xl translate-y-2 group-hover:translate-y-0 border border-white/10 dark:border-slate-200/50 flex items-center gap-2">
              <span class="material-icons-round text-sm text-orange-500 animate-pulse">touch_app</span>
              <span>点击查看详情</span>
              <!-- Arrow -->
              <div class="absolute -top-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-slate-900/90 dark:bg-white/90 rotate-45 border-l border-t border-white/10 dark:border-slate-200/50"></div>
           </div>

           <!-- Background Animation -->
           <div class="absolute inset-0 bg-gradient-to-r from-transparent via-orange-50/30 dark:via-orange-900/10 to-transparent translate-x-[-100%] animate-shimmer-fast"></div>
           
           <!-- Left: Title -->
           <div class="flex items-center gap-3 pr-6 relative z-10">
              <span class="text-2xl font-black italic tracking-wider text-transparent bg-clip-text bg-gradient-to-tr from-red-600 via-orange-500 to-yellow-400 animate-fire drop-shadow-md">算力排行 TOP 10</span>
              <div class="relative">
                 <span class="material-icons-round text-orange-500 text-3xl animate-flame-surge absolute top-0 left-0 blur-[1px] opacity-70">local_fire_department</span>
                 <span class="material-icons-round text-orange-500 text-3xl animate-bounce-slight relative z-10">local_fire_department</span>
              </div>
           </div>

           <!-- Vertical Divider -->
           <div class="h-8 w-px bg-slate-200 dark:bg-slate-600 mx-4"></div>

           <!-- Right: Top 3 -->
           <div class="flex items-center gap-6 pl-2 relative z-10">
              <!-- No.1 -->
              <div class="flex items-center gap-2">
                 <span class="material-icons-round text-yellow-500 text-2xl drop-shadow-sm animate-pulse">emoji_events</span>
                 <span class="text-base font-bold text-slate-800 dark:text-slate-100 max-w-[80px] truncate">{{ dashboardRankType === 'user' ? (topUsers[0]?.nickname || topUsers[0]?.username) : topUsers[0]?.deptName }}</span>
              </div>
              <!-- No.2 -->
              <div class="flex items-center gap-2 opacity-90">
                 <span class="material-icons-round text-slate-400 text-xl">emoji_events</span>
                 <span class="text-sm font-bold text-slate-600 dark:text-slate-300 max-w-[80px] truncate">{{ dashboardRankType === 'user' ? (topUsers[1]?.nickname || topUsers[1]?.username) : topUsers[1]?.deptName }}</span>
              </div>
              <!-- No.3 -->
              <div class="flex items-center gap-2 opacity-80">
                 <span class="material-icons-round text-amber-700 text-xl">emoji_events</span>
                 <span class="text-sm font-bold text-slate-600 dark:text-slate-300 max-w-[80px] truncate">{{ dashboardRankType === 'user' ? (topUsers[2]?.nickname || topUsers[2]?.username) : topUsers[2]?.deptName }}</span>
              </div>
           </div>
        </div>
      </div>

      <div class="flex gap-3 items-center">
        <!-- Dashboard Filters -->
        <div v-if="isAdmin" class="flex gap-2">
           <div class="relative">
              <select v-model="dashboardTimeRange" @change="fetchData" class="appearance-none bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 h-12 pl-4 pr-10 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all cursor-pointer shadow-sm">
                 <option value="day">今日数据</option>
                 <option value="week">本周数据</option>
                 <option value="month">本月数据</option>
                 <option value="total">历史总计</option>
              </select>
              <span class="material-icons-round text-slate-400 text-sm absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
           </div>

           <div class="relative">
              <select v-model="dashboardRankType" @change="fetchData" class="appearance-none bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 h-12 pl-4 pr-10 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all cursor-pointer shadow-sm">
                 <option value="user">按用户统计</option>
                 <option value="dept_all">按所有部门</option>
                 <option value="dept_level1">按一级部门</option>
              </select>
              <span class="material-icons-round text-slate-400 text-sm absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
           </div>
        </div>

        <button @click="fetchData" class="px-6 py-2 rounded-full bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 hover:text-blue-600 dark:hover:text-blue-400 transition-all text-sm font-medium shadow-sm flex items-center gap-2 h-12">
          <span class="material-icons-round text-base">refresh</span>
          刷新数据
        </button>
      </div>
    </div>

    <!-- Layer 1: Vital Signs (核心生命体征) -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- 脑容量 -->
      <div @click="openKnowledgeDetail" class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300 cursor-pointer">
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <span class="material-icons-round text-6xl text-blue-500">psychology</span>
        </div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 mb-2">
            <span class="p-1.5 rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-500 dark:text-blue-400">
              <span class="material-icons-round text-lg">storage</span>
            </span>
            <span class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">脑容量</span>
          </div>
          <div class="flex items-baseline gap-2 mt-2">
            <span class="text-3xl font-black text-slate-900 dark:text-white">10,600</span>
            <span class="text-xs font-medium text-emerald-500 flex items-center">
              +120
              <span class="material-icons-round text-[10px]">arrow_upward</span>
            </span>
          </div>
          <div class="mt-3 h-1.5 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
            <div class="h-full bg-blue-500 w-[100%] rounded-full relative overflow-hidden">
               <div class="absolute inset-0 bg-white/30 animate-[shimmer_2s_infinite]"></div>
            </div>
          </div>
          <p class="text-[10px] text-slate-400 mt-2">向量切片总数 (Chunks)</p>
        </div>
      </div>

      <!-- 突触活跃度 -->
      <div @click="openActivityDetail" class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300 cursor-pointer">
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <span class="material-icons-round text-6xl text-purple-500">hub</span>
        </div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 mb-2">
            <span class="p-1.5 rounded-lg bg-purple-50 dark:bg-purple-900/30 text-purple-500 dark:text-purple-400">
              <span class="material-icons-round text-lg">timeline</span>
            </span>
            <span class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">突触活跃度</span>
          </div>
          <div class="flex items-baseline gap-2 mt-2">
            <span class="text-3xl font-black text-slate-900 dark:text-white">{{ stats.todayCount }}</span>
            <span class="text-xs font-medium text-purple-500 flex items-center gap-1">
              <span class="w-1.5 h-1.5 rounded-full bg-purple-500 animate-pulse"></span>
              {{ dashboardTimeRange === 'day' ? 'Live' : (dashboardTimeRange === 'week' ? 'Weekly' : (dashboardTimeRange === 'month' ? 'Monthly' : 'Total')) }}
            </span>
          </div>
          <div class="mt-3 h-12 w-full" ref="synapseChartRef"></div>
          <p class="text-[10px] text-slate-400 mt-2">{{ dashboardTimeRange === 'day' ? '今日交互会话数' : (dashboardTimeRange === 'week' ? '本周交互会话数' : (dashboardTimeRange === 'month' ? '本月交互会话数' : '历史总会话数')) }} (近7天趋势)</p>
        </div>
      </div>

      <!-- 思考延迟 -->
      <div @click="openLatencyDetail" class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300 cursor-pointer">
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <span class="material-icons-round text-6xl text-emerald-500">speed</span>
        </div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 mb-2">
            <span class="p-1.5 rounded-lg bg-emerald-50 dark:bg-emerald-900/30 text-emerald-500 dark:text-emerald-400">
              <span class="material-icons-round text-lg">bolt</span>
            </span>
            <span class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">思考延迟</span>
          </div>
          <div class="flex items-baseline gap-2 mt-2">
            <span class="text-3xl font-black text-slate-900 dark:text-white">{{ Math.round(stats.avgDuration) }}</span>
            <span class="text-sm font-medium text-slate-400">ms</span>
          </div>
          
          <div class="mt-4 flex items-center gap-2">
             <div class="flex-1 h-2 bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden flex">
               <div class="h-full bg-emerald-500" style="width: 30%"></div>
               <div class="h-full bg-yellow-400" style="width: 10%"></div>
               <div class="h-full bg-transparent" style="width: 60%"></div>
             </div>
             <span class="text-xs font-bold text-emerald-500">优</span>
          </div>
          <p class="text-[10px] text-slate-400 mt-2">RAG 检索 + 生成首字耗时</p>
        </div>
      </div>

      <!-- 算力消耗 -->
      <div @click="openTokenDetail" class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300 cursor-pointer">
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <span class="material-icons-round text-6xl text-orange-500">local_fire_department</span>
        </div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 mb-2">
            <span class="p-1.5 rounded-lg bg-orange-50 dark:bg-orange-900/30 text-orange-500 dark:text-orange-400">
              <span class="material-icons-round text-lg">token</span>
            </span>
            <span class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">算力消耗</span>
          </div>
          <div class="flex items-baseline gap-2 mt-2">
            <span class="text-3xl font-black text-slate-900 dark:text-white">{{ (stats.todayTokens / 1000).toFixed(1) }}k</span>
            <span class="text-sm font-medium text-slate-400">Tokens</span>
          </div>
          <div class="mt-3 flex gap-1">
             <div class="flex-1 flex flex-col gap-1">
                <div class="h-1.5 w-full bg-orange-500/20 rounded-full overflow-hidden">
                   <div class="h-full bg-orange-500" style="width: 70%"></div>
                </div>
                <span class="text-[9px] text-slate-400">Prompt</span>
             </div>
             <div class="flex-1 flex flex-col gap-1">
                <div class="h-1.5 w-full bg-blue-500/20 rounded-full overflow-hidden">
                   <div class="h-full bg-blue-500" style="width: 30%"></div>
                </div>
                <span class="text-[9px] text-slate-400">Completion</span>
             </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Layer 2: Cognitive Analytics (认知分布与趋势) -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
      <!-- 知识领域雷达图 -->
      <div @click="openRadarDetail" class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm p-6 relative overflow-hidden group hover:border-blue-500/30 transition-colors flex flex-col cursor-pointer">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <span class="material-icons-round text-8xl text-blue-500">radar</span>
        </div>
        <div class="flex items-center justify-between mb-2 relative z-10 shrink-0">
          <h3 class="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span class="p-1.5 rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-500">
              <span class="material-icons-round text-lg">radar</span>
            </span>
            认知能力雷达
          </h3>
        </div>
        <!-- 放大图表容器并居中 -->
        <div class="flex-1 flex items-center justify-center -ml-2">
           <div ref="radarRef" class="w-[110%] h-[320px]"></div>
        </div>
      </div>

      <!-- 提问高频词云 -->
      <div class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm p-0 relative overflow-hidden group hover:border-indigo-500/30 transition-colors flex flex-col">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity pointer-events-none">
          <span class="material-icons-round text-8xl text-indigo-500">cloud</span>
        </div>
        <div class="flex items-center justify-between p-6 pb-0 relative z-10 shrink-0">
          <h3 class="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span class="p-1.5 rounded-lg bg-indigo-50 dark:bg-indigo-900/30 text-indigo-500">
              <span class="material-icons-round text-lg">cloud_queue</span>
            </span>
            热点词云
          </h3>
          <!-- 切换数据源按钮 -->
          <span class="material-icons-round text-sm text-indigo-400/50 cursor-pointer hover:text-indigo-500 transition-colors transform active:rotate-180 duration-300" 
                @click="toggleWordCloudSource"
                :title="useRealWordCloud ? '切换至演示数据' : '切换至真实数据'">
             sync
          </span>
        </div>
        <div ref="wordCloudRef" class="flex-1 min-h-[300px] w-full"></div>
      </div>

      <!-- 命中率漏斗 -->
      <div class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm p-6 relative overflow-hidden group hover:border-purple-500/30 transition-colors flex flex-col">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <span class="material-icons-round text-8xl text-purple-500">filter_alt</span>
        </div>
        <div class="flex items-center justify-between mb-4 relative z-10 shrink-0">
          <h3 class="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span class="p-1.5 rounded-lg bg-purple-50 dark:bg-purple-900/30 text-purple-500">
              <span class="material-icons-round text-lg">filter_alt</span>
            </span>
            RAG 检索漏斗
          </h3>
        </div>
        <div ref="funnelRef" class="flex-1 min-h-[280px]"></div>
      </div>
    </div>

    <!-- Layer 3: Neural Stream (实时神经流) -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[600px]">
      <!-- 索引构建日志 -->
      <div class="rounded-2xl bg-[#1e293b] text-slate-300 border border-slate-700 shadow-inner overflow-hidden flex flex-col">
         <div class="p-4 border-b border-slate-700 bg-slate-900/50 flex justify-between items-center">
            <h3 class="text-sm font-bold text-white flex items-center gap-2">
               <span class="material-icons-round text-emerald-400 text-sm animate-pulse">terminal</span>
               索引构建流
            </h3>
            <span class="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Live</span>
         </div>
         <div ref="logContainerRef" class="flex-1 p-4 overflow-y-auto font-mono text-xs space-y-3 custom-scrollbar scroll-smooth">
            <div v-for="(log, i) in indexLogs" :key="i" class="flex gap-3 opacity-80 hover:opacity-100 transition-opacity">
               <span class="text-slate-500 shrink-0">[{{ log.time }}]</span>
               <span :class="log.color">{{ log.msg }}</span>
            </div>
            <!-- Typing cursor -->
            <div class="flex gap-2 items-center text-slate-500">
               <span>>_</span>
               <span class="w-2 h-4 bg-slate-500 animate-blink"></span>
            </div>
         </div>
      </div>

       <!-- 实时问答流 -->
       <div class="lg:col-span-2 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm flex flex-col overflow-hidden">
          <div class="p-4 border-b border-slate-100 dark:border-slate-700 flex flex-wrap justify-between items-center gap-4">
             <div class="flex flex-wrap items-center gap-4 flex-1">
                <h3 class="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2 shrink-0">
                   <span class="material-icons-round text-indigo-500">question_answer</span>
                   实时问答监控
                </h3>
             </div>
             <div class="flex gap-2 shrink-0">
                <input 
                  v-model="searchKeyword" 
                  @keyup.enter="handleSearch"
                  placeholder="搜索关键字 (提问/模型)..." 
                  class="bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg px-3 py-1 text-xs outline-none focus:border-blue-500 transition-colors w-48" 
                />
             </div>
          </div>
          
          <div class="flex-1 min-h-0 overflow-hidden" v-loading="loading">
             <div class="h-full overflow-y-auto custom-scrollbar">
                <table class="w-full text-sm text-left">
                   <thead class="text-xs text-slate-500 bg-slate-50 dark:bg-slate-900/50 uppercase sticky top-0 backdrop-blur-sm z-10">
                      <tr>
                         <th class="px-6 py-2.5 font-medium w-32">用户</th>
                         <th class="px-6 py-2.5 font-medium">提问摘要</th>
                         <th class="px-6 py-2.5 font-medium w-24">模型</th>
                         <th class="px-6 py-2.5 font-medium text-center w-20">耗时</th>
                         <th class="px-6 py-2.5 font-medium text-center w-20">Tokens</th>
                         <th class="px-6 py-2.5 font-medium text-right w-32">时间</th>
                         <th class="px-6 py-2.5 font-medium text-center w-20">操作</th>
                      </tr>
                   </thead>
                   <tbody class="divide-y divide-slate-100 dark:divide-slate-700/50">
                      <tr v-for="row in tableData" :key="row.id" class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors group">
                         <td class="px-6 py-3">
                            <span class="font-medium text-slate-700 dark:text-slate-200">{{ row.nickname || row.username }}</span>
                         </td>
                         <td class="px-6 py-3">
                            <div class="max-w-xs truncate text-slate-600 dark:text-slate-400 group-hover:text-blue-500 transition-colors" :title="row.question">{{ row.question }}</div>
                         </td>
                         <td class="px-6 py-3">
                            <span class="px-2 py-1 rounded text-[10px] font-medium bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 border border-blue-100 dark:border-blue-800">
                               {{ formatModelName(row.model) }}
                            </span>
                         </td>
                         <td class="px-6 py-3 text-center">
                            <span class="font-mono text-xs font-medium" :class="row.duration > 3000 ? 'text-orange-500' : 'text-emerald-500'">
                               {{ row.duration }}ms
                            </span>
                         </td>
                         <td class="px-6 py-3 text-center">
                            <span class="font-mono text-xs font-medium text-slate-600 dark:text-slate-400">
                               {{ row.tokens }}
                            </span>
                         </td>
                         <td class="px-6 py-3 text-right text-slate-400 text-xs">
                            {{ row.createTime }}
                         </td>
                         <td class="px-6 py-3 text-center">
                            <span 
                               @click="openDetail(row)" 
                               class="text-xs text-blue-500 hover:text-blue-600 font-medium hover:underline cursor-pointer whitespace-nowrap"
                            >
                               详情
                            </span>
                         </td>
                      </tr>
                   </tbody>
                </table>
             </div>
          </div>
          <!-- Pagination -->
          <div class="border-t border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50">
             <Pagination
                v-model:page="sessionPage.pageNum"
                v-model:limit="sessionPage.pageSize"
                :page-sizes="[10, 15, 20, 50]"
                :total="sessionPage.total"
                @pagination="fetchData"
             />
          </div>
       </div>
    </div>

    <!-- 用户选择弹窗 (算力消耗榜) -->
    <el-dialog 
      v-model="userSelectVisible" 
      width="1280px" 
      class="!rounded-xl overflow-hidden" 
      draggable
      align-center
      :show-close="false"
    >
      <div class="px-8 pt-8 pb-6 flex justify-between items-start border-b border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900">
        <div>
          <h2 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-white mb-1">算力消耗排行榜</h2>
          <p class="text-sm text-slate-500 flex items-center gap-2">
            <span class="material-icons-round text-sm">analytics</span>
            SwiftBoot 智能中枢 - 算力消耗详情分析
          </p>
        </div>
        <div class="flex items-center gap-4">
          <!-- Time Range Filter -->
          <div class="relative">
             <select v-model="timeRange" @change="fetchUserList" class="appearance-none bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 py-2 pl-4 pr-10 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all cursor-pointer">
                <option value="day">今日榜单</option>
                <option value="week">本周榜单</option>
                <option value="month">本月榜单</option>
                <option value="total">历史总榜</option>
             </select>
             <span class="material-icons-round text-slate-400 text-sm absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
          </div>

          <!-- Rank Type Filter -->
          <div class="relative">
             <select v-model="rankType" @change="fetchUserList" class="appearance-none bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 py-2 pl-4 pr-10 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all cursor-pointer">
                <option value="user">按用户查看</option>
                <option value="dept_all">按所有部门</option>
                <option value="dept_level1">按一级部门</option>
             </select>
             <span class="material-icons-round text-slate-400 text-sm absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
          </div>

          <div @click="userSelectVisible = false" class="cursor-pointer text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
        </div>
      </div>

      <div class="h-[650px] overflow-y-auto custom-scrollbar bg-white dark:bg-slate-900">
        <table class="w-full text-left border-collapse table-fixed">
          <thead class="sticky top-0 bg-white dark:bg-slate-900 z-10 shadow-sm">
            <tr class="text-[11px] uppercase tracking-[0.1em] text-slate-400 border-b border-slate-100 dark:border-slate-800">
              <th class="px-8 py-4 font-semibold w-24">排名</th>
              <th v-if="rankType === 'user'" class="px-6 py-4 font-semibold w-48">用户</th>
              <th v-if="rankType === 'user'" class="px-6 py-4 font-semibold w-32">姓名</th>
              <th class="px-6 py-4 font-semibold w-40">{{ rankType === 'user' ? '所属部门' : '部门名称' }}</th>
              <th class="px-6 py-4 font-semibold w-48">算力消耗 (Tokens)</th>
              <th class="px-6 py-4 font-semibold w-32">耗费</th>
              <th class="px-6 py-4 font-semibold w-32">消耗状态</th>
              <th v-if="rankType === 'user'" class="px-8 py-4 font-semibold text-right w-32">管理操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
            <tr v-for="(user, index) in userList" :key="index" 
                class="group transition-colors"
                :class="user._isPlaceholder ? 'bg-slate-50/10' : 'hover:bg-slate-50/50 dark:hover:bg-slate-800/30 cursor-pointer ' + (tempSelectedUser?.userId === user.userId ? 'bg-slate-50/30' : '')"
                @click="!user._isPlaceholder && rankType === 'user' && handleUserSelectChange(user)"
            >
              <!-- 排名 -->
              <td class="px-8 py-4 h-16">
                <div v-if="!user._isPlaceholder">
                  <div v-if="index === 0" class="flex items-center gap-2">
                    <span class="material-icons-round text-gold !text-xl">emoji_events</span>
                    <span class="text-xs font-bold text-gold uppercase tracking-tighter">冠军</span>
                  </div>
                  <div v-else-if="index === 1" class="flex items-center gap-2">
                    <span class="material-icons-round text-silver !text-xl">workspace_premium</span>
                    <span class="text-xs font-bold text-silver uppercase tracking-tighter">亚军</span>
                  </div>
                  <div v-else-if="index === 2" class="flex items-center gap-2">
                    <span class="material-icons-round text-bronze !text-xl">military_tech</span>
                    <span class="text-xs font-bold text-bronze uppercase tracking-tighter">季军</span>
                  </div>
                  <span v-else class="font-num text-slate-400 text-sm ml-2">{{ (index + 1).toString().padStart(2, '0') }}</span>
                </div>
                <span v-else class="text-slate-300">-</span>
              </td>

              <!-- 用户 (仅 User 模式) -->
              <td v-if="rankType === 'user'" class="px-6 py-4 font-num text-sm text-slate-500">
                {{ user._isPlaceholder ? '暂无' : `${user.username}@swiftboot.ai` }}
              </td>

              <!-- 姓名 (仅 User 模式) -->
              <td v-if="rankType === 'user'" class="px-6 py-4">
                <span class="font-medium text-slate-900 dark:text-white">
                  {{ user._isPlaceholder ? '暂无' : (user.nickname || user.username) }}
                </span>
              </td>

              <!-- 部门 -->
              <td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                {{ user._isPlaceholder ? '暂无' : (user.deptName || '未分配') }}
              </td>

              <!-- 算力消耗 -->
              <td class="px-6 py-4">
                <div v-if="!user._isPlaceholder" class="space-y-1.5 w-44">
                  <div class="flex justify-between items-end">
                    <span class="font-num font-semibold text-slate-900 dark:text-white">{{ (user.tokenConsumption || 0).toLocaleString() }}</span>
                    <span class="text-[10px] text-slate-400 font-num uppercase">{{ Math.min(Math.round(((user.tokenConsumption || 0) / 50000) * 100), 100) }}%</span>
                  </div>
                  <div class="w-full h-1 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all duration-500"
                         :class="index === 0 ? 'bg-primary' : (index === 1 ? 'bg-primary/60' : (index === 2 ? 'bg-primary/50' : (index < 5 ? 'bg-primary/40' : (index < 8 ? 'bg-primary/30' : 'bg-primary/20'))))"
                         :style="{ width: Math.min(((user.tokenConsumption || 0) / 50000) * 100, 100) + '%' }">
                    </div>
                  </div>
                </div>
                <span v-else class="text-slate-300">暂无数据</span>
              </td>

              <!-- 耗费 -->
              <td class="px-6 py-4">
                <span v-if="!user._isPlaceholder" class="font-num font-medium text-slate-900 dark:text-white">¥{{ ((user.tokenConsumption || 0) * 0.0002).toFixed(2) }}</span>
                <span v-else class="text-slate-300">-</span>
              </td>

              <!-- 消耗状态 -->
              <td class="px-6 py-4">
                <span v-if="!user._isPlaceholder" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium"
                      :class="(user.tokenConsumption || 0) > (timeRange === 'day' ? 5000 : (timeRange === 'week' ? 30000 : 100000)) ? 'bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400' : 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400'">
                  <span class="w-1 h-1 rounded-full" :class="(user.tokenConsumption || 0) > (timeRange === 'day' ? 5000 : (timeRange === 'week' ? 30000 : 100000)) ? 'bg-amber-500 animate-pulse' : 'bg-emerald-500'"></span>
                  {{ (user.tokenConsumption || 0) > (timeRange === 'day' ? 5000 : (timeRange === 'week' ? 30000 : 100000)) ? '接近上限' : '运行正常' }}
                </span>
                <span v-else class="text-slate-300 text-xs">暂无</span>
              </td>

              <!-- 管理操作 -->
              <td v-if="rankType === 'user'" class="px-8 py-4 text-right" @click.stop>
                <div v-if="!user._isPlaceholder" 
                     class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
                     :class="user.status === 0 ? 'bg-blue-600' : 'bg-slate-200'"
                     @click="handleStatusChange(user)">
                  <span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
                        :class="user.status === 0 ? 'translate-x-5' : 'translate-x-0'"></span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="px-8 py-5 bg-slate-50 dark:bg-slate-800/50 flex justify-between items-center border-t border-slate-100 dark:border-slate-800">
        <div class="text-[11px] text-slate-400 flex items-center gap-2 uppercase tracking-wider">
          <span class="material-icons-round text-xs">update</span>
          数据更新于: <span class="font-num">{{ new Date().toLocaleString() }}</span>
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-1.5">
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-white dark:hover:bg-slate-800 text-slate-500 transition-all"
                    :disabled="userPage.pageNum <= 1"
                    @click="userPage.pageNum--; fetchUserList()">
              <span class="material-icons-round text-sm">chevron_left</span>
            </button>
            <div class="flex gap-1 px-1">
              <button class="w-8 h-8 flex items-center justify-center rounded-lg bg-primary text-white text-xs font-num font-bold">{{ userPage.pageNum }}</button>
            </div>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-white dark:hover:bg-slate-800 text-slate-500 transition-all"
                    :disabled="userList.length < userPage.pageSize && !userList.some(u => u._isPlaceholder)"
                    @click="userPage.pageNum++; fetchUserList()">
              <span class="material-icons-round text-sm">chevron_right</span>
            </button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 脑容量详情弹窗 (Knowledge) -->
    <el-dialog
      v-model="knowledgeDetailVisible"
      width="1000px"
      class="!rounded-xl overflow-hidden"
      align-center
      :show-close="false"
      @opened="initKnowledgeChart"
    >
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-white dark:bg-slate-900">
        <h3 class="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <span class="material-icons-round text-blue-500">psychology</span>
          认知能力全景 (Cognitive Capacity)
          <span class="material-icons-round text-sm text-blue-400/50 cursor-pointer hover:text-blue-500 transition-colors transform active:rotate-180 duration-300 ml-2" 
                @click="toggleKnowledgeDataSource"
                :title="useRealKnowledgeData ? '切换至演示数据' : '切换至真实数据'">
             sync
          </span>
        </h3>
        
        <button @click="knowledgeDetailVisible = false" class="opacity-50 hover:opacity-100 transition-opacity text-slate-500 dark:text-slate-400 outline-none focus:outline-none ml-2">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="p-6 bg-slate-50 dark:bg-slate-900/50 flex flex-col gap-6 h-[700px] overflow-hidden">
         <!-- 核心区域：左右双栏 -->
         <div class="grid grid-cols-2 gap-6 flex-1 min-h-0">
             
             <!-- 左栏：左脑 · 硬知识 -->
             <div class="flex flex-col gap-4 h-full">
                 <!-- 顶部卡片 -->
                 <div class="bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl p-5 text-white shadow-lg shadow-blue-500/20 relative overflow-hidden group shrink-0">
                     <div class="absolute right-0 top-0 opacity-10 group-hover:opacity-20 transition-opacity">
                         <span class="material-icons-round text-8xl transform translate-x-4 -translate-y-4">storage</span>
                     </div>
                     <div class="relative z-10">
                         <div class="flex items-center gap-2 mb-3">
                             <span class="bg-white/20 p-1.5 rounded-lg backdrop-blur-sm">
                                 <span class="material-icons-round text-sm">memory</span>
                             </span>
                             <span class="text-xs font-bold uppercase tracking-wider opacity-90">左脑 · 硬知识</span>
                         </div>
                         <div class="flex items-baseline gap-2">
                             <span class="text-4xl font-black">{{ knowledgeStats.totalChunks?.toLocaleString() || 0 }}</span>
                             <span class="text-xs opacity-80 font-medium">切片 (Chunks)</span>
                         </div>
                         <p class="text-[11px] mt-3 opacity-80 leading-relaxed">项目代码与文档的结构化索引</p>
                     </div>
                 </div>

                 <!-- 旭日图 -->
                 <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4 flex-1 flex flex-col min-h-0">
                     <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200 mb-2 flex items-center gap-2 shrink-0">
                         <span class="material-icons-round text-blue-500 text-base">pie_chart</span>
                         知识覆盖分布
                     </h4>
                     <div class="flex-1 relative w-full h-full">
                         <div ref="knowledgeChartRef" class="w-full h-full"></div>
                         <!-- Center Text Overlay -->
                         <div class="absolute inset-0 flex items-center justify-center pointer-events-none opacity-0 transition-opacity duration-300" :class="{'opacity-100': !isSunburstHovered}">
                            <div class="text-center">
                                <span class="text-xs text-slate-400 block mb-1">Total Coverage</span>
                                <span class="text-xl font-black text-slate-700 dark:text-slate-300">100%</span>
                            </div>
                         </div>
                     </div>
                 </div>

                 <!-- 智能切分策略说明 (新增) -->
                 <div class="bg-blue-50 dark:bg-blue-900/10 rounded-xl border border-blue-100 dark:border-blue-800/30 p-4 shrink-0">
                     <h4 class="text-xs font-bold text-blue-800 dark:text-blue-300 mb-3 flex items-center gap-2">
                         <span class="material-icons-round text-sm">content_cut</span>
                         智能切分策略 (Smart Slicing)
                     </h4>
                     <div class="grid grid-cols-2 gap-3">
                         <div class="flex items-start gap-2">
                             <span class="material-icons-round text-blue-400 text-sm mt-0.5">code</span>
                             <div>
                                 <div class="text-xs font-bold text-slate-700 dark:text-slate-300">Java/Backend</div>
                                 <div class="text-[11px] text-slate-500 leading-tight mt-0.5">基于 AST 解析，按 Class/Method 边界切分，保留完整函数签名与注释。</div>
                             </div>
                         </div>
                         <div class="flex items-start gap-2">
                             <span class="material-icons-round text-indigo-400 text-sm mt-0.5">web</span>
                             <div>
                                 <div class="text-xs font-bold text-slate-700 dark:text-slate-300">Vue/Frontend</div>
                                 <div class="text-[11px] text-slate-500 leading-tight mt-0.5">识别 Template/Script/Style 语义块，确保组件逻辑上下文完整。</div>
                             </div>
                         </div>
                     </div>
                 </div>
             </div>

             <!-- 右栏：右脑 · 软记忆 -->
             <div class="flex flex-col gap-4 h-full">
                 <!-- 顶部卡片 -->
                 <div class="bg-gradient-to-br from-purple-500 to-pink-600 rounded-xl p-5 text-white shadow-lg shadow-purple-500/20 relative overflow-hidden group shrink-0">
                     <div class="absolute right-0 top-0 opacity-10 group-hover:opacity-20 transition-opacity">
                         <span class="material-icons-round text-8xl transform translate-x-4 -translate-y-4">history_edu</span>
                     </div>
                     <div class="relative z-10">
                         <div class="flex items-center gap-2 mb-3">
                             <span class="bg-white/20 p-1.5 rounded-lg backdrop-blur-sm">
                                 <span class="material-icons-round text-sm">auto_awesome</span>
                             </span>
                             <span class="text-xs font-bold uppercase tracking-wider opacity-90">右脑 · 软记忆</span>
                         </div>
                         <div class="flex items-baseline gap-2">
                             <span class="text-4xl font-black">{{ knowledgeStats.memoryCount?.toLocaleString() || 0 }}</span>
                             <span class="text-xs opacity-80 font-medium">条目 (Entries)</span>
                         </div>
                         <p class="text-[11px] mt-3 opacity-80 leading-relaxed">对话中沉淀的业务规则与偏好</p>
                     </div>
                 </div>

                 <!-- 记忆清单 -->
                 <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4 flex-1 flex flex-col min-h-0">
                     <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200 mb-2 flex items-center gap-2 shrink-0">
                         <span class="material-icons-round text-purple-500 text-base">format_list_bulleted</span>
                         近期记忆流
                     </h4>
                     <div class="flex-1 overflow-y-auto custom-scrollbar pr-1 space-y-3">
                         <div v-if="!knowledgeStats.rightBrain || knowledgeStats.rightBrain.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400">
                            <span class="material-icons-round text-4xl mb-2 opacity-20">inbox</span>
                            <span class="text-xs">暂无记忆条目</span>
                         </div>
                         <div v-else v-for="(item, idx) in knowledgeStats.rightBrain" :key="idx" class="p-3 rounded-lg bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-800 hover:border-purple-200 dark:hover:border-purple-800/50 transition-colors group">
                             <div class="flex justify-between items-start mb-1.5">
                                 <span class="px-1.5 py-0.5 rounded text-[9px] font-bold uppercase tracking-wider border"
                                       :class="{
                                         'bg-blue-50 text-blue-600 border-blue-100': item.type === 'rule',
                                         'bg-amber-50 text-amber-600 border-amber-100': item.type === 'preference',
                                         'bg-emerald-50 text-emerald-600 border-emerald-100': item.type === 'business',
                                         'bg-red-50 text-red-600 border-red-100': item.type === 'correction'
                                       }">
                                     {{ item.type }}
                                 </span>
                                 <span class="text-[10px] text-slate-400 font-mono">{{ item.time }}</span>
                             </div>
                             <p class="text-xs text-slate-600 dark:text-slate-300 leading-relaxed line-clamp-3 group-hover:line-clamp-none transition-all">
                                 {{ item.content }}
                             </p>
                         </div>
                     </div>
                 </div>

                 <!-- 记忆策略说明 (新增) -->
                 <div class="bg-purple-50 dark:bg-purple-900/10 rounded-xl border border-purple-100 dark:border-purple-800/30 p-4 shrink-0">
                     <h4 class="text-xs font-bold text-purple-800 dark:text-purple-300 mb-3 flex items-center gap-2">
                         <span class="material-icons-round text-sm">psychology_alt</span>
                         记忆留存机制 (Retention Policy)
                     </h4>
                     <div class="grid grid-cols-2 gap-3">
                         <div class="flex items-start gap-2">
                             <span class="material-icons-round text-purple-400 text-sm mt-0.5">filter_center_focus</span>
                             <div>
                                 <div class="text-xs font-bold text-slate-700 dark:text-slate-300">关键提取</div>
                                 <div class="text-[11px] text-slate-500 leading-tight mt-0.5">NLP 自动识别“必须”、“修正”、“偏好”等关键词，高优先级存入。</div>
                             </div>
                         </div>
                         <div class="flex items-start gap-2">
                             <span class="material-icons-round text-pink-400 text-sm mt-0.5">hourglass_top</span>
                             <div>
                                 <div class="text-xs font-bold text-slate-700 dark:text-slate-300">动态淘汰</div>
                                 <div class="text-[11px] text-slate-500 leading-tight mt-0.5">基于时间衰减与引用频率 (LRU)，自动清理过期或低频记忆。</div>
                             </div>
                         </div>
                     </div>
                 </div>
             </div>
         </div>
         
         <!-- 底部：智能建议 -->
         <div class="bg-gradient-to-r from-emerald-50 to-teal-50 dark:from-emerald-900/20 dark:to-teal-900/20 p-4 rounded-xl border border-emerald-100 dark:border-emerald-800/30 flex gap-4 items-center shrink-0">
             <div class="w-10 h-10 rounded-full bg-emerald-100 dark:bg-emerald-900/40 flex items-center justify-center shrink-0 shadow-sm shadow-emerald-200/50 dark:shadow-none">
                <span class="material-icons-round text-emerald-600 dark:text-emerald-400 animate-pulse">medical_services</span>
             </div>
             <div>
                <h4 class="text-sm font-bold text-emerald-800 dark:text-emerald-200 mb-0.5 flex items-center gap-2">
                    AI 诊断建议
                    <span class="text-[10px] px-1.5 py-0.5 rounded-full bg-emerald-200/50 text-emerald-700 border border-emerald-300/50">Smart Check</span>
                </h4>
                <p class="text-xs text-emerald-700 dark:text-emerald-300 leading-relaxed">{{ knowledgeStats.suggestion }}</p>
             </div>
         </div>
      </div>
    </el-dialog>

    <!-- 突触活跃度详情弹窗 -->
    <el-dialog
      v-model="activityDetailVisible"
      width="900px"
      class="!rounded-xl overflow-hidden"
      align-center
      :show-close="false"
      @opened="initActivityChart"
    >
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-white dark:bg-slate-900">
        <h3 class="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <span class="material-icons-round text-purple-500">hub</span>
          突触活跃度分析
          <span class="material-icons-round text-sm text-purple-400/50 cursor-pointer hover:text-purple-500 transition-colors transform active:rotate-180 duration-300 ml-2" 
                @click="toggleActivityDataSource"
                :title="useRealActivityData ? '切换至演示数据' : '切换至真实数据'">
             sync
          </span>
        </h3>
        
        <div class="flex gap-2">
            <div class="relative">
              <select v-model="activityTimeRange" @change="fetchActivityStats" class="appearance-none bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 py-1 pl-3 pr-8 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500 transition-all cursor-pointer">
                 <option value="week">本周 (Daily)</option>
                 <option value="month">本月 (Daily)</option>
                 <option value="quarter">本季度 (Weekly)</option>
                 <option value="year">本年 (Monthly)</option>
                 <option value="all">历史总计 (Monthly)</option>
              </select>
              <span class="material-icons-round text-slate-400 text-xs absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
           </div>
           
           <button @click="activityDetailVisible = false" class="opacity-50 hover:opacity-100 transition-opacity text-slate-500 dark:text-slate-400 outline-none focus:outline-none ml-2">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
               <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
             </svg>
           </button>
        </div>
      </div>
      
      <div class="p-6 bg-slate-50 dark:bg-slate-900/50 flex flex-col gap-6 h-[600px]">
         <!-- Description -->
         <div class="bg-purple-50 dark:bg-purple-900/10 p-3 rounded-lg border border-purple-100 dark:border-purple-800/30 mb-0">
            <p class="text-sm text-purple-800 dark:text-purple-300 leading-relaxed">
               <span class="font-bold">突触活跃度 (Synapse Activity)</span>：反映用户与 AI 系统的交互频率与深度。每一次对话如同神经网络中的一次突触激发，活跃度越高代表系统“大脑”运转越频繁，知识流动越高效。
            </p>
         </div>

         <!-- Chart Area -->
         <div class="flex-1 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4 flex flex-col">
            <div ref="activityChartRef" class="w-full h-full"></div>
         </div>
         
         <!-- Suggestion Area -->
         <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm flex gap-4 items-start">
             <div class="w-10 h-10 rounded-full bg-purple-50 dark:bg-purple-900/20 flex items-center justify-center shrink-0">
                <span class="material-icons-round text-purple-500 animate-pulse">insights</span>
             </div>
             <div>
                <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200 mb-1">智能分析建议</h4>
                <p class="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{{ activitySuggestion }}</p>
             </div>
         </div>
      </div>
    </el-dialog>

    <!-- 算力消耗详情弹窗 -->
    <el-dialog
      v-model="tokenDetailVisible"
      width="900px"
      class="!rounded-xl overflow-hidden"
      align-center
      :show-close="false"
      @opened="initTokenChart"
    >
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-white dark:bg-slate-900">
        <h3 class="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <span class="material-icons-round text-orange-500">local_fire_department</span>
          算力消耗分析
          <span class="material-icons-round text-sm text-orange-400/50 cursor-pointer hover:text-orange-500 transition-colors transform active:rotate-180 duration-300 ml-2" 
                @click="toggleTokenDataSource"
                :title="useRealTokenData ? '切换至演示数据' : '切换至真实数据'">
             sync
          </span>
        </h3>
        
        <div class="flex gap-2">
            <div class="relative">
              <select v-model="tokenTimeRange" @change="fetchTokenStats" class="appearance-none bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 py-1 pl-3 pr-8 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all cursor-pointer">
                 <option value="week">本周 (Daily)</option>
                 <option value="month">本月 (Daily)</option>
                 <option value="quarter">本季度 (Weekly)</option>
                 <option value="year">本年 (Monthly)</option>
                 <option value="all">历史总计 (Monthly)</option>
              </select>
              <span class="material-icons-round text-slate-400 text-xs absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
           </div>
           
           <button @click="tokenDetailVisible = false" class="opacity-50 hover:opacity-100 transition-opacity text-slate-500 dark:text-slate-400 outline-none focus:outline-none ml-2">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
               <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
             </svg>
           </button>
        </div>
      </div>
      
      <div class="p-6 bg-slate-50 dark:bg-slate-900/50 flex flex-col gap-6 h-[600px]">
         <!-- Description -->
         <div class="bg-orange-50 dark:bg-orange-900/10 p-3 rounded-lg border border-orange-100 dark:border-orange-800/30 mb-0">
            <p class="text-sm text-orange-800 dark:text-orange-300 leading-relaxed">
               <span class="font-bold">算力消耗 (Token Consumption)</span>：统计 Prompt（输入）与 Completion（输出）的 Token 总量。Prompt 代表提问与上下文的长度，Completion 代表 AI 生成内容的长度。Token 是计费与性能评估的核心单位。
            </p>
         </div>

         <!-- Chart Area -->
         <div class="flex-1 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4 flex flex-col">
            <div ref="tokenChartRef" class="w-full h-full"></div>
         </div>
         
         <!-- Suggestion Area -->
         <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm flex gap-4 items-start">
             <div class="w-10 h-10 rounded-full bg-orange-50 dark:bg-orange-900/20 flex items-center justify-center shrink-0">
                <span class="material-icons-round text-orange-500 animate-pulse">insights</span>
             </div>
             <div>
                <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200 mb-1">成本优化建议</h4>
                <p class="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{{ tokenSuggestion }}</p>
             </div>
         </div>
      </div>
    </el-dialog>

    <!-- 思考延迟详情弹窗 -->
    <el-dialog
      v-model="latencyDetailVisible"
      width="900px"
      class="!rounded-xl overflow-hidden"
      align-center
      :show-close="false"
      @opened="initLatencyChart"
    >
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex justify-between items-center bg-white dark:bg-slate-900">
        <h3 class="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <span class="material-icons-round text-emerald-500">speed</span>
          思考延迟详情 (Response Latency)
          <span class="material-icons-round text-sm text-emerald-400/50 cursor-pointer hover:text-emerald-500 transition-colors transform active:rotate-180 duration-300 ml-2" 
                @click="toggleLatencyDataSource"
                :title="useRealLatencyData ? '切换至演示数据' : '切换至真实数据'">
             sync
          </span>
        </h3>
        
        <div class="flex gap-2">
            <div class="relative">
              <select v-model="latencyTimeRange" @change="fetchLatencyStats" class="appearance-none bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 py-1 pl-3 pr-8 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all cursor-pointer">
                 <option value="week">本周 (Daily)</option>
                 <option value="month">本月 (Daily)</option>
                 <option value="quarter">本季度 (Weekly)</option>
                 <option value="year">本年 (Monthly)</option>
                 <option value="all">历史总计 (Monthly)</option>
              </select>
              <span class="material-icons-round text-slate-400 text-xs absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
           </div>
           
           <button @click="latencyDetailVisible = false" class="opacity-50 hover:opacity-100 transition-opacity text-slate-500 dark:text-slate-400 outline-none focus:outline-none ml-2">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
               <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
             </svg>
           </button>
        </div>
      </div>
      
      <div class="p-6 bg-slate-50 dark:bg-slate-900/50 flex flex-col gap-6 h-[600px]">
         <!-- Description -->
         <div class="bg-emerald-50 dark:bg-emerald-900/10 p-3 rounded-lg border border-emerald-100 dark:border-emerald-800/30 mb-0">
            <p class="text-sm text-emerald-800 dark:text-emerald-300 leading-relaxed">
               <span class="font-bold">思考延迟 (Thinking Latency)</span>：统计 AI 从接收到用户提问到开始输出第一个字符的时间间隔。它综合反映了 RAG 知识检索耗时、Prompt 构建耗时以及大模型首字生成耗时 (TTFT)。
            </p>
         </div>

         <!-- Chart Area -->
         <div class="flex-1 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4 flex flex-col">
            <div ref="latencyChartRef" class="w-full h-full"></div>
         </div>
         
         <!-- Suggestion Area -->
         <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm flex gap-4 items-start">
             <div class="w-10 h-10 rounded-full bg-emerald-50 dark:bg-emerald-900/20 flex items-center justify-center shrink-0">
                <span class="material-icons-round text-emerald-500 animate-pulse">speed</span>
             </div>
             <div>
                <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200 mb-1">性能优化建议</h4>
                <p class="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{{ latencySuggestion }}</p>
             </div>
         </div>
      </div>
    </el-dialog>

    <!-- 问答详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="问答详情" 
      width="800px" 
      class="glass-dialog"
      align-center
      destroy-on-close
    >
      <div class="p-6 space-y-6 max-h-[70vh] overflow-y-auto custom-scrollbar" v-if="currentDetail">
         <!-- Question -->
         <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center shrink-0">
               <span class="material-icons-round text-slate-500">person</span>
            </div>
            <div class="flex-1 bg-slate-50 dark:bg-slate-800/50 p-4 rounded-2xl rounded-tl-none border border-slate-100 dark:border-slate-700/50">
               <div class="text-xs text-slate-400 mb-1 flex justify-between">
                  <span>{{ currentDetail.nickname || currentDetail.username }}</span>
                  <span>{{ currentDetail.createTime }}</span>
               </div>
               <div class="text-sm text-slate-800 dark:text-slate-200 leading-relaxed whitespace-pre-wrap">{{ currentDetail.question }}</div>
            </div>
         </div>

         <!-- Answer -->
         <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-blue-500/10 flex items-center justify-center shrink-0">
               <span class="material-icons-round text-blue-500">smart_toy</span>
            </div>
            <div class="flex-1 bg-blue-50/50 dark:bg-blue-900/10 p-4 rounded-2xl rounded-tl-none border border-blue-100 dark:border-blue-800/30">
               <div class="text-xs text-blue-400 mb-1 flex justify-between items-center">
                  <span class="font-bold">AI 回答 ({{ currentDetail.model }})</span>
                  <div class="flex gap-2">
                     <span class="flex items-center gap-1"><span class="material-icons-round text-[10px]">schedule</span> {{ currentDetail.duration }}ms</span>
                     <span class="flex items-center gap-1"><span class="material-icons-round text-[10px]">token</span> {{ currentDetail.tokens }}</span>
                  </div>
               </div>
               <div class="text-sm text-slate-800 dark:text-slate-200 leading-relaxed markdown-body" v-html="renderMarkdown(currentDetail.answer)"></div>
            </div>
         </div>
      </div>
    </el-dialog>

    <!-- 认知能力雷达详情弹窗 -->
    <el-dialog
      v-model="radarDetailVisible"
      width="900px"
      class="!rounded-xl overflow-hidden"
      align-center
      :show-close="false"
      @opened="initRadarDetailChart"
    >
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-white dark:bg-slate-900">
        <h3 class="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <span class="material-icons-round text-blue-500">radar</span>
          认知能力指标说明
        </h3>
        <button @click="radarDetailVisible = false" class="opacity-50 hover:opacity-100 transition-opacity text-slate-500 dark:text-slate-400 outline-none focus:outline-none">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="p-6 bg-slate-50 dark:bg-slate-900/50 flex gap-6 h-[600px]">
        <!-- 左侧：雷达图 -->
        <div class="w-5/12 flex flex-col bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm p-4">
           <h4 class="text-sm font-bold text-slate-700 dark:text-slate-200 mb-4 text-center">实时能力评估</h4>
           <div class="flex-1 flex items-center justify-center relative">
              <div ref="radarDetailRef" class="w-full h-full min-h-[300px]"></div>
           </div>
           <div class="text-center text-xs text-slate-400 mt-2">
              {{ radarSubtitle }}
           </div>
        </div>

        <!-- 右侧：详细说明 -->
        <div class="w-7/12 space-y-4 overflow-y-auto custom-scrollbar pr-2">
           <!-- 知识储备 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-blue-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">知识储备 (Knowledge)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 20%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">代表系统当前掌握的知识库规模和丰富程度。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                计算方式：归一化(向量切片总数 / 目标基准数 1000) * 100% (当向量数据库中的知识切片数量达到 1,000 条时，知识储备指标达到满分 100%)
             </div>
           </div>

           <!-- 交互活跃 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-indigo-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">交互活跃 (Activity)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 15%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">反映用户与系统的互动频率和依赖程度。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                 {{ activityBenchmarkText }}
              </div>
           </div>

           <!-- 记忆深度 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-purple-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">记忆深度 (Memory)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 15%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">衡量系统在多轮对话中保持上下文连贯性的能力。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                 计算方式：统计多轮对话的平均轮数，以及历史消息在 RAG 检索中的引用比例。
              </div>
           </div>

           <!-- 响应速度 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">响应速度 (Speed)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 15%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">体现系统的推理效率和网络延迟状况。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                 计算方式：100 - (平均首字延迟 / 3000ms * 100)，延迟越低分数越高。
              </div>
           </div>

           <!-- 技能覆盖 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-amber-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">技能覆盖 (Skills)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 15%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">评估系统在不同领域（如后端、前端、运维、DB）的知识均衡性。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                 计算方式：基于知识库中不同技术栈标签（Tag）的分布熵值计算。
              </div>
           </div>

           <!-- 准确度 -->
           <div class="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
              <div class="flex items-center gap-2 mb-2">
                 <span class="w-2 h-2 rounded-full bg-red-500"></span>
                 <span class="font-bold text-slate-800 dark:text-slate-200">准确度 (Accuracy)</span>
                 <span class="text-xs text-slate-400 ml-auto">权重: 20%</span>
              </div>
              <p class="text-xs text-slate-600 dark:text-slate-400 mb-2">反映 RAG 检索结果与用户问题的语义相关性。</p>
              <div class="text-[10px] text-slate-400 bg-slate-50 dark:bg-slate-900 p-2 rounded border border-slate-100 dark:border-slate-800">
                 计算方式：基于向量检索的平均 Cosine 相似度得分 (Threshold > 0.7)。
              </div>
           </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import 'github-markdown-css/github-markdown.css'
import { getDashboardStats, listAiSession, getUserTokenStats, getActivityStats, getTokenStats, getKnowledgeStats, getLatencyStats } from '@/api/monitor/ai-session'
import { changeStatus } from '@/api/system/user'
import type { User } from '@/api/system/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 扩展 User 类型以包含 dashboard 所需的额外字段
interface DashboardUser extends User {
  userId?: number
  tokenConsumption?: number
  _isPlaceholder?: boolean
}

const userStore = useUserStore()
const isAdmin = computed(() => {
  // 检查是否为超级管理员 (ID=1) 或具有 admin 角色
  const info = userStore.userInfo
  if (!info) return false
  if (info.userId === 1) return true
  
  return info.roles?.some((role: any) => {
    return role === 'admin' || role?.roleKey === 'admin'
  }) || false
})

const activityBenchmarkText = computed(() => {
  const range = dashboardTimeRange.value
  let target = 5000
  let label = '历史总计'
  
  if (range === 'day' || range === 'today') {
    target = 50
    label = '今日'
  } else if (range === 'week') {
    target = 300
    label = '本周'
  } else if (range === 'month') {
    target = 1000
    label = '本月'
  }
  
  return `计算方式：${label}会话总数 / 目标基准数 ${target} * 100% (当${label}会话数达到 ${target} 次时，交互活跃指标达到满分 100%)`
})

const radarSubtitle = computed(() => {
  const range = dashboardTimeRange.value
  let label = '历史'
  if (range === 'day' || range === 'today') {
    label = '今日'
  } else if (range === 'week') {
    label = '本周'
  } else if (range === 'month') {
    label = '本月'
  }
  return `基于${label}的系统运行数据实时计算`
})

// Data
const stats = ref({
  todayCount: 0,
  todayTokens: 0,
  avgDuration: 0,
  activeUsers: [] as any[],
  topUsers: [] as DashboardUser[],
  tokenTrend: [] as any[],
  last7DaysTrend: [] as number[] // 模拟近7天数据
})

const tableData = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const detailVisible = ref(false)
const currentDetail = ref<any>(null)
const radarDetailVisible = ref(false)

const openRadarDetail = () => {
  radarDetailVisible.value = true
}

// Markdown Renderer
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

const renderMarkdown = (text: string) => {
  if (!text) return ''
  return md.render(text)
}

const handleSearch = () => {
  fetchData()
}

const openDetail = (row: any) => {
  currentDetail.value = row
  detailVisible.value = true
}

const formatModelName = (model: string) => {
  if (!model) return '-'
  model = model.toLowerCase()
  if (model.includes('deepseek')) return 'DeepSeek'
  if (model.includes('gemini') && model.includes('pro')) return 'Gemini Pro'
  if (model.includes('gemini') && model.includes('flash')) return 'Gemini Flash'
  if (model.includes('gemini')) return 'Gemini'
  if (model.includes('gpt-4')) return 'GPT-4'
  if (model.includes('gpt-3.5')) return 'GPT-3.5'
  if (model.includes('claude')) return 'Claude'
  return model.split('-')[0] // Fallback: take first part
}

// User Selection Logic
const userSelectVisible = ref(false)
const userList = ref<DashboardUser[]>([])
const userListLoading = ref(false)
const userSearchQuery = ref('')
const timeRange = ref('week')
const rankType = ref('user')
const dashboardTimeRange = ref('week')
const dashboardRankType = ref('user')

const sessionPage = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const userPage = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const tempSelectedUser = ref<DashboardUser | null>(null)
const selectedUserId = ref<number | undefined>(undefined)
const selectedUserName = ref('')

const openUserSelect = () => {
  userSelectVisible.value = true
  fetchUserList()
}

const fetchUserList = async () => {
  userListLoading.value = true
  try {
    const res = await getUserTokenStats({
      pageNum: userPage.pageNum,
      pageSize: userPage.pageSize,
      username: userSearchQuery.value,
      timeRange: timeRange.value,
      rankType: rankType.value
    })
    // @ts-ignore
    if (res.code === 200) {
      // @ts-ignore
      const list = res.data.list || []
      userList.value = list
      
      // Pad with placeholders to ensure 10 rows
      const targetSize = userPage.pageSize
      while (userList.value.length < targetSize) {
        userList.value.push({ _isPlaceholder: true } as DashboardUser)
      }

      // @ts-ignore
      userPage.total = res.data.total
    }
  } finally {
    userListLoading.value = false
  }
}

const topUsers = ref<DashboardUser[]>([])

const handleStatusChange = async (row: any) => {
  try {
    await changeStatus({ id: row.userId, status: row.status })
    ElMessage.success(`${row.username} 已${row.status === 0 ? '启用' : '禁用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0 // Revert on error
    ElMessage.error('修改状态失败')
  }
}

const handleUserSelectChange = (val: DashboardUser | null) => {
  tempSelectedUser.value = val
}

const confirmUserSelect = () => {
  if (tempSelectedUser.value) {
    selectedUserId.value = tempSelectedUser.value.userId
    selectedUserName.value = tempSelectedUser.value.username || ''
    fetchData()
    userSelectVisible.value = false
  }
}

const clearUserSelect = () => {
  selectedUserId.value = undefined
  selectedUserName.value = ''
  tempSelectedUser.value = null
  fetchData()
}

// Log Stream
const indexLogs = ref<{time: string, msg: string, color?: string}[]>([])
let logEventSource: EventSource | null = null
const logContainerRef = ref<HTMLElement | null>(null)

// 自动滚动到底部
const scrollToBottom = () => {
  if (logContainerRef.value) {
    nextTick(() => {
      logContainerRef.value!.scrollTop = logContainerRef.value!.scrollHeight
    })
  }
}

const initLogStream = () => {
  // 连接到后端 SSE 接口，请求 100 条历史记录
  logEventSource = new EventSource('/api/system/ai/index/stream?limit=100')
  
  logEventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      // 使用 push 将新日志添加到末尾
      indexLogs.value.push(data)
      
      // 保持最新的 500 条日志 (避免前端内存溢出)
      if (indexLogs.value.length > 500) {
        indexLogs.value.shift()
      }
      
      // 收到消息后自动滚动到底部
      scrollToBottom()
    } catch (e) {
      console.error('Failed to parse log message:', e)
    }
  }
  
  logEventSource.onerror = (err) => {
    console.error('Log stream error:', err)
    logEventSource?.close()
  }
}

// Refs
const radarRef = ref<HTMLElement>()
const radarDetailRef = ref<HTMLElement>() // New ref for detail chart
const wordCloudRef = ref<HTMLElement>()
const funnelRef = ref<HTMLElement>()

let radarChart: echarts.ECharts | null = null
let radarDetailChart: echarts.ECharts | null = null // New instance for detail chart
let wordCloudChart: echarts.ECharts | null = null
let funnelChart: echarts.ECharts | null = null
let animationId: number | null = null
let radarZr: any = null
let radarDragging = false
let radarDragStartX = 0
let radarDragStartAngle = 90

const onRadarDown = (event: any) => {
  radarDragging = true
  radarDragStartX = event.offsetX
  if (radarZr) {
    radarZr.setCursorStyle('grabbing')
  }
}

const onRadarMove = (event: any) => {
  if (radarDragging && radarChart) {
    const deltaX = event.offsetX - radarDragStartX
    const nextAngle = radarDragStartAngle + deltaX * 0.3
    radarChart.setOption({ radar: { startAngle: nextAngle } })
  }
}

const onRadarUp = () => {
  radarDragging = false
  const option = radarChart?.getOption() as any
  radarDragStartAngle = option?.radar?.[0]?.startAngle ?? radarDragStartAngle
  if (radarZr) {
    radarZr.setCursorStyle('grab')
  }
}

const synapseChartRef = ref<HTMLElement>()
let synapseChart: echarts.ECharts | null = null

// Word Cloud State
const useRealWordCloud = ref(false) // 默认演示模式

const toggleWordCloudSource = () => {
  useRealWordCloud.value = !useRealWordCloud.value
  ElMessage.success(useRealWordCloud.value ? '已切换至真实数据' : '已切换至演示数据')
  initWordCloud()
}

const initWordCloud = () => {
  if (wordCloudRef.value) {
    // Clean up old instance and animation
    if (wordCloudChart) {
      wordCloudChart.dispose()
      wordCloudChart = null
    }
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
    }

    wordCloudChart = echarts.init(wordCloudRef.value)
    
    let rawTags = []
    
    if (useRealWordCloud.value) {
       // 真实数据
       rawTags = (stats.value as any).wordCloud || []
    } else {
       // 模拟 30 个热点词数据 (演示用)
       rawTags = Array.from({ length: 30 }, (_, i) => ({
         name: ['Spring Boot', 'Redis', 'Vue 3', 'DeepSeek', 'RAG', 'Vector DB', 'MySQL', 'MyBatis Plus', 'Element Plus', 'TypeScript', 'Vite', 'Pinia', 'Axios', 'Java 17', 'Python', 'FastAPI', 'Docker', 'Nginx', 'Linux', 'Git', 'Maven', 'Gradle', 'Jenkins', 'Kubernetes', 'Cloud Native', 'Microservices', 'Distributed', 'High Availability', 'Performance', 'Security'][i],
         value: Math.floor(Math.random() * 100) + 10,
         category: Math.floor(Math.random() * 6)
       }))
    }
    
    // 如果没有数据，使用默认占位数据防止空白
    const tags = rawTags.length > 0 ? rawTags.map((item: any) => ({
      name: item.name,
      value: Number(item.value),
      category: Number(item.category)
    })) : [
      { name: '暂无数据', value: 10, category: 0 }
    ]

    // 生成球面上均匀分布的点 (Fibonacci Sphere)
    const points: any[] = []
    const count = tags.length
    const goldenRatio = (1 + Math.sqrt(5)) / 2
    
    for (let i = 0; i < count; i++) {
      const y = 1 - (i / (count - 1)) * 2
      const radius = Math.sqrt(1 - y * y)
      const theta = 2 * Math.PI * i / goldenRatio // Golden angle increment
      
      const x = Math.cos(theta) * radius
      const z = Math.sin(theta) * radius
      
      points.push({
        ...tags[i],
        x3d: x,
        y3d: y,
        z3d: z,
        originX: x,
        originY: y,
        originZ: z
      })
    }

    const colors = ['#3b82f6', '#10b981', '#8b5cf6', '#f59e0b', '#ec4899', '#6366f1']
    let angleY = 0
    let angleX = 0
    const sphereRadius = 120
    
    // 交互状态
    let isDragging = false
    let lastMouseX = 0
    let lastMouseY = 0
    
    const wordZr = wordCloudChart.getZr()
    wordZr.on('mousedown', (e: any) => {
      isDragging = true
      lastMouseX = e.offsetX
      lastMouseY = e.offsetY
    })
    wordZr.on('mousemove', (e: any) => {
      if (isDragging) {
        const dx = e.offsetX - lastMouseX
        const dy = e.offsetY - lastMouseY
        angleY += dx * 0.005
        angleX += dy * 0.005
        lastMouseX = e.offsetX
        lastMouseY = e.offsetY
      }
    })
    wordZr.on('mouseup', () => { isDragging = false })
    wordZr.on('globalout', () => { isDragging = false })

    // 动画循环
    const animate = () => {
      if (!wordCloudChart) return // Stop if chart disposed

      if (!isDragging) {
        angleY += 0.005 // 自动自转
      }
      
      const data = points.map(p => {
        // 1. 绕 X 轴旋转
        let y1 = p.originY * Math.cos(angleX) - p.originZ * Math.sin(angleX)
        let z1 = p.originY * Math.sin(angleX) + p.originZ * Math.cos(angleX)
        
        // 2. 绕 Y 轴旋转
        let x2 = p.originX * Math.cos(angleY) - z1 * Math.sin(angleY)
        let z2 = p.originX * Math.sin(angleY) + z1 * Math.cos(angleY)
        
        // 透视投影
        const scale = (z2 + 2) / 3 // 简单的透视缩放
        const alpha = (z2 + 1) / 2 // 透明度随深度变化
        
        return {
          name: p.name,
          value: [x2 * sphereRadius, y1 * sphereRadius, p.value], // [x, y, rawValue]
          symbolSize: Math.min(Math.max(p.value / 1.5, 30) * scale, 60), // 限制最大尺寸
          itemStyle: {
            color: colors[p.category],
            opacity: Math.max(0.2, Math.min(1, alpha)),
            shadowBlur: 10 * scale,
            shadowColor: colors[p.category]
          },
          label: {
            show: true,
            formatter: '{b}',
            fontSize: Math.max(10, (p.value / 5) * scale),
            color: '#334155', // 深色文字
            opacity: Math.max(0.2, Math.min(1, alpha))
          },
          z: z2 * 100 // 控制层级
        }
      })

      wordCloudChart?.setOption({
        tooltip: {
          show: true,
          formatter: (params: any) => {
             return `${params.name}: ${params.value[2]} 次`
          }
        },
        xAxis: { show: false, min: -150, max: 150 },
        yAxis: { show: false, min: -150, max: 150 },
        grid: { left: 0, right: 0, top: 0, bottom: 0 },
        series: [{
          type: 'graph',
          coordinateSystem: 'cartesian2d',
          layout: 'none',
          data: data,
          roam: false, // 关闭 ECharts 自带的平移缩放，改用自定义 3D 旋转
          draggable: false, 
          animation: false
        }]
      })
      
      animationId = requestAnimationFrame(animate)
    }
    
    animate()
  }
}

// Mock Data for Charts
const initCharts = () => {
  if (synapseChartRef.value && !synapseChart) {
     synapseChart = echarts.init(synapseChartRef.value)
     
     // Generate last 7 data points labels ending with today/current week/current month
     const xAxisData = []
     const range = dashboardTimeRange.value
     
     if (range === 'month') {
        for (let i = 6; i >= 0; i--) {
           if (i === 0) {
             xAxisData.push('本月')
           } else if (i === 1) {
             xAxisData.push('上月')
           } else {
             xAxisData.push(`前${i}月`)
           }
        }
     } else if (range === 'week') {
        for (let i = 6; i >= 0; i--) {
           if (i === 0) {
             xAxisData.push('本周')
           } else if (i === 1) {
             xAxisData.push('上周')
           } else {
             xAxisData.push(`前${i}周`)
           }
        }
     } else {
        // Default to 'day' (last 7 days)
        const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
        for (let i = 6; i >= 0; i--) {
           const d = new Date()
           d.setDate(d.getDate() - i)
           xAxisData.push(weekDays[d.getDay()])
        }
     }

     const data = stats.value.last7DaysTrend.length ? stats.value.last7DaysTrend : [12, 18, 24, 35, 20, 28, stats.value.todayCount || 42]
     
     synapseChart.setOption({
        tooltip: { 
          trigger: 'axis',
          formatter: '{b}: {c}'
        },
        grid: { left: 0, right: 0, top: 5, bottom: 0 },
        xAxis: { type: 'category', show: false, data: xAxisData },
        yAxis: { type: 'value', show: false },
        series: [{
           data: data,
           type: 'line',
           smooth: true,
           showSymbol: false,
           lineStyle: { width: 2, color: '#a855f7' },
           areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                 { offset: 0, color: 'rgba(168, 85, 247, 0.4)' },
                 { offset: 1, color: 'rgba(168, 85, 247, 0)' }
              ])
           }
        }]
     })
  }

  if (radarRef.value && !radarChart) {
    radarChart = echarts.init(radarRef.value)
    const radarOption = getRadarOption() // Extract option generation
    // @ts-ignore
    radarChart.setOption(radarOption)
    radarZr = radarChart.getZr()
    radarZr.on('mousedown', onRadarDown)
    radarZr.on('mousemove', onRadarMove)
    radarZr.on('mouseup', onRadarUp)
    radarZr.on('globalout', onRadarUp)
    // 设置初始鼠标样式
    radarZr.setCursorStyle('grab')
  }

  initWordCloud()

  if (funnelRef.value && !funnelChart) {
    funnelChart = echarts.init(funnelRef.value)
    funnelChart.setOption({
       tooltip: { 
         trigger: 'item',
         formatter: '{b} : {c}%'
       },
       color: ['#6366f1', '#8b5cf6', '#3b82f6', '#0ea5e9', '#10b981'],
       series: [{
          type: 'funnel',
          left: '10%', top: 10, bottom: 10, width: '80%',
          min: 0, max: 100,
          minSize: '0%', maxSize: '100%',
          sort: 'descending',
          gap: 2,
          label: {
            show: true,
            position: 'inside',
            formatter: '{b}\n{c}%',
            color: '#fff',
            fontSize: 12,
            fontWeight: 'bold',
            textShadowBlur: 3,
            textShadowColor: 'rgba(0,0,0,0.5)'
          },
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 1,
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowOffsetY: 5,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          },
          data: [
             { value: 100, name: '提问接收', itemStyle: { color: '#6366f1' } },
             { value: 80, name: '意图识别', itemStyle: { color: '#8b5cf6' } }, 
             { value: 60, name: '向量检索', itemStyle: { color: '#3b82f6' } }, 
             { value: 40, name: '知识重排', itemStyle: { color: '#0ea5e9' } },
             { value: 20, name: '最终引用', itemStyle: { color: '#10b981' } }
          ]
       }]
    })
  }
}

const updateRadarChart = () => {
  if (radarChart) {
      // @ts-ignore
      radarChart.setOption(getRadarOption())
  }
  
  if (radarDetailChart) {
      // @ts-ignore
      radarDetailChart.setOption(getRadarOption(true))
  }
}

const updateSynapseChart = () => {
  if (!synapseChart) return
  
  const range = dashboardTimeRange.value
  const xAxisData: string[] = []
  const data: number[] = []
  
  // Convert tokenTrend to Map for easy lookup
  const trendMap = new Map()
  if (stats.value.tokenTrend && Array.isArray(stats.value.tokenTrend)) {
    stats.value.tokenTrend.forEach((item: any) => {
      trendMap.set(item.date, Number(item.tokens))
    })
  }

  // Generate 7 time points
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    let key = ''
    let label = ''
    
    if (range === 'month') {
       d.setDate(1) // Set to 1st of month to avoid overflow issues
       d.setMonth(d.getMonth() - i)
       key = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-01`
       label = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}`
    } else if (range === 'week') {
       // Find Monday of current week
       const day = d.getDay()
       const diff = d.getDate() - day + (day === 0 ? -6 : 1)
       d.setDate(diff)
       // Go back i weeks
       d.setDate(d.getDate() - i * 7)
       
       key = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
       
       // Calculate week number of year for label
       const d2 = new Date(d)
       d2.setDate(d2.getDate() + 3) // Thursday
       const onejan = new Date(d2.getFullYear(), 0, 1)
       const weekNum = Math.ceil((((d2.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7)
       label = `${d.getMonth() + 1}月第${weekNum}周`
    } else {
       // Day
       d.setDate(d.getDate() - i)
       key = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
       const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
       label = i === 0 ? '今天' : weekDays[d.getDay()]
    }
    
    xAxisData.push(label)
    data.push(trendMap.get(key) || 0)
  }
  
  synapseChart.setOption({
     xAxis: { data: xAxisData },
     series: [{ data }]
  })
}

let latencyChart: echarts.ECharts | null = null

const openLatencyDetail = () => {
  latencyDetailVisible.value = true
}

const toggleLatencyDataSource = () => {
  useRealLatencyData.value = !useRealLatencyData.value
  ElMessage.success(useRealLatencyData.value ? '已切换至真实数据' : '已切换至演示数据')
  fetchLatencyStats()
}

const fetchLatencyStats = async () => {
    if (!latencyChart) return
    
    latencyChart.showLoading({ color: '#10b981', maskColor: 'rgba(255, 255, 255, 0)' })
    try {
        let xAxis: string[] = []
        let series: number[] = []
        let suggestion = ''

        if (useRealLatencyData.value) {
            const res = await getLatencyStats(latencyTimeRange.value)
            // @ts-ignore
            if (res.code === 200) {
                // @ts-ignore
                const data = res.data
                xAxis = data.xAxis || []
                series = data.series || []
                suggestion = data.suggestion || '暂无建议'
            }
        } else {
             // Mock
             let count = 7
             if (latencyTimeRange.value === 'month') count = 30
             if (latencyTimeRange.value === 'quarter') count = 12
             if (latencyTimeRange.value === 'year') count = 12
             if (latencyTimeRange.value === 'all') count = 6
             
             for (let i = 0; i < count; i++) {
                 xAxis.push(latencyTimeRange.value === 'week' ? `周${['一','二','三','四','五','六','日'][i]}` : `${i+1}`)
                 series.push(Math.floor(Math.random() * 2000) + 500)
             }
             suggestion = '演示数据：系统响应速度极快，平均延迟维持在 1.5s 以内，用户体验流畅。'
        }

        latencySuggestion.value = suggestion
        
        latencyChart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' },
                formatter: '{b}<br/>{a}: {c} ms'
            },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true, top: '10%' },
            xAxis: [
                {
                    type: 'category',
                    data: xAxis,
                    axisTick: { alignWithLabel: true },
                    axisLine: { lineStyle: { color: '#94a3b8' } },
                    axisLabel: { color: '#64748b', fontSize: 11 }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: 'ms',
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
                    axisLabel: { color: '#94a3b8' }
                }
            ],
            series: [
                {
                    name: '平均延迟',
                    type: 'bar',
                    barWidth: '40%',
                    data: series,
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#10b981' },
                            { offset: 1, color: '#6ee7b7' }
                        ]),
                        borderRadius: [4, 4, 0, 0]
                    },
                    animationDelay: (idx: number) => idx * 10
                }
            ]
        })
    } finally {
        latencyChart.hideLoading()
    }
}

const initLatencyChart = () => {
    nextTick(() => {
        if (latencyChartRef.value) {
            if (latencyChart) latencyChart.dispose()
            latencyChart = echarts.init(latencyChartRef.value)
            fetchLatencyStats()
        }
    })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats(selectedUserId.value, dashboardTimeRange.value, dashboardRankType.value)
    // @ts-ignore
    if (res.code === 200 && res.data) {
      // @ts-ignore
      Object.assign(stats.value, res.data)
      // 更新 Top 3 用户
      if (stats.value.topUsers) {
         topUsers.value = stats.value.topUsers.slice(0, 3)
      }
      
      // Update charts
      updateSynapseChart()
      updateRadarChart()
      if (wordCloudChart) {
        wordCloudChart.dispose()
        wordCloudChart = null
        if (wordCloudRef.value) {
          initCharts()
        }
      }
    }
    
    const listQuery = { pageNum: sessionPage.pageNum, pageSize: sessionPage.pageSize } as any
    if (selectedUserId.value) {
       listQuery.userId = selectedUserId.value
    }
    if (searchKeyword.value) {
       listQuery.keyword = searchKeyword.value
    }
    const listRes = await listAiSession(listQuery)
    // @ts-ignore
    if (listRes.code === 200) {
      // @ts-ignore
      tableData.value = listRes.data?.list || []
      // @ts-ignore
      sessionPage.total = listRes.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// Resize observer
const handleResize = () => {
   radarChart?.resize()
   radarDetailChart?.resize()
   wordCloudChart?.resize()
   funnelChart?.resize()
   synapseChart?.resize()
   activityChart?.resize()
   tokenChart?.resize()
   latencyChart?.resize()
}

onMounted(() => {
  fetchData()
  nextTick(() => {
     initCharts()
     initLogStream()
     window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
   window.removeEventListener('resize', handleResize)
   if (animationId) {
     cancelAnimationFrame(animationId)
     animationId = null
   }
   if (logEventSource) {
     logEventSource.close()
     logEventSource = null
   }
  if (radarZr) {
    radarZr.off('mousedown', onRadarDown)
    radarZr.off('mousemove', onRadarMove)
    radarZr.off('mouseup', onRadarUp)
    radarZr.off('globalout', onRadarUp)
  }
   radarChart?.dispose()
   radarDetailChart?.dispose()
   wordCloudChart?.dispose()
   funnelChart?.dispose()
   synapseChart?.dispose()
   activityChart?.dispose()
   tokenChart?.dispose()
   latencyChart?.dispose()
})

const getRadarOption = (forDetail = false) => {
    const radarData = (stats.value as any).radar || {}
    const dataValues = [
        radarData.knowledge || 60,
        radarData.activity || 60,
        radarData.memory || 60,
        radarData.speed || 60,
        radarData.skills || 60,
        radarData.accuracy || 60
    ]

    return {
       tooltip: { show: false },
       radar: {
         radius: forDetail ? '65%' : '72%',
         center: ['50%', '55%'],
         startAngle: radarDragStartAngle,
         indicator: [
           { name: '知识储备', max: 100 },
           { name: '交互活跃', max: 100 },
           { name: '记忆深度', max: 100 },
           { name: '响应速度', max: 100 },
           { name: '技能覆盖', max: 100 },
           { name: '准确度', max: 100 }
         ],
         shape: 'circle',
         splitNumber: 4,
         axisName: { 
           color: 'inherit',
           fontSize: forDetail ? 12 : 13,
           fontWeight: 700,
           formatter: (value: string) => {
             const index = ['知识储备', '交互活跃', '记忆深度', '响应速度', '技能覆盖', '准确度'].indexOf(value)
             const val = dataValues[index]
             return `{a|${value}}\n{b|${val}%}`
           },
           rich: {
             a: { fontSize: forDetail ? 12 : 13, fontWeight: 'bold', padding: [4, 0], color: '#334155' },
             b: { fontSize: 11, color: '#64748b', fontWeight: 'bold' }
           }
         },
         splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
         splitArea: { 
           show: true,
           areaStyle: {
             color: ['rgba(148, 163, 184, 0.02)', 'rgba(148, 163, 184, 0.05)']
           }
         },
         axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
       },
       series: [{
          type: 'radar',
          data: [{
             value: dataValues,
             name: '认知能力评估',
             itemStyle: { color: '#3b82f6' },
             areaStyle: { 
               color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                 { offset: 0, color: 'rgba(59, 130, 246, 0.5)' },
                 { offset: 1, color: 'rgba(59, 130, 246, 0.1)' }
               ])
             },
             lineStyle: { width: 3, color: '#3b82f6', type: 'dashed' },
             symbol: 'circle',
             symbolSize: 8,
             label: { 
               show: true,
               formatter: '{@value}',
               fontSize: 11,
               color: '#1e293b',
               fontWeight: 'bold'
             }
          }]
       }]
    }
}

const initRadarDetailChart = () => {
  if (radarDetailRef.value) {
     if (radarDetailChart) radarDetailChart.dispose()
     radarDetailChart = echarts.init(radarDetailRef.value)
     // @ts-ignore
     radarDetailChart.setOption(getRadarOption(true))
  }
}

const latencyDetailVisible = ref(false)
const latencyTimeRange = ref('week')
const latencyChartRef = ref<HTMLElement>()
const latencySuggestion = ref('')
const useRealLatencyData = ref(true)

const activityDetailVisible = ref(false)
const activityTimeRange = ref('week')
const activityChartRef = ref<HTMLElement>()
let activityChart: echarts.ECharts | null = null
const activitySuggestion = ref('')
const useRealActivityData = ref(true)

const knowledgeDetailVisible = ref(false)
const knowledgeStats = ref<any>({})
const useRealKnowledgeData = ref(true)
const knowledgeChartRef = ref(null)
const isSunburstHovered = ref(false)

const openKnowledgeDetail = () => {
  knowledgeDetailVisible.value = true
  fetchKnowledgeStats()
}

const toggleKnowledgeDataSource = () => {
  useRealKnowledgeData.value = !useRealKnowledgeData.value
  fetchKnowledgeStats()
}

const fetchKnowledgeStats = async () => {
  if (useRealKnowledgeData.value) {
    try {
      const res = await getKnowledgeStats()
      // @ts-ignore
      if (res.code === 200) {
        // @ts-ignore
        const data = res.data
        
        // 兼容处理：如果后端返回 recent_memories，映射到 rightBrain
        if (data.recent_memories && (!data.rightBrain || data.rightBrain.length === 0)) {
           data.rightBrain = data.recent_memories
        }
        
        knowledgeStats.value = data
      }
    } catch (e) {
      console.error(e)
    }
  } else {
    // Mock Data
    knowledgeStats.value = {
        totalChunks: 12450,
        memoryCount: 128,
        leftBrain: [
            {
                name: 'Backend',
                value: 6000,
                children: [
                    { name: 'Java', value: 5500 },
                    { name: 'XML', value: 500 }
                ]
            },
            {
                name: 'Frontend',
                value: 4000,
                children: [
                    { name: 'Vue', value: 3000 },
                    { name: 'TS', value: 1000 }
                ]
            },
            {
                name: 'Data',
                value: 1500,
                children: [
                    { name: 'SQL', value: 1500 }
                ]
            },
            {
                name: 'Docs',
                value: 950
            }
        ],
        rightBrain: [
            { type: 'preference', content: '用户偏好使用 Element Plus UI 库', time: '2026-02-14' },
            { type: 'rule', content: '所有 Service 接口必须添加 @Transactional 注解', time: '2026-02-13' },
            { type: 'business', content: '订单状态流转：Create -> Pay -> Ship -> Complete', time: '2026-02-12' },
            { type: 'correction', content: '修正了用户权限校验的逻辑漏洞', time: '2026-02-11' }
        ],
        suggestion: '系统运行良好，知识库覆盖全面。建议定期清理旧的记忆条目。'
    }
  }
  
  initKnowledgeChart()
}

const initKnowledgeChart = () => {
  nextTick(() => {
    if (!knowledgeChartRef.value) return
    
    // @ts-ignore
    const chart = echarts.init(knowledgeChartRef.value)
    const data = knowledgeStats.value.leftBrain || []
    
    const option = {
        color: ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#6366f1'],
        series: {
            type: 'sunburst',
            data: data,
            radius: [0, '95%'],
            sort: undefined,
            emphasis: {
                focus: 'ancestor'
            },
            levels: [
                {},
                {
                    r0: '15%',
                    r: '40%',
                    itemStyle: {
                        borderRadius: 5,
                        borderWidth: 2
                    },
                    label: {
                        rotate: 'tangential'
                    }
                },
                {
                    r0: '40%',
                    r: '75%',
                    label: {
                        align: 'right'
                    },
                    itemStyle: {
                        borderRadius: 5,
                        borderWidth: 2
                    }
                },
                {
                    r0: '75%',
                    r: '77%',
                    label: {
                        position: 'outside',
                        padding: 3,
                        silent: false
                    },
                    itemStyle: {
                        borderWidth: 2
                    }
                }
            ]
        },
        tooltip: {
            trigger: 'item',
            formatter: (params: any) => {
                const total = knowledgeStats.value.totalChunks || 1
                const percent = ((params.value / total) * 100).toFixed(1)
                return `${params.name}: ${params.value} Chunks (${percent}%)`
            }
        }
    };
    
    chart.setOption(option)
    
    chart.on('mouseover', () => {
        isSunburstHovered.value = true
    })
    
    chart.on('mouseout', () => {
        isSunburstHovered.value = false
    })
    
    window.addEventListener('resize', () => chart.resize())
  })
}

const openActivityDetail = () => {
  activityDetailVisible.value = true
}

const toggleActivityDataSource = () => {
  useRealActivityData.value = !useRealActivityData.value
  ElMessage.success(useRealActivityData.value ? '已切换至真实数据' : '已切换至演示数据')
  fetchActivityStats()
}

const generateMockActivityData = (range: string) => {
    let xAxis: string[] = []
    let series: number[] = []
    let suggestion = ''

    if (range === 'week') {
        const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        xAxis = days
        series = days.map(() => Math.floor(Math.random() * 100) + 20)
        suggestion = '演示数据：本周系统活跃度表现强劲，尤其是工作日期间交互频繁，体现了用户对 AI 辅助办公的高依赖度。'
    } else if (range === 'month') {
        const days = 30
        for (let i = 1; i <= days; i++) {
            xAxis.push(`${i}日`)
            series.push(Math.floor(Math.random() * 80) + 10)
        }
        suggestion = '演示数据：本月整体流量平稳，月中出现的小高峰可能与新功能发布有关，建议持续关注用户反馈。'
    } else if (range === 'quarter') {
        for (let i = 1; i <= 12; i++) {
            xAxis.push(`第${i}周`)
            series.push(Math.floor(Math.random() * 500) + 100)
        }
        suggestion = '演示数据：季度数据显示系统处于稳步增长期，周活跃用户数（WAU）环比增长 15%，用户粘性显著提升。'
    } else if (range === 'year') {
        const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        xAxis = months
        series = months.map(() => Math.floor(Math.random() * 2000) + 500)
        suggestion = '演示数据：年度回顾显示，随着知识库的不断丰富，系统交互量呈现指数级增长，已成为团队不可或缺的智能助手。'
    } else { // all
         const months = ['2025-08', '2025-09', '2025-10', '2025-11', '2025-12', '2026-01']
         xAxis = months
         series = months.map(() => Math.floor(Math.random() * 3000) + 800)
         suggestion = '演示数据：历史总览表明系统已稳定运行超过半年，累计服务次数突破万次，核心价值得到充分验证。'
    }
    return { xAxis, series, suggestion }
}

const fetchActivityStats = async () => {
    if (!activityChart) return
    
    activityChart.showLoading({ color: '#a855f7', maskColor: 'rgba(255, 255, 255, 0)' })
    try {
        let xAxis: string[] = []
        let series: number[] = []
        let suggestion = ''

        if (useRealActivityData.value) {
            const res = await getActivityStats(activityTimeRange.value)
            // @ts-ignore
            if (res.code === 200) {
                // @ts-ignore
                const data = res.data
                xAxis = data.xAxis || []
                series = data.series || []
                suggestion = data.suggestion || '暂无建议'
            }
        } else {
             const mock = generateMockActivityData(activityTimeRange.value)
             xAxis = mock.xAxis
             series = mock.series
             suggestion = mock.suggestion
        }

        activitySuggestion.value = suggestion
        
        activityChart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true, top: '10%' },
            xAxis: [
                {
                    type: 'category',
                    data: xAxis,
                    axisTick: { alignWithLabel: true },
                    axisLine: { lineStyle: { color: '#94a3b8' } },
                    axisLabel: { color: '#64748b', fontSize: 11 }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
                    axisLabel: { color: '#94a3b8' }
                }
            ],
            series: [
                {
                    name: '交互会话数',
                    type: 'bar',
                    barWidth: '40%',
                    data: series,
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#a855f7' },
                            { offset: 1, color: '#d8b4fe' }
                        ]),
                        borderRadius: [4, 4, 0, 0]
                    },
                    animationDelay: (idx: number) => idx * 10
                }
            ]
        })
    } finally {
        activityChart.hideLoading()
    }
}

const initActivityChart = () => {
    nextTick(() => {
        if (activityChartRef.value) {
            if (activityChart) activityChart.dispose()
            activityChart = echarts.init(activityChartRef.value)
            fetchActivityStats()
        }
    })
}

// Token Detail Logic
const tokenDetailVisible = ref(false)
const tokenTimeRange = ref('week')
const tokenChartRef = ref<HTMLElement>()
let tokenChart: echarts.ECharts | null = null
const tokenSuggestion = ref('')
const useRealTokenData = ref(true)

const openTokenDetail = () => {
  tokenDetailVisible.value = true
}

const toggleTokenDataSource = () => {
  useRealTokenData.value = !useRealTokenData.value
  ElMessage.success(useRealTokenData.value ? '已切换至真实数据' : '已切换至演示数据')
  fetchTokenStats()
}

const generateMockTokenData = (range: string) => {
    let xAxis: string[] = []
    let promptSeries: number[] = []
    let completionSeries: number[] = []
    let suggestion = ''

    if (range === 'week') {
        const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        xAxis = days
        promptSeries = days.map(() => Math.floor(Math.random() * 5000) + 1000)
        completionSeries = days.map(() => Math.floor(Math.random() * 2000) + 500)
        suggestion = '演示数据：本周 Token 消耗主要集中在 Prompt 输入上，表明用户倾向于使用长文本提问。'
    } else if (range === 'month') {
        const days = 30
        for (let i = 1; i <= days; i++) {
            xAxis.push(`${i}日`)
            promptSeries.push(Math.floor(Math.random() * 4000) + 800)
            completionSeries.push(Math.floor(Math.random() * 1500) + 300)
        }
        suggestion = '演示数据：本月算力消耗平稳，Completion 输出占比较低，模型推理效率较高。'
    } else if (range === 'quarter') {
        for (let i = 1; i <= 12; i++) {
            xAxis.push(`第${i}周`)
            promptSeries.push(Math.floor(Math.random() * 20000) + 5000)
            completionSeries.push(Math.floor(Math.random() * 8000) + 2000)
        }
        suggestion = '演示数据：季度数据显示算力成本稳步上升，建议关注高消耗用户并优化 Token 策略。'
    } else if (range === 'year') {
        const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        xAxis = months
        promptSeries = months.map(() => Math.floor(Math.random() * 80000) + 20000)
        completionSeries = months.map(() => Math.floor(Math.random() * 30000) + 8000)
        suggestion = '演示数据：年度消耗总量符合预期，RAG 检索带来的 Prompt 膨胀在可控范围内。'
    } else { // all
         const months = ['2023-08', '2023-09', '2023-10', '2023-11', '2023-12', '2024-01']
         xAxis = months
         promptSeries = months.map(() => Math.floor(Math.random() * 100000) + 25000)
         completionSeries = months.map(() => Math.floor(Math.random() * 40000) + 10000)
         suggestion = '演示数据：历史数据表明系统算力需求与活跃用户数呈正相关，扩容计划需提上日程。'
    }
    return { xAxis, promptSeries, completionSeries, suggestion }
}

const fetchTokenStats = async () => {
    if (!tokenChart) return
    
    tokenChart.showLoading({ color: '#f97316', maskColor: 'rgba(255, 255, 255, 0)' })
    try {
        let xAxis: string[] = []
        let promptSeries: number[] = []
        let completionSeries: number[] = []
        let suggestion = ''

        if (useRealTokenData.value) {
            const res = await getTokenStats(tokenTimeRange.value)
            // @ts-ignore
            if (res.code === 200) {
                // @ts-ignore
                const data = res.data
                xAxis = data.xAxis || []
                const totalSeries = data.series || []
                suggestion = data.suggestion || '暂无建议'
                
                // 模拟拆分 Prompt/Completion (因为后端目前只存了 Total)
                // 假设 70% Prompt, 30% Completion
                promptSeries = totalSeries.map((val: number) => Math.round(val * 0.7))
                completionSeries = totalSeries.map((val: number) => Math.round(val * 0.3))
                
                if (totalSeries.length > 0) {
                    suggestion += ' (注：当前数据库仅存储总 Token，Prompt/Completion 为按 7:3 比例估算展示)'
                }
            }
        } else {
             const mock = generateMockTokenData(tokenTimeRange.value)
             xAxis = mock.xAxis
             promptSeries = mock.promptSeries
             completionSeries = mock.completionSeries
             suggestion = mock.suggestion
        }

        tokenSuggestion.value = suggestion
        
        tokenChart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' },
                formatter: (params: any) => {
                    let res = `<div class="font-bold mb-1">${params[0].name}</div>`
                    let total = 0
                    params.forEach((item: any) => {
                        res += `<div class="flex items-center gap-2 text-xs">
                                  <span class="w-2 h-2 rounded-full" style="background:${item.color}"></span>
                                  <span class="text-slate-500">${item.seriesName}:</span>
                                  <span class="font-mono font-bold ml-auto">${item.value.toLocaleString()}</span>
                                </div>`
                        total += item.value
                    })
                    res += `<div class="mt-1 pt-1 border-t border-slate-200 dark:border-slate-700 flex justify-between text-xs font-bold">
                              <span>Total:</span>
                              <span class="font-mono">${total.toLocaleString()}</span>
                            </div>`
                    return res
                }
            },
            legend: {
                data: ['Prompt (输入)', 'Completion (输出)'],
                bottom: 0,
                textStyle: { color: '#64748b' }
            },
            grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true, top: '10%' },
            xAxis: [
                {
                    type: 'category',
                    data: xAxis,
                    axisTick: { alignWithLabel: true },
                    axisLine: { lineStyle: { color: '#94a3b8' } },
                    axisLabel: { color: '#64748b', fontSize: 11 }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
                    axisLabel: { color: '#94a3b8' }
                }
            ],
            series: [
                {
                    name: 'Prompt (输入)',
                    type: 'bar',
                    stack: 'total',
                    barWidth: '40%',
                    data: promptSeries,
                    itemStyle: {
                        color: '#f97316',
                        borderRadius: [0, 0, 0, 0]
                    },
                    animationDelay: (idx: number) => idx * 10
                },
                {
                    name: 'Completion (输出)',
                    type: 'bar',
                    stack: 'total',
                    barWidth: '40%',
                    data: completionSeries,
                    itemStyle: {
                        color: '#3b82f6',
                        borderRadius: [4, 4, 0, 0] // Top rounded
                    },
                    animationDelay: (idx: number) => idx * 10 + 100
                }
            ]
        })
    } finally {
        tokenChart.hideLoading()
    }
}

const initTokenChart = () => {
    nextTick(() => {
        if (tokenChartRef.value) {
            if (tokenChart) tokenChart.dispose()
            tokenChart = echarts.init(tokenChartRef.value)
            fetchTokenStats()
        }
    })
}
</script>

<style scoped>
.font-display {
  font-family: 'Space Grotesk', 'PingFang SC', sans-serif;
}
.font-num {
  font-family: 'Space Grotesk', sans-serif;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.05);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
  border: 2px solid transparent;
  background-clip: content-box;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #475569;
  border: 2px solid transparent;
  background-clip: content-box;
}
.dark .custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
}

.toggle-switch {
  @apply relative inline-flex h-5 w-10 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none;
}
.toggle-active {
  background-color: #41adec;
}
.toggle-inactive {
  @apply bg-slate-200 dark:bg-slate-700;
}
.toggle-dot {
  @apply pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out;
}
.translate-active {
  transform: translateX(1.25rem);
}
.translate-inactive {
  transform: translateX(0);
}

.text-gold { color: #D4AF37; }
.text-silver { color: #A8A8A8; }
.text-bronze { color: #CD7F32; }
.bg-primary { background-color: #41adec; }
.text-primary { color: #41adec; }

/* Dialog Styles Override */
:deep(.el-dialog) {
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 32px 64px -12px rgba(0,0,0,0.14);
  padding: 0;
  background-color: #fff;
}
.dark :deep(.el-dialog) {
  background-color: #0f172a;
  border: 1px solid #1e293b;
}
:deep(.el-dialog__header) {
  display: none;
}
:deep(.el-dialog__body) {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes pulse-slow {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

@keyframes fire-flicker {
  0%, 100% { text-shadow: 0 0 4px rgba(255, 69, 0, 0.4), 0 -1px 8px rgba(255, 140, 0, 0.3); transform: scale(1); filter: brightness(1); }
  50% { text-shadow: 0 0 10px rgba(255, 69, 0, 0.7), 0 -4px 12px rgba(255, 140, 0, 0.6); transform: scale(1.03); filter: brightness(1.2); }
}

@keyframes flame-surge {
  0%, 100% { transform: scale(1) translateY(0); opacity: 0.6; }
  50% { transform: scale(1.3) translateY(-2px); opacity: 0.8; }
}

@keyframes bounce-slight {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

@keyframes shimmer-fast {
  0% { transform: translateX(-100%) skewX(-15deg); }
  100% { transform: translateX(200%) skewX(-15deg); }
}

.animate-blink {
  animation: blink 1s step-end infinite;
}

.animate-pulse-slow {
  animation: pulse-slow 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animate-fire {
  animation: fire-flicker 2s ease-in-out infinite;
}

.animate-flame-surge {
  animation: flame-surge 1.5s ease-in-out infinite;
}

.animate-bounce-slight {
  animation: bounce-slight 2s ease-in-out infinite;
}

.animate-shimmer-fast {
  animation: shimmer-fast 3s linear infinite;
}
</style>

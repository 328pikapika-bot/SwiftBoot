<template>
  <div class="min-h-screen p-6 bg-[#f8fafc] dark:bg-[#0f172a] font-display transition-colors duration-300">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-black tracking-tight text-slate-900 dark:text-white flex items-center gap-3">
          <span class="w-2 h-8 bg-blue-500 rounded-full"></span>
          SwiftBoot 智能中枢
          <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-500/10 text-blue-500 border border-blue-500/20 uppercase tracking-wider">Live</span>
        </h1>
        <p class="text-slate-500 dark:text-slate-400 mt-1 ml-5 text-sm">AI 大脑实时监控与认知分析系统</p>
      </div>
      <div class="flex gap-3 items-center">
        <!-- 算力消耗榜 Widget (仅管理员) -->
        <div v-if="isAdmin && topUsers.length > 0" 
             @click="openUserSelect" 
             class="flex items-center bg-slate-50 dark:bg-slate-800 rounded-full border border-slate-200 dark:border-slate-700 cursor-pointer hover:border-blue-300 dark:hover:border-blue-600 transition-all group overflow-hidden relative mr-2 py-1.5 px-4 h-12"
        >
           <!-- Left: Title -->
           <div class="flex items-center gap-2 pr-4">
              <span class="text-sm font-bold text-slate-500 dark:text-slate-400">算力排行 TOP 10</span>
              <span class="material-icons-round text-slate-400 text-lg">bar_chart</span>
           </div>

           <!-- Vertical Divider -->
           <div class="h-4 w-px bg-slate-300 dark:bg-slate-600 mx-2"></div>

           <!-- Right: Top 3 -->
           <div class="flex items-center gap-4 pl-2">
              <!-- No.1 -->
              <div class="flex items-center gap-1.5">
                 <span class="material-icons-round text-yellow-500 text-base">emoji_events</span>
                 <span class="text-xs font-medium text-slate-600 dark:text-slate-300 max-w-[60px] truncate">{{ topUsers[0]?.nickname || topUsers[0]?.username }}</span>
              </div>
              <!-- No.2 -->
              <div class="flex items-center gap-1.5">
                 <span class="material-icons-round text-slate-400 text-base">emoji_events</span>
                 <span class="text-xs font-medium text-slate-600 dark:text-slate-300 max-w-[60px] truncate">{{ topUsers[1]?.nickname || topUsers[1]?.username }}</span>
              </div>
              <!-- No.3 -->
              <div class="flex items-center gap-1.5">
                 <span class="material-icons-round text-amber-700 text-base">emoji_events</span>
                 <span class="text-xs font-medium text-slate-600 dark:text-slate-300 max-w-[60px] truncate">{{ topUsers[2]?.nickname || topUsers[2]?.username }}</span>
              </div>
           </div>
        </div>

        <button @click="fetchData" class="px-4 py-2 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-all text-sm font-medium shadow-sm flex items-center gap-2 h-12">
          <span class="material-icons-round text-base">refresh</span>
          刷新数据
        </button>
      </div>
    </div>

    <!-- Layer 1: Vital Signs (核心生命体征) -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- 脑容量 -->
      <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300">
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
            <span class="text-3xl font-black text-slate-900 dark:text-white">24,501</span>
            <span class="text-xs font-medium text-emerald-500 flex items-center">
              +120
              <span class="material-icons-round text-[10px]">arrow_upward</span>
            </span>
          </div>
          <div class="mt-3 h-1.5 w-full bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
            <div class="h-full bg-blue-500 w-[75%] rounded-full relative overflow-hidden">
               <div class="absolute inset-0 bg-white/30 animate-[shimmer_2s_infinite]"></div>
            </div>
          </div>
          <p class="text-[10px] text-slate-400 mt-2">向量切片总数 (Chunks)</p>
        </div>
      </div>

      <!-- 突触活跃度 -->
      <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300">
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
              Live
            </span>
          </div>
          <div class="mt-3 h-12 w-full" ref="synapseChartRef"></div>
          <p class="text-[10px] text-slate-400 mt-2">今日交互会话数 (近7天趋势)</p>
        </div>
      </div>

      <!-- 思考延迟 -->
      <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300">
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
      <div class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-xl shadow-slate-200/40 dark:shadow-black/20 border border-slate-100 dark:border-slate-700/50 hover:-translate-y-1 transition-all duration-300">
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
      <div class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm p-6 relative overflow-hidden group hover:border-blue-500/30 transition-colors flex flex-col">
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
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[400px]">
       <!-- 索引构建日志 -->
       <div class="rounded-2xl bg-[#1e293b] text-slate-300 border border-slate-700 shadow-inner overflow-hidden flex flex-col">
          <div class="p-4 border-b border-slate-700 bg-slate-900/50 flex justify-between items-center">
             <h3 class="text-sm font-bold text-white flex items-center gap-2">
                <span class="material-icons-round text-emerald-400 text-sm animate-pulse">terminal</span>
                索引构建流
             </h3>
             <span class="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Live</span>
          </div>
          <div class="flex-1 p-4 overflow-y-auto font-mono text-xs space-y-3 custom-scrollbar">
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
       <div class="lg:col-span-2 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/50 shadow-sm flex flex-col">
          <div class="p-4 border-b border-slate-100 dark:border-slate-700 flex justify-between items-center">
             <h3 class="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <span class="material-icons-round text-indigo-500">question_answer</span>
                实时问答监控
             </h3>
             <div class="flex gap-2">
                <input placeholder="搜索会话..." class="bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg px-3 py-1 text-xs outline-none focus:border-blue-500 transition-colors" />
             </div>
          </div>
          
          <div class="flex-1 overflow-hidden" v-loading="loading">
             <div class="h-full overflow-y-auto custom-scrollbar">
                <table class="w-full text-sm text-left">
                   <thead class="text-xs text-slate-500 bg-slate-50 dark:bg-slate-900/50 uppercase sticky top-0 backdrop-blur-sm z-10">
                      <tr>
                         <th class="px-6 py-3 font-medium">用户</th>
                         <th class="px-6 py-3 font-medium">提问摘要</th>
                         <th class="px-6 py-3 font-medium">模型</th>
                         <th class="px-6 py-3 font-medium text-center">耗时</th>
                         <th class="px-6 py-3 font-medium text-right">时间</th>
                      </tr>
                   </thead>
                   <tbody class="divide-y divide-slate-100 dark:divide-slate-700/50">
                      <tr v-for="row in tableData" :key="row.id" class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors group cursor-pointer">
                         <td class="px-6 py-4">
                            <div class="flex items-center gap-3">
                               <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-xs shadow-md shadow-indigo-500/20">
                                  {{ (row.username || 'U').charAt(0).toUpperCase() }}
                               </div>
                               <span class="font-medium text-slate-700 dark:text-slate-200">{{ row.username }}</span>
                            </div>
                         </td>
                         <td class="px-6 py-4">
                            <div class="max-w-xs truncate text-slate-600 dark:text-slate-400 group-hover:text-blue-500 transition-colors">{{ row.question }}</div>
                         </td>
                         <td class="px-6 py-4">
                            <span class="px-2 py-1 rounded text-[10px] font-medium bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 border border-blue-100 dark:border-blue-800">
                               {{ row.model }}
                            </span>
                         </td>
                         <td class="px-6 py-4 text-center">
                            <span class="font-mono text-xs font-medium" :class="row.duration > 3000 ? 'text-orange-500' : 'text-emerald-500'">
                               {{ row.duration }}ms
                            </span>
                         </td>
                         <td class="px-6 py-4 text-right text-slate-400 text-xs">
                            {{ row.createTime }}
                         </td>
                      </tr>
                   </tbody>
                </table>
             </div>
          </div>
       </div>
    </div>

    <!-- 用户选择弹窗 (算力消耗榜) -->
    <el-dialog 
      v-model="userSelectVisible" 
      width="600px" 
      class="rank-dialog rounded-3xl overflow-hidden" 
      draggable
      align-center
      :show-close="false"
    >
      <template #header="{ close, titleId, titleClass }">
         <div class="relative h-32 bg-gradient-to-br from-amber-300 via-yellow-400 to-amber-500 p-6 flex flex-col justify-end overflow-hidden">
            <!-- 装饰背景 -->
            <div class="absolute top-0 right-0 p-4 opacity-20">
               <span class="material-icons-round text-9xl text-white transform rotate-12">emoji_events</span>
            </div>
            <div class="absolute top-4 right-4">
               <button @click="close" class="text-amber-900/50 hover:text-amber-900 transition-colors">
                  <span class="material-icons-round text-2xl">close</span>
               </button>
            </div>
            
            <h4 :id="titleId" :class="titleClass" class="text-2xl font-black text-amber-950 flex items-center gap-2 relative z-10">
               <span class="material-icons-round text-3xl animate-bounce">emoji_events</span>
               算力消耗排行榜
            </h4>
            <p class="text-amber-900/80 text-xs font-bold mt-1 relative z-10">Top Users by Token Consumption</p>
         </div>
      </template>

      <div class="p-6 bg-white dark:bg-slate-900">
        <div class="flex gap-3 mb-6">
          <div class="relative flex-1 group">
             <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-amber-500 material-icons-round text-lg transition-colors">search</span>
             <input 
               v-model="userSearchQuery" 
               placeholder="搜索用户名或昵称..." 
               class="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-800 border-2 border-transparent focus:border-amber-400 outline-none transition-all text-sm font-medium"
               @keyup.enter="fetchUserList"
             />
          </div>
          <button @click="fetchUserList" class="px-5 py-2 rounded-xl bg-slate-900 dark:bg-slate-700 hover:bg-amber-500 text-white text-sm font-bold transition-all shadow-lg hover:shadow-amber-500/30 active:scale-95">
            搜索
          </button>
        </div>

        <div class="rounded-2xl border border-slate-100 dark:border-slate-800 overflow-hidden shadow-sm">
          <el-table 
            :data="userList" 
            v-loading="userListLoading" 
            highlight-current-row 
            @current-change="handleUserSelectChange" 
            style="width: 100%" 
            height="380px"
            :header-cell-style="{ background: '#fff', color: '#94a3b8', fontSize: '12px', fontWeight: '800', borderBottom: '2px solid #f1f5f9' }"
          >
            <el-table-column width="70" align="center" label="排名">
              <template #default="scope">
                <div v-if="scope.$index < 3" class="flex justify-center">
                   <span class="material-icons-round text-2xl drop-shadow-sm" 
                         :class="['text-yellow-400', 'text-slate-400', 'text-amber-700'][scope.$index]">
                      emoji_events
                   </span>
                </div>
                <span v-else class="text-slate-400 font-bold text-sm">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            
            <el-table-column width="60" align="center">
              <template #default="scope">
                <div class="w-9 h-9 rounded-xl shadow-sm border-2 border-white dark:border-slate-700 flex items-center justify-center text-white text-xs font-black transform transition-transform hover:scale-110"
                     :class="scope.$index < 3 ? 'bg-gradient-to-br from-amber-400 to-orange-500' : 'bg-gradient-to-br from-slate-400 to-slate-500'">
                  {{ (scope.row.nickname || scope.row.username || 'U').charAt(0).toUpperCase() }}
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="username" label="用户" min-width="120">
               <template #default="scope">
                 <div class="flex flex-col">
                   <span class="font-bold text-slate-700 dark:text-slate-200 text-sm">{{ scope.row.nickname || scope.row.username }}</span>
                   <div class="flex items-center gap-2">
                      <span class="text-[10px] text-slate-400">@{{ scope.row.username }}</span>
                      <span v-if="scope.row.status === 1" class="px-1.5 py-0.5 rounded text-[9px] bg-red-100 text-red-500 font-bold">已禁用</span>
                   </div>
                 </div>
               </template>
            </el-table-column>
            
            <el-table-column prop="deptName" label="部门" min-width="100">
               <template #default="scope">
                 <span class="px-2.5 py-1 rounded-lg text-[10px] font-bold bg-slate-100 dark:bg-slate-800 text-slate-500">{{ scope.row.deptName || '未分配' }}</span>
               </template>
            </el-table-column>

            <el-table-column label="算力消耗" width="110" align="right">
               <template #default="scope">
                 <div class="font-mono font-black text-sm" :class="scope.$index < 3 ? 'text-amber-500' : 'text-slate-600'">
                    {{ scope.row.tokenConsumption ? (scope.row.tokenConsumption / 1000).toFixed(1) + 'k' : '0' }}
                 </div>
               </template>
            </el-table-column>
            
            <el-table-column label="状态" width="80" align="center">
               <template #default="scope">
                  <el-switch 
                    v-model="scope.row.status" 
                    :active-value="0" 
                    :inactive-value="1"
                    size="small"
                    style="--el-switch-on-color: #10b981; --el-switch-off-color: #ef4444"
                    @change="handleStatusChange(scope.row)"
                    @click.stop
                  />
               </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="mt-6 flex justify-between items-center">
          <span class="text-xs font-bold text-slate-400">Total Users: {{ userPage.total }}</span>
          <div class="flex gap-3">
             <button @click="userSelectVisible = false" class="px-5 py-2 rounded-xl text-slate-500 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-xs font-bold">
               关闭
             </button>
             <button 
               @click="confirmUserSelect" 
               :disabled="!tempSelectedUser"
               class="px-6 py-2 rounded-xl bg-amber-400 hover:bg-amber-500 text-amber-950 text-xs font-bold shadow-lg shadow-amber-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all hover:-translate-y-0.5"
             >
               查看该用户数据
             </button>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, listAiSession, getUserTokenStats } from '@/api/monitor/ai-session'
import { changeStatus } from '@/api/system/user'
import type { User } from '@/api/system/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 扩展 User 类型以包含 dashboard 所需的额外字段
interface DashboardUser extends User {
  userId?: number
  tokenConsumption?: number
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

// Data
const stats = ref({
  todayCount: 0,
  todayTokens: 0,
  avgDuration: 0,
  activeUsers: [] as any[],
  topUsers: [] as DashboardUser[],
  last7DaysTrend: [] as number[] // 模拟近7天数据
})

const tableData = ref<any[]>([])
const loading = ref(false)

// User Selection Logic
const userSelectVisible = ref(false)
const userList = ref<DashboardUser[]>([])
const userListLoading = ref(false)
const userSearchQuery = ref('')
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
      username: userSearchQuery.value
    })
    // @ts-ignore
    if (res.code === 200) {
      // @ts-ignore
      userList.value = res.data.list || []
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

// Mock Logs
const indexLogs = ref([
   { time: '10:42:01', msg: '检测到变更: SysUserController.java', color: 'text-blue-400' },
   { time: '10:42:02', msg: '正在解析 AST 语法树...', color: 'text-slate-400' },
   { time: '10:42:03', msg: '向量化完成: 提取 3 个方法，新增 12 个向量切片', color: 'text-emerald-400' },
   { time: '10:45:12', msg: '检测到变更: index.vue', color: 'text-blue-400' },
   { time: '10:45:15', msg: '索引更新完毕', color: 'text-emerald-400' }
])

// Refs
const radarRef = ref<HTMLElement>()
const wordCloudRef = ref<HTMLElement>()
const funnelRef = ref<HTMLElement>()

let radarChart: echarts.ECharts | null = null
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

// Mock Data for Charts
const initCharts = () => {
  if (synapseChartRef.value) {
     synapseChart = echarts.init(synapseChartRef.value)
     const data = stats.value.last7DaysTrend.length ? stats.value.last7DaysTrend : [12, 18, 24, 35, 20, 28, stats.value.todayCount || 42]
     
     synapseChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 0, right: 0, top: 5, bottom: 0 },
        xAxis: { type: 'category', show: false, data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
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

  if (radarRef.value) {
    radarChart = echarts.init(radarRef.value)
    const radarOption = {
       tooltip: { show: false },
       radar: {
         radius: '72%',
         center: ['50%', '55%'],
          startAngle: radarDragStartAngle,
         indicator: [
           { name: 'Java 后端', max: 100 },
           { name: 'Vue 前端', max: 100 },
           { name: 'Python 引擎', max: 100 },
           { name: '数据库/SQL', max: 100 },
           { name: '文档/Markdown', max: 100 },
           { name: 'Shell/脚本', max: 100 }
         ],
         shape: 'circle',
         splitNumber: 4,
         axisName: { 
           color: 'inherit',
           fontSize: 13,
           fontWeight: 700,
           formatter: (value: string) => {
             const map: Record<string, number> = {
               'Java 后端': 85,
               'Vue 前端': 65,
               'Python 引擎': 45,
               '数据库/SQL': 70,
               '文档/Markdown': 30,
               'Shell/脚本': 50
             }
             return `{a|${value}}\n{b|${map[value]}%}`
           },
           rich: {
             a: { fontSize: 13, fontWeight: 'bold', padding: [4, 0], color: '#334155' },
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
             value: [85, 65, 45, 70, 30, 50],
             name: '技术栈覆盖度',
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

  if (wordCloudRef.value) {
    wordCloudChart = echarts.init(wordCloudRef.value)
    
    // 3D 球体词云配置
    const tags = [
      { name: 'Spring Boot', value: 90, category: 0 },
      { name: 'Vue3', value: 80, category: 1 },
      { name: 'RAG', value: 75, category: 2 },
      { name: 'DeepSeek', value: 70, category: 2 },
      { name: 'Redis', value: 60, category: 3 },
      { name: 'ChromaDB', value: 55, category: 3 },
      { name: 'Element Plus', value: 50, category: 1 },
      { name: 'MyBatis', value: 45, category: 0 },
      { name: 'Spring Security', value: 40, category: 0 },
      { name: 'Python', value: 35, category: 4 },
      { name: 'Docker', value: 30, category: 5 },
      { name: 'Nginx', value: 25, category: 5 },
      { name: 'LangChain', value: 65, category: 2 },
      { name: 'MySQL', value: 68, category: 3 },
      { name: 'TypeScript', value: 55, category: 1 }
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

  if (funnelRef.value) {
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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats(selectedUserId.value)
    // @ts-ignore
    if (res.code === 200 && res.data) {
      // @ts-ignore
      Object.assign(stats.value, res.data)
      // 更新 Top 3 用户
      if (stats.value.topUsers) {
         topUsers.value = stats.value.topUsers.slice(0, 3)
      }
      
      // Update charts if they exist
      if (synapseChart) {
         const data = stats.value.last7DaysTrend.length ? stats.value.last7DaysTrend : [12, 18, 24, 35, 20, 28, stats.value.todayCount || 42]
         synapseChart.setOption({ series: [{ data }] })
      }
    }
    
    const listQuery = { pageNum: 1, pageSize: 10 } as any
    if (selectedUserId.value) {
       listQuery.userId = selectedUserId.value
    }
    const listRes = await listAiSession(listQuery)
    // @ts-ignore
    if (listRes.code === 200) {
      // @ts-ignore
      tableData.value = listRes.rows || []
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
   wordCloudChart?.resize()
   funnelChart?.resize()
   synapseChart?.resize()
}

onMounted(() => {
  fetchData()
  nextTick(() => {
     initCharts()
     window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
   window.removeEventListener('resize', handleResize)
   if (animationId) {
     cancelAnimationFrame(animationId)
     animationId = null
   }
  if (radarZr) {
    radarZr.off('mousedown', onRadarDown)
    radarZr.off('mousemove', onRadarMove)
    radarZr.off('mouseup', onRadarUp)
    radarZr.off('globalout', onRadarUp)
  }
   radarChart?.dispose()
   wordCloudChart?.dispose()
   funnelChart?.dispose()
   synapseChart?.dispose()
})
</script>

<style scoped>
.font-display {
  font-family: 'Inter', system-ui, sans-serif;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #334155;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.animate-blink {
  animation: blink 1s step-end infinite;
}
</style>

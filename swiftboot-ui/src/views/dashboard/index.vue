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

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
      <div class="bg-white dark:bg-slate-900 rounded-[28px] border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col overflow-hidden">
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
          <div>
             <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                <span class="w-1.5 h-1.5 rounded-full bg-purple-500"></span>
                向量索引更新
             </h3>
             <div class="space-y-6 overflow-y-auto max-h-[300px] pr-2 custom-scrollbar">
                  <div
                    v-for="log in vectorLogs"
                    :key="log.id"
                    class="flex gap-3 cursor-pointer rounded-lg px-2 py-1.5 -mx-2 transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/70"
                    :title="buildLogHeadline(log)"
                    @click="openOperLogDetail(log.id)"
                  >
                    <div class="mt-1 w-2 h-2 rounded-full shrink-0 bg-purple-500"></div>
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

          <div class="border-l border-slate-100 dark:border-slate-800 pl-6 lg:block hidden">
             <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                用户操作动态
             </h3>
              <div class="space-y-6 overflow-y-auto max-h-[300px] pr-2 custom-scrollbar">
                  <div
                    v-for="log in operLogs"
                    :key="log.id"
                    class="flex gap-3 cursor-pointer rounded-lg px-2 py-1.5 -mx-2 transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/70"
                    :title="buildLogHeadline(log)"
                    @click="openOperLogDetail(log.id)"
                  >
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
        <router-link to="/monitor/operlog" class="p-4 text-center text-sm font-medium text-blue-500 hover:bg-slate-50 dark:hover:bg-slate-800 border-t border-slate-100 dark:border-slate-800 transition-colors">
          查看全部日志
        </router-link>
      </div>

      <div class="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden flex flex-col">
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

      <div class="hidden">
      <div
        class="order-2 relative overflow-hidden bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:shadow-purple-500/10 cursor-pointer"
        role="button"
        tabindex="0"
        @click="goToAiDashboard"
        @keydown.enter.prevent="goToAiDashboard"
        @keydown.space.prevent="goToAiDashboard"
      >
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(168,85,247,0.16),_transparent_48%),radial-gradient(circle_at_bottom_left,_rgba(59,130,246,0.12),_transparent_42%)] pointer-events-none"></div>
        <div class="relative z-10">
          <div class="flex justify-between items-start gap-3 mb-4">
            <div class="flex items-center gap-3">
              <div class="p-2.5 rounded-xl bg-gradient-to-br from-purple-50 to-sky-50 dark:from-purple-900/30 dark:to-sky-900/20 border border-purple-100/80 dark:border-purple-800/40 shadow-sm">
                <span class="material-icons-round text-purple-500">auto_awesome</span>
              </div>
              <div>
                <div class="text-sm font-semibold text-slate-600 dark:text-slate-300">智能索引总览</div>
                <div class="text-xs text-slate-400 dark:text-slate-500">{{ aiIndexSubtitle }}</div>
              </div>
            </div>
            <span
              class="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-semibold"
              :class="aiIndexStatusClass"
            >
              <span class="w-1.5 h-1.5 rounded-full" :class="aiIndexStatusDotClass"></span>
              {{ aiIndexStatusText }}
            </span>
          </div>

          <div class="flex items-end justify-between gap-4 mb-4">
            <div>
              <div class="text-xs uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">All Indexed Chunks</div>
              <div class="mt-1 text-3xl font-black tracking-tight text-slate-900 dark:text-white">{{ aiIndexDisplay }}</div>
            </div>
            <div class="hidden xl:flex items-center gap-2 rounded-2xl border border-white/70 dark:border-slate-800/70 bg-white/80 dark:bg-slate-950/40 backdrop-blur px-3 py-2 shadow-sm">
              <span class="material-icons-round text-base text-sky-500">dataset</span>
              <div>
                <div class="text-[11px] font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">已聚合类型</div>
                <div class="text-sm font-bold text-slate-700 dark:text-slate-200">{{ aiIndexTypeCount }} 类</div>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div
              v-for="item in aiIndexBreakdown"
              :key="item.key"
              class="rounded-xl border border-slate-200/70 dark:border-slate-700/70 bg-white/75 dark:bg-slate-950/35 backdrop-blur px-3 py-2.5 shadow-sm"
            >
              <div class="flex items-center justify-between gap-2">
                <div class="flex items-center gap-2 min-w-0">
                  <span class="material-icons-round text-base shrink-0" :class="item.iconClass">{{ item.icon }}</span>
                  <span class="text-xs font-semibold text-slate-600 dark:text-slate-300 truncate">{{ item.label }}</span>
                </div>
                <span class="text-sm font-black text-slate-900 dark:text-white">{{ formatCount(item.value) }}</span>
              </div>
            </div>
            <div
              v-if="!aiIndexBreakdown.length"
              class="col-span-2 rounded-xl border border-dashed border-slate-200 dark:border-slate-700 bg-slate-50/70 dark:bg-slate-950/30 px-3 py-4 text-center text-xs text-slate-500 dark:text-slate-400"
            >
              AI 引擎启动后会在这里展示各类型索引数量
            </div>
          </div>

          <div class="mt-4 flex items-center justify-between gap-3 border-t border-slate-200/70 dark:border-slate-800/70 pt-4">
            <div class="text-[11px] text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
              <span class="material-icons-round text-sm">touch_app</span>
              点击卡片可进入 AI 看板
            </div>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white/85 dark:bg-slate-950/50 px-3 py-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300 transition-all hover:border-sky-300 hover:text-sky-600 dark:hover:border-sky-700 dark:hover:text-sky-300"
                @click.stop="goToAiDashboard"
              >
                <span class="material-icons-round text-sm">dashboard</span>
                AI 看板
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-full border border-purple-200 dark:border-purple-800 bg-purple-50/90 dark:bg-purple-950/40 px-3 py-1.5 text-xs font-semibold text-purple-600 dark:text-purple-300 transition-all hover:bg-purple-100 dark:hover:bg-purple-900/50"
                @click.stop="goToKnowledgeDetail"
              >
                <span class="material-icons-round text-sm">insights</span>
                知识详情
              </button>
            </div>
          </div>
        </div>
      </div>

        <div class="order-1 relative overflow-hidden rounded-[28px] border border-slate-200/80 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 p-4 shadow-lg shadow-slate-200/20 dark:shadow-slate-950/20">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.14),_transparent_32%),radial-gradient(circle_at_bottom_right,_rgba(249,115,22,0.1),_transparent_38%)] pointer-events-none"></div>
          <div class="relative z-10">
            <div class="flex items-start justify-between gap-4 mb-4">
              <div>
                <div class="inline-flex items-center gap-2 rounded-full border border-emerald-200/70 dark:border-emerald-900/50 bg-emerald-50/80 dark:bg-emerald-950/30 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-emerald-600 dark:text-emerald-300">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                  Runtime Pulse
                </div>
                <h3 class="mt-2 text-lg font-black tracking-tight text-slate-900 dark:text-white">实时运行总览</h3>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">合并展示内存、JVM 和在线时长，保持实时刷新。</p>
              </div>
              <div class="rounded-2xl border border-white/70 dark:border-slate-800/70 bg-white/80 dark:bg-slate-950/45 px-3 py-2 backdrop-blur shadow-sm">
                <div class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">Last Poll</div>
                <div class="mt-1 text-sm font-bold text-slate-700 dark:text-slate-200">{{ responseTimeDisplay }}</div>
              </div>
            </div>

            <div class="space-y-2.5">
              <div
                v-for="item in runtimeSummaryItems"
                :key="item.label"
                class="rounded-2xl border border-slate-200/80 dark:border-slate-700/80 bg-white/80 dark:bg-slate-950/35 px-4 py-3 shadow-sm"
              >
                <div class="flex items-center justify-between gap-4">
                  <div class="flex items-center gap-3 min-w-0">
                    <div class="w-10 h-10 rounded-2xl flex items-center justify-center border" :class="item.iconWrapClass">
                      <span class="material-icons-round text-[18px]" :class="item.iconClass">{{ item.icon }}</span>
                    </div>
                    <div class="min-w-0">
                      <div class="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">{{ item.label }}</div>
                      <div class="mt-1 text-xs text-slate-500 dark:text-slate-400 truncate">{{ item.detail }}</div>
                    </div>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="text-xl font-black tracking-tight text-slate-900 dark:text-white">{{ item.value }}</div>
                    <div class="mt-0.5 text-[11px] font-semibold" :class="item.stateClass">{{ item.state }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-3 grid grid-cols-2 gap-3">
              <div class="rounded-2xl border border-slate-200/80 dark:border-slate-700/80 bg-slate-50/85 dark:bg-slate-950/35 px-4 py-3">
                <div class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">AI 记忆</div>
                <div class="mt-2 text-2xl font-black text-slate-900 dark:text-white">{{ formatCount(aiStats.memory_count || 0) }}</div>
              </div>
              <div class="rounded-2xl border border-slate-200/80 dark:border-slate-700/80 bg-slate-50/85 dark:bg-slate-950/35 px-4 py-3">
                <div class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">引擎状态</div>
                <div class="mt-2 text-sm font-bold" :class="aiEngineStateClass">{{ aiEngineStateText }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Grid -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-8">
      <div
        class="relative overflow-hidden bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:shadow-purple-500/10 cursor-pointer"
        role="button"
        tabindex="0"
        @click="goToAiDashboard"
        @keydown.enter.prevent="goToAiDashboard"
        @keydown.space.prevent="goToAiDashboard"
      >
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(168,85,247,0.16),_transparent_48%),radial-gradient(circle_at_bottom_left,_rgba(59,130,246,0.12),_transparent_42%)] pointer-events-none"></div>
        <div class="relative z-10">
          <div class="flex justify-between items-start gap-3 mb-4">
            <div class="flex items-center gap-3">
              <div class="p-2.5 rounded-xl bg-gradient-to-br from-purple-50 to-sky-50 dark:from-purple-900/30 dark:to-sky-900/20 border border-purple-100/80 dark:border-purple-800/40 shadow-sm">
                <span class="material-icons-round text-purple-500">auto_awesome</span>
              </div>
              <div>
                <div class="text-sm font-semibold text-slate-600 dark:text-slate-300">智能索引总览</div>
                <div class="text-xs text-slate-400 dark:text-slate-500">{{ aiIndexSubtitle }}</div>
              </div>
            </div>
            <span
              class="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-semibold"
              :class="aiIndexStatusClass"
            >
              <span class="w-1.5 h-1.5 rounded-full" :class="aiIndexStatusDotClass"></span>
              {{ aiIndexStatusText }}
            </span>
          </div>

          <div class="flex items-end justify-between gap-4 mb-4">
            <div>
              <div class="text-xs uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">All Indexed Chunks</div>
              <div class="mt-1 text-3xl font-black tracking-tight text-slate-900 dark:text-white">{{ aiIndexDisplay }}</div>
            </div>
            <div class="hidden 2xl:flex items-center gap-2 rounded-2xl border border-white/70 dark:border-slate-800/70 bg-white/80 dark:bg-slate-950/40 backdrop-blur px-3 py-2 shadow-sm">
              <span class="material-icons-round text-base text-sky-500">dataset</span>
              <div>
                <div class="text-[11px] font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">已聚合类型</div>
                <div class="text-sm font-bold text-slate-700 dark:text-slate-200">{{ aiIndexTypeCount }} 类</div>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div
              v-for="item in aiIndexBreakdown"
              :key="item.key"
              class="rounded-xl border border-slate-200/70 dark:border-slate-700/70 bg-white/75 dark:bg-slate-950/35 backdrop-blur px-3 py-2.5 shadow-sm"
            >
              <div class="flex items-center justify-between gap-2">
                <div class="flex items-center gap-2 min-w-0">
                  <span class="material-icons-round text-base shrink-0" :class="item.iconClass">{{ item.icon }}</span>
                  <span class="text-xs font-semibold text-slate-600 dark:text-slate-300 truncate">{{ item.label }}</span>
                </div>
                <span class="text-sm font-black text-slate-900 dark:text-white">{{ formatCount(item.value) }}</span>
              </div>
            </div>
            <div
              v-if="!aiIndexBreakdown.length"
              class="col-span-2 rounded-xl border border-dashed border-slate-200 dark:border-slate-700 bg-slate-50/70 dark:bg-slate-950/30 px-3 py-4 text-center text-xs text-slate-500 dark:text-slate-400"
            >
              AI 引擎启动后会在这里展示各类型索引数量
            </div>
          </div>

          <div class="mt-4 flex items-center justify-between gap-3 border-t border-slate-200/70 dark:border-slate-800/70 pt-4">
            <div class="text-[11px] text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
              <span class="material-icons-round text-sm">touch_app</span>
              点击卡片可进入 AI 看板
            </div>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white/85 dark:bg-slate-950/50 px-3 py-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300 transition-all hover:border-sky-300 hover:text-sky-600 dark:hover:border-sky-700 dark:hover:text-sky-300"
                @click.stop="goToAiDashboard"
              >
                <span class="material-icons-round text-sm">dashboard</span>
                AI 看板
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-full border border-purple-200 dark:border-purple-800 bg-purple-50/90 dark:bg-purple-950/40 px-3 py-1.5 text-xs font-semibold text-purple-600 dark:text-purple-300 transition-all hover:bg-purple-100 dark:hover:bg-purple-900/50"
                @click.stop="goToKnowledgeDetail"
              >
                <span class="material-icons-round text-sm">insights</span>
                知识详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-[28px] border border-slate-200/80 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 p-4 shadow-lg shadow-slate-200/20 dark:shadow-slate-950/20">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.14),_transparent_32%),radial-gradient(circle_at_bottom_right,_rgba(249,115,22,0.1),_transparent_38%)] pointer-events-none"></div>
        <div class="relative z-10">
          <div class="flex items-start justify-between gap-4 mb-4">
            <div>
              <div class="inline-flex items-center gap-2 rounded-full border border-emerald-200/70 dark:border-emerald-900/50 bg-emerald-50/80 dark:bg-emerald-950/30 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-emerald-600 dark:text-emerald-300">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                Runtime Pulse
              </div>
              <h3 class="mt-2 text-lg font-black tracking-tight text-slate-900 dark:text-white">实时运行总览</h3>
              <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">合并展示内存、JVM 和在线时长，保持实时刷新。</p>
            </div>
            <div class="rounded-2xl border border-white/70 dark:border-slate-800/70 bg-white/80 dark:bg-slate-950/45 px-3 py-2 backdrop-blur shadow-sm">
              <div class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">Last Poll</div>
              <div class="mt-1 text-sm font-bold text-slate-700 dark:text-slate-200">{{ responseTimeDisplay }}</div>
            </div>
          </div>

          <div class="space-y-2.5">
            <div
              v-for="item in runtimeSummaryItems"
              :key="item.label"
              class="rounded-2xl border border-slate-200/80 dark:border-slate-700/80 bg-white/80 dark:bg-slate-950/35 px-4 py-3 shadow-sm"
            >
              <div class="flex items-center justify-between gap-4">
                <div class="flex items-center gap-3 min-w-0">
                  <div class="w-10 h-10 rounded-2xl flex items-center justify-center border" :class="item.iconWrapClass">
                    <span class="material-icons-round text-[18px]" :class="item.iconClass">{{ item.icon }}</span>
                  </div>
                  <div class="min-w-0">
                    <div class="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">{{ item.label }}</div>
                    <div class="mt-1 text-xs text-slate-500 dark:text-slate-400 truncate">{{ item.detail }}</div>
                  </div>
                </div>
                <div class="text-right shrink-0">
                  <div class="text-xl font-black tracking-tight text-slate-900 dark:text-white">{{ item.value }}</div>
                  <div class="mt-0.5 text-[11px] font-semibold" :class="item.stateClass">{{ item.state }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-[28px] border border-slate-200/80 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 shadow-xl shadow-slate-200/25 dark:shadow-slate-950/25">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(56,189,248,0.12),_transparent_34%),radial-gradient(circle_at_bottom_right,_rgba(99,102,241,0.12),_transparent_40%)] pointer-events-none"></div>
        <div class="relative z-10 p-6">
          <div class="flex items-start justify-between gap-4 mb-5">
            <div>
              <div class="inline-flex items-center gap-2 rounded-full border border-sky-200/70 dark:border-sky-900/50 bg-sky-50/80 dark:bg-sky-950/30 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-sky-600 dark:text-sky-300">
                <span class="w-1.5 h-1.5 rounded-full bg-sky-500 animate-pulse"></span>
                Quick Access
              </div>
              <h2 class="mt-3 text-2xl font-black tracking-tight text-slate-900 dark:text-white">快捷入口</h2>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">保留高频模块，并补上 AI 看板与配置管理两个更常用的入口。</p>
            </div>
            <div class="text-right shrink-0">
              <div class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">入口数量</div>
              <div class="mt-1 text-2xl font-black text-slate-900 dark:text-white">{{ quickLinks.length }}</div>
            </div>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <router-link
              v-for="item in quickLinks"
              :key="item.path"
              :to="item.path"
              class="group relative overflow-hidden rounded-2xl border border-slate-200/80 dark:border-slate-700/70 bg-white/90 dark:bg-slate-950/35 p-4 transition-all duration-300 hover:-translate-y-1 hover:border-blue-300/80 dark:hover:border-blue-700/60 hover:shadow-lg hover:shadow-blue-500/10"
            >
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/[0.04] via-transparent to-emerald-500/[0.05] opacity-0 group-hover:opacity-100 transition-opacity"></div>
              <div class="relative z-10 flex h-full flex-col justify-between gap-5">
                <div class="flex items-start justify-between gap-2">
                  <div class="w-10 h-10 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center border border-slate-200/70 dark:border-slate-700/70 group-hover:bg-blue-500/10 group-hover:border-blue-200 dark:group-hover:border-blue-800 transition-all">
                    <span class="material-symbols-outlined text-[20px] text-slate-600 dark:text-slate-300 group-hover:text-blue-500 transition-colors">{{ item.icon }}</span>
                  </div>
                  <span class="material-icons-round text-sm text-slate-300 dark:text-slate-600 group-hover:text-blue-400 transition-colors">arrow_outward</span>
                </div>
                <div>
                  <div class="text-[11px] uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500 mb-1.5">Module</div>
                  <div class="text-sm font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-300 transition-colors">{{ item.title }}</div>
                </div>
              </div>
            </router-link>
          </div>
        </div>
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
import { ref, onMounted, onUnmounted, onActivated, onDeactivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getServer } from '@/api/monitor/server'
import { list as getOperLogs } from '@/api/monitor/operlog'
import request, { ApiResponse } from '@/utils/request'

interface AiStats {
  knowledge_count: number
  memory_count: number
  total_chunks?: number
  engine_up?: boolean
  languages?: Record<string, number>
  file_types?: Record<string, number>
}

const serverStats = ref<any>({})
const responseTime = ref(0)
const uptime = ref('')
const aiStats = ref<AiStats>({ knowledge_count: 0, memory_count: 0, total_chunks: 0 })
const operLogs = ref<any[]>([])
const vectorLogs = ref<any[]>([])
const isLogsLoading = ref(false)
const router = useRouter()
let timer: any = null
let statsTimer: any = null

const quickLinks = [
  { title: '用户管理', path: '/system/user', icon: 'person' },
  { title: '角色管理', path: '/system/role', icon: 'manage_accounts' },
  { title: '菜单管理', path: '/system/menu', icon: 'menu' },
  { title: '字典管理', path: '/system/dict', icon: 'book' },
  { title: '代码生成', path: '/tool/gen', icon: 'terminal' },
  { title: 'AI 看板', path: '/ai/dashboard', icon: 'dashboard' },
  { title: '配置管理', path: '/tool/config', icon: 'tune' },
  { title: '监控大屏', path: '/monitor/server', icon: 'monitoring' }
]

const aiIndexBreakdown = computed(() => {
  if (aiStats.value.engine_up === false) return []

  const fileTypes = aiStats.value.file_types || {}
  const languages = aiStats.value.languages || {}
  const totalKnowledge = Number(aiStats.value.knowledge_count || aiStats.value.total_chunks || 0)
  const typeMap: Record<string, { label: string; icon: string; iconClass: string }> = {
    java: { label: 'Java', icon: 'dns', iconClass: 'text-blue-500' },
    vue: { label: 'Vue', icon: 'web', iconClass: 'text-cyan-500' },
    ts: { label: 'TypeScript', icon: 'code', iconClass: 'text-sky-500' },
    js: { label: 'JavaScript', icon: 'integration_instructions', iconClass: 'text-yellow-500' },
    py: { label: 'Python', icon: 'smart_toy', iconClass: 'text-emerald-500' },
    sql: { label: 'SQL', icon: 'database', iconClass: 'text-amber-500' },
    md: { label: 'Markdown', icon: 'description', iconClass: 'text-fuchsia-500' },
    txt: { label: 'Text', icon: 'article', iconClass: 'text-violet-500' },
    doc: { label: 'Doc', icon: 'article', iconClass: 'text-purple-500' }
  }

  const fileTypeEntries = Object.entries(fileTypes)
    .map(([key, value]) => ({
      key,
      label: typeMap[key]?.label || key.toUpperCase(),
      icon: typeMap[key]?.icon || 'category',
      iconClass: typeMap[key]?.iconClass || 'text-slate-500',
      value: Number(value || 0)
    }))
    .filter(item => item.value > 0)
    .sort((a, b) => b.value - a.value)

  if (fileTypeEntries.length) {
    const typedTotal = fileTypeEntries.reduce((sum, item) => sum + item.value, 0)
    const derivedDocs = Math.max(totalKnowledge - typedTotal, 0)
    const hasDocType = fileTypeEntries.some(item => ['md', 'txt', 'doc'].includes(item.key))

    if (derivedDocs > 0 && !hasDocType) {
      fileTypeEntries.push({
        key: 'docs-derived',
        label: 'Docs',
        icon: 'description',
        iconClass: 'text-fuchsia-500',
        value: derivedDocs
      })
    }
  }

  if (fileTypeEntries.length) {
    return fileTypeEntries.sort((a, b) => b.value - a.value)
  }

  const languageEntries = [
    { key: 'java', label: 'Java', icon: 'dns', iconClass: 'text-blue-500', value: Number(languages.Java || 0) },
    { key: 'frontend', label: '前端', icon: 'web', iconClass: 'text-cyan-500', value: Number(languages.Frontend || 0) },
    { key: 'python', label: 'Python', icon: 'smart_toy', iconClass: 'text-emerald-500', value: Number(languages.Python || 0) },
    { key: 'sql', label: 'SQL', icon: 'database', iconClass: 'text-amber-500', value: Number(languages.SQL || 0) },
    { key: 'docs', label: '文档', icon: 'description', iconClass: 'text-fuchsia-500', value: Number(languages.Docs || 0) },
    { key: 'other', label: '其他', icon: 'category', iconClass: 'text-slate-500', value: Number(languages.Other || 0) }
  ]

  return languageEntries.filter(item => item.value > 0)
})

const aiIndexTypeCount = computed(() => aiIndexBreakdown.value.length)

const aiIndexDisplay = computed(() => {
  if (aiStats.value.engine_up === false) return '未启动'
  const total = aiStats.value.total_chunks
  const breakdownTotal = aiIndexBreakdown.value.reduce((sum, item) => sum + item.value, 0)
  const knowledge = aiStats.value.knowledge_count
  const count = Math.max(
    typeof total === 'number' ? total : 0,
    breakdownTotal,
    typeof knowledge === 'number' ? knowledge : 0
  )
  return `${count.toLocaleString()} 件`
})

const aiIndexSubtitle = computed(() => {
  if (aiStats.value.engine_up === false) return '索引引擎当前离线'
  if (!aiIndexBreakdown.value.length) return '等待索引统计同步'
  return `覆盖 ${aiIndexTypeCount.value} 类项目知识索引`
})

const aiIndexStatusText = computed(() => {
  if (aiStats.value.engine_up === false) return '引擎离线'
  if (!aiIndexBreakdown.value.length) return '统计同步中'
  return 'RAG Ready'
})

const aiIndexStatusClass = computed(() => {
  if (aiStats.value.engine_up === false) {
    return 'bg-rose-50 text-rose-600 dark:bg-rose-950/40 dark:text-rose-300'
  }
  if (!aiIndexBreakdown.value.length) {
    return 'bg-amber-50 text-amber-600 dark:bg-amber-950/40 dark:text-amber-300'
  }
  return 'bg-purple-50 text-purple-600 dark:bg-purple-950/40 dark:text-purple-300'
})

const aiIndexStatusDotClass = computed(() => {
  if (aiStats.value.engine_up === false) return 'bg-rose-500'
  if (!aiIndexBreakdown.value.length) return 'bg-amber-500'
  return 'bg-purple-500'
})

const formatCount = (value: number) => Number(value || 0).toLocaleString()

const responseTimeDisplay = computed(() => (responseTime.value > 0 ? `${responseTime.value}ms` : '--'))

const aiEngineStateText = computed(() => {
  if (aiStats.value.engine_up === false) return '离线'
  if (!aiIndexBreakdown.value.length) return '同步中'
  return '在线'
})

const aiEngineStateClass = computed(() => {
  if (aiStats.value.engine_up === false) return 'text-rose-600 dark:text-rose-300'
  if (!aiIndexBreakdown.value.length) return 'text-amber-600 dark:text-amber-300'
  return 'text-emerald-600 dark:text-emerald-300'
})

const memoryUsagePercent = computed(() => {
  const total = Number(serverStats.value.mem?.total || 0)
  const used = Number(serverStats.value.mem?.used || 0)
  if (!total) return '--'
  return `${Math.round((used / total) * 100)}%`
})

const memoryUsageDetail = computed(() => {
  const total = Number(serverStats.value.mem?.total || 0)
  const used = Number(serverStats.value.mem?.used || 0)
  if (!total) return '等待服务端数据'
  return `${used.toFixed(1)}G / ${total.toFixed(1)}G`
})

const jvmUsagePercent = computed(() => {
  const total = Number(serverStats.value.jvm?.total || 0)
  const free = Number(serverStats.value.jvm?.free || 0)
  if (!total) return '--'
  return `${(((total - free) / total) * 100).toFixed(1)}%`
})

const jvmUsageDetail = computed(() => {
  const total = Number(serverStats.value.jvm?.total || 0)
  const free = Number(serverStats.value.jvm?.free || 0)
  if (!total) return '等待 JVM 指标'
  return `已用 ${(total - free).toFixed(1)}G / 总 ${(total).toFixed(1)}G`
})

const runtimeSummaryItems = computed(() => [
  {
    label: 'Memory',
    value: memoryUsagePercent.value,
    detail: memoryUsageDetail.value,
    state: serverStats.value.mem?.total ? '实时采样' : '等待数据',
    icon: 'memory',
    iconClass: 'text-blue-500',
    iconWrapClass: 'bg-blue-50 border-blue-100 dark:bg-blue-950/40 dark:border-blue-900/40',
    stateClass: serverStats.value.mem?.total ? 'text-blue-600 dark:text-blue-300' : 'text-slate-400 dark:text-slate-500'
  },
  {
    label: 'JVM',
    value: jvmUsagePercent.value,
    detail: jvmUsageDetail.value,
    state: serverStats.value.jvm?.total ? '实时采样' : '等待数据',
    icon: 'speed',
    iconClass: 'text-orange-500',
    iconWrapClass: 'bg-orange-50 border-orange-100 dark:bg-orange-950/40 dark:border-orange-900/40',
    stateClass: serverStats.value.jvm?.total ? 'text-orange-600 dark:text-orange-300' : 'text-slate-400 dark:text-slate-500'
  },
  {
    label: 'Uptime',
    value: uptime.value || '--',
    detail: responseTime.value > 0 ? `最近一次刷新耗时 ${responseTime.value}ms` : '等待首次轮询完成',
    state: uptime.value ? '持续运行' : '启动中',
    icon: 'public',
    iconClass: 'text-emerald-500',
    iconWrapClass: 'bg-emerald-50 border-emerald-100 dark:bg-emerald-950/40 dark:border-emerald-900/40',
    stateClass: uptime.value ? 'text-emerald-600 dark:text-emerald-300' : 'text-slate-400 dark:text-slate-500'
  }
])

const goToAiDashboard = () => {
  router.push('/ai/dashboard')
}

const goToKnowledgeDetail = () => {
  router.push({
    path: '/ai/dashboard',
    query: { focus: 'knowledge' }
  })
}

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
      const rawVectorLogs = vectorRes.data?.list || (vectorRes as any).rows || []
      vectorLogs.value = rawVectorLogs.filter((item: any) => isVectorIndexLog(item))
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

const isVectorIndexLog = (log: any) => {
  const title = String(log?.title || '')
  return title.startsWith('RAG 向量索引更新完成:')
}

const buildLogHeadline = (log: any) => {
  const operName = getDisplayOperName(log) || '系统'
  const action = getLogAction(log) || ''
  const title = String(log?.title || '').trim()
  return `${operName} ${action} ${title}`.replace(/\s+/g, ' ').trim()
}

const openOperLogDetail = (logId?: number) => {
  if (!logId) return
  router.push({
    path: '/monitor/operlog',
    query: { logId: String(logId) }
  })
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
      const nextStats = { ...res.data } as AiStats

      try {
        const engineRes = await fetch('http://localhost:8001/stats')
        if (engineRes.ok) {
          const engineStats = await engineRes.json()
          const engineKnowledge = Number(engineStats.knowledge_count || 0)
          if (engineKnowledge > 0) {
            nextStats.knowledge_count = Math.max(Number(nextStats.knowledge_count || 0), engineKnowledge)
            nextStats.total_chunks = Math.max(Number(nextStats.total_chunks || 0), engineKnowledge)
          }
        }
      } catch (error) {
        console.debug('Direct AI engine stats fetch failed:', error)
      }

      aiStats.value = nextStats
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

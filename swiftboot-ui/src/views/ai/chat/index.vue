<template>
  <div class="app-container relative h-full w-full bg-dashboard-bg overflow-hidden font-sans">
    <!-- 主聊天窗口 -->
    <div class="relative z-10 flex items-center justify-center h-full w-full p-4 md:p-8 lg:p-12">
      <div class="floating-chat-card flex flex-col w-full max-w-6xl h-full max-h-[850px] bg-white rounded-2xl border border-border-light overflow-hidden">
        <!-- Header -->
        <header class="h-16 flex-shrink-0 bg-white border-b border-border-light px-6 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-primary/5 rounded-xl flex items-center justify-center">
              <span class="material-symbols-outlined text-primary text-2xl">auto_awesome</span>
            </div>
            <div>
              <h2 class="text-[15px] font-bold text-slate-900 tracking-tight">SwiftBoot 智能会话</h2>
              <div class="flex items-center gap-1.5">
                <span class="relative flex h-2 w-2">
                  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                </span>
                <span class="text-[11px] text-slate-400 font-medium">本地 RAG 索引已就绪</span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <div class="hidden sm:flex items-center px-3 py-1.5 bg-slate-50 rounded-lg mr-2 border border-slate-100">
              <span class="text-[11px] font-medium text-slate-500">模型: SwiftLLM-V2</span>
            </div>
            <button class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">
              <span class="material-symbols-outlined text-[20px]">history</span>
            </button>
            <button class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">
              <span class="material-symbols-outlined text-[20px]">settings</span>
            </button>
          </div>
        </header>

        <div class="flex-1 flex overflow-hidden">
          <!-- Chat Area -->
          <div class="flex-1 flex flex-col h-full bg-white">
            <!-- Messages -->
            <div class="flex-1 overflow-y-auto custom-scrollbar p-6 space-y-6">
              <!-- User Message Example -->
              <div class="flex flex-col items-end">
                <div class="bg-white border border-border-light px-5 py-3 rounded-2xl rounded-tr-none max-w-[85%] shadow-sm">
                  <p class="text-[14px] leading-relaxed text-slate-700">
                    请帮我优化一下 `UserMapper.xml` 中的分页查询，目前的查询在大数据量下有些缓慢。
                  </p>
                </div>
                <span class="text-[10px] text-slate-400 mt-2 mr-1">14:20</span>
              </div>

              <!-- AI Message Example -->
              <div class="flex gap-4">
                <div class="w-8 h-8 flex-shrink-0 bg-primary/5 rounded-lg flex items-center justify-center text-primary border border-primary/10">
                  <span class="material-symbols-outlined text-sm">auto_awesome</span>
                </div>
                <div class="flex-1 space-y-4">
                  <div class="bg-chat-ai border border-border-light p-6 rounded-2xl rounded-tl-none">
                    <div class="flex items-center gap-2 mb-4 text-primary/70">
                      <div class="flex gap-1">
                        <div class="w-1 h-1 bg-primary rounded-full"></div>
                        <div class="w-1 h-1 bg-primary rounded-full"></div>
                        <div class="w-1 h-1 bg-primary rounded-full"></div>
                      </div>
                      <span class="text-[11px] font-semibold tracking-wider">已分析本地项目结构</span>
                    </div>
                    <div class="prose prose-sm max-w-none text-slate-600">
                      <p class="mb-3 text-[14px]">针对您的 `UserMapper.xml` 查询，建议采用<b>延迟关联 (Deferred Join)</b> 技术。通过减少回表次数来提升深分页性能：</p>
                      <div class="bg-slate-900 rounded-lg p-4 my-4 overflow-x-auto relative">
                        <div class="flex justify-between items-center mb-2 text-[10px] text-slate-500 font-mono">
                          <span>XML - UserMapper.xml</span>
                          <span class="cursor-pointer hover:text-white uppercase">复制代码</span>
                        </div>
                        <pre class="text-[13px] text-slate-300 font-mono leading-relaxed"><code>&lt;select id="selectPage" resultMap="BaseResultMap"&gt;
    SELECT u.* FROM sys_user u
    JOIN (SELECT id FROM sys_user ORDER BY create_time DESC LIMIT #{offset}, #{limit}) tmp 
    ON u.id = tmp.id
&lt;/select&gt;</code></pre>
                      </div>
                    </div>
                    <div class="mt-6 pt-4 border-t border-slate-200/60">
                      <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-3">知识库参考来源</p>
                      <div class="flex flex-wrap gap-2">
                        <button class="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-border-light rounded-full text-[12px] text-slate-600 hover:border-primary/30 hover:bg-primary/5 transition-all group">
                          <span class="material-symbols-outlined text-[16px] text-slate-400 group-hover:text-primary">description</span>
                          <span class="font-medium">UserMapper.xml</span>
                        </button>
                        <button class="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-border-light rounded-full text-[12px] text-slate-600 hover:border-primary/30 hover:bg-primary/5 transition-all group">
                          <span class="material-symbols-outlined text-[16px] text-slate-400 group-hover:text-primary">settings_applications</span>
                          <span class="font-medium">mybatis-config.xml</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Input Area -->
            <div class="p-6 pt-0 bg-white">
              <div class="relative bg-white border border-border-light rounded-xl shadow-sm focus-within:ring-2 focus-within:ring-primary/10 focus-within:border-primary/30 transition-all p-2">
                <textarea class="w-full bg-transparent border-none focus:ring-0 resize-none py-2 px-3 text-[14px] text-slate-700 placeholder:text-slate-400 focus:outline-none" placeholder="询问关于您项目的问题..." rows="2"></textarea>
                <div class="flex items-center justify-between px-2 pb-1">
                  <div class="flex items-center gap-1">
                    <button class="p-2 text-slate-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-all" title="上传代码">
                      <span class="material-symbols-outlined text-[20px]">attach_file</span>
                    </button>
                    <button class="p-2 text-slate-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-all" title="代码片段">
                      <span class="material-symbols-outlined text-[20px]">code</span>
                    </button>
                    <button class="p-2 text-slate-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-all" title="表情">
                      <span class="material-symbols-outlined text-[20px]">mood</span>
                    </button>
                  </div>
                  <button class="bg-primary text-white p-2 rounded-lg flex items-center justify-center px-5 hover:shadow-lg hover:shadow-primary/20 transition-all active:scale-95 group">
                    <span class="text-sm font-semibold mr-1.5">发送</span>
                    <span class="material-symbols-outlined text-[18px] group-hover:translate-x-0.5 transition-transform">send</span>
                  </button>
                </div>
              </div>
              <p class="text-center text-[10px] text-slate-400 mt-4 tracking-wide font-medium">
                SWIFTBOOT 智能全栈引擎 · RAG 增强型 AI
              </p>
            </div>
          </div>

          <!-- Sidebar (Right) -->
          <aside class="w-80 bg-slate-panel border-l border-border-light flex flex-col hidden lg:flex shadow-[inset_1px_0_0_0_rgba(0,0,0,0.02)]">
            <div class="p-5 border-b border-border-light bg-white/50 backdrop-blur-sm sticky top-0 z-10">
              <h3 class="text-[12px] font-bold text-slate-900 uppercase tracking-widest flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-[18px]">account_tree</span>
                当前知识环境
              </h3>
            </div>
            <div class="flex-1 overflow-y-auto custom-scrollbar p-5 space-y-8">
              <section>
                <div class="flex items-center justify-between mb-4">
                  <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">已索引文件 (1,248)</p>
                  <span class="text-[10px] font-mono text-primary bg-primary/5 px-1.5 py-0.5 rounded">98.2% 覆盖</span>
                </div>
                <div class="space-y-1 ml-2">
                  <div class="flex items-center gap-2 text-[13px] text-slate-700 py-1.5 px-2 hover:bg-white hover:shadow-sm rounded-lg transition-all cursor-pointer group">
                    <span class="material-symbols-outlined text-[18px] text-amber-400">folder_open</span>
                    <span class="font-medium">src/main/java</span>
                  </div>
                  <div class="ml-6 space-y-1">
                    <div class="tree-line flex items-center gap-2 text-[12px] text-slate-500 py-1.5 px-2 hover:text-slate-900 hover:bg-white rounded-lg transition-all cursor-pointer">
                      <span class="material-symbols-outlined text-[16px] text-blue-400">description</span>
                      <span class="truncate">UserController.java</span>
                    </div>
                    <div class="tree-line flex items-center gap-2 text-[12px] text-slate-500 py-1.5 px-2 hover:text-slate-900 hover:bg-white rounded-lg transition-all cursor-pointer">
                      <span class="material-symbols-outlined text-[16px] text-blue-400">description</span>
                      <span class="truncate">UserServiceImpl.java</span>
                    </div>
                  </div>
                  <div class="flex items-center gap-2 text-[13px] text-slate-700 py-1.5 px-2 hover:bg-white hover:shadow-sm rounded-lg transition-all cursor-pointer group mt-1">
                    <span class="material-symbols-outlined text-[18px] text-amber-400">folder</span>
                    <span class="font-medium">resources/mapper</span>
                  </div>
                  <div class="flex items-center gap-2 text-[13px] text-slate-700 py-1.5 px-2 hover:bg-white hover:shadow-sm rounded-lg transition-all cursor-pointer mt-1">
                    <span class="material-symbols-outlined text-[18px] text-slate-400">settings</span>
                    <span class="font-medium">pom.xml</span>
                  </div>
                </div>
              </section>
              <section>
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-4">RAG 向量空间</p>
                <div class="relative h-44 w-full bg-white rounded-xl border border-border-light shadow-sm overflow-hidden group">
                  <div class="absolute inset-0 opacity-[0.05]" style="background-image: linear-gradient(#2b2bee 1px, transparent 1px), linear-gradient(90deg, #2b2bee 1px, transparent 1px); background-size: 20px 20px;"></div>
                  <div class="vector-dot top-10 left-12"></div>
                  <div class="vector-dot top-24 left-8"></div>
                  <div class="vector-dot top-16 left-32"></div>
                  <div class="vector-dot top-32 left-44"></div>
                  <div class="vector-dot top-8 left-56"></div>
                  <div class="vector-dot top-20 left-20 bg-primary shadow-[0_0_8px_rgba(43,43,238,0.6)]"></div>
                  <div class="vector-dot top-28 left-36"></div>
                  <div class="vector-dot top-12 left-48"></div>
                  <div class="absolute inset-x-0 h-[2px] bg-primary/20 top-1/2 -translate-y-1/2 shadow-[0_0_10px_rgba(43,43,238,0.3)]"></div>
                  <div class="absolute bottom-3 left-3 flex items-center gap-1.5">
                    <div class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></div>
                    <span class="text-[9px] font-bold text-slate-400 uppercase">实时检索中...</span>
                  </div>
                  <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-white/40 backdrop-blur-[1px]">
                    <button class="text-[11px] font-semibold text-primary bg-white border border-primary/20 px-3 py-1 rounded-full shadow-sm">查看映射详情</button>
                  </div>
                </div>
                <div class="mt-3 flex justify-between text-[10px] text-slate-400 font-medium">
                  <span>维度: 1536 (OpenAI)</span>
                  <span>相似度阈值: 0.82</span>
                </div>
              </section>
            </div>
            <div class="p-5 bg-white border-t border-border-light">
              <button class="w-full flex items-center justify-center gap-2 py-2.5 text-[12px] font-bold bg-white border border-slate-200 text-slate-600 rounded-xl hover:border-primary/40 hover:text-primary hover:bg-primary/[0.02] transition-all shadow-sm active:scale-[0.98]">
                <span class="material-symbols-outlined text-[18px]">refresh</span>
                更新索引
              </button>
            </div>
          </aside>
        </div>
      </div>
    </div>

    <!-- Floating Action Button -->
    <button class="fixed bottom-8 right-8 w-14 h-14 neural-glow rounded-full flex items-center justify-center text-white z-50 hover:scale-110 active:scale-95 transition-all shadow-xl group">
      <span class="material-symbols-outlined text-[30px] group-hover:rotate-12 transition-transform">auto_awesome</span>
    </button>
  </div>
</template>

<script setup lang="ts">
// 逻辑部分后续添加，目前仅还原 UI
</script>

<style scoped>
/* 自定义颜色类 (模拟 Tailwind 配置) */
.bg-primary { background-color: #2b2bee; }
.bg-primary\/5 { background-color: rgba(43, 43, 238, 0.05); }
.bg-primary\/10 { background-color: rgba(43, 43, 238, 0.1); }
.bg-primary\/20 { background-color: rgba(43, 43, 238, 0.2); }
.bg-primary\/40 { background-color: rgba(43, 43, 238, 0.4); }
.bg-primary\/\[0\.02\] { background-color: rgba(43, 43, 238, 0.02); }

.text-primary { color: #2b2bee; }
.text-primary\/70 { color: rgba(43, 43, 238, 0.7); }

.border-primary\/10 { border-color: rgba(43, 43, 238, 0.1); }
.border-primary\/20 { border-color: rgba(43, 43, 238, 0.2); }
.border-primary\/30 { border-color: rgba(43, 43, 238, 0.3); }
.border-primary\/40 { border-color: rgba(43, 43, 238, 0.4); }

.bg-dashboard-bg { background-color: #f6f6f8; }
.bg-chat-ai { background-color: #f8fafc; }
.bg-slate-panel { background-color: #fcfdfe; }

.border-border-light { border-color: #f0f2f5; }

.hover\:bg-primary\/5:hover { background-color: rgba(43, 43, 238, 0.05); }
.hover\:bg-primary\/\[0\.02\]:hover { background-color: rgba(43, 43, 238, 0.02); }
.hover\:text-primary:hover { color: #2b2bee; }
.hover\:border-primary\/30:hover { border-color: rgba(43, 43, 238, 0.3); }
.hover\:border-primary\/40:hover { border-color: rgba(43, 43, 238, 0.4); }
.hover\:shadow-primary\/20:hover { --un-shadow-color: rgba(43, 43, 238, 0.2); }
.group:hover .group-hover\:text-primary { color: #2b2bee; }


/* 自定义样式 */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }

.floating-chat-card {
    box-shadow: 0 20px 50px -12px rgba(0, 0, 0, 0.08);
}

.neural-glow {
    box-shadow: 0 0 15px rgba(43, 43, 238, 0.4);
    background: radial-gradient(circle at center, #4f46e5 0%, #2b2bee 100%);
}

.vector-dot {
    position: absolute;
    width: 0.25rem;
    height: 0.25rem;
    border-radius: 9999px;
    background-color: rgba(43, 43, 238, 0.4);
}

.tree-line {
    position: relative;
}
.tree-line::before {
    content: '';
    position: absolute;
    left: -12px;
    top: -10px;
    bottom: 10px;
    width: 1px;
    background: #e2e8f0;
}
.tree-line::after {
    content: '';
    position: absolute;
    left: -12px;
    top: 10px;
    width: 8px;
    height: 1px;
    background: #e2e8f0;
}

/* 覆盖 UnoCSS 可能未覆盖的特定样式 */
.app-container {
  height: calc(100vh - 84px); /* 保持原有高度设置以适应 Layout */
}
</style>
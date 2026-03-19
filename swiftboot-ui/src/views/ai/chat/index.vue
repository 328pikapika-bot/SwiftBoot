<template>
  <div class="min-h-full bg-slate-50 dark:bg-[#0f172a] px-6 py-10 md:px-10">
    <div class="mx-auto max-w-5xl">
      <div class="relative overflow-hidden rounded-[32px] border border-slate-200/80 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 shadow-xl shadow-slate-200/30 dark:shadow-slate-950/30">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.14),_transparent_34%),radial-gradient(circle_at_bottom_right,_rgba(99,102,241,0.14),_transparent_40%)] pointer-events-none"></div>
        <div class="relative z-10 px-8 py-10 md:px-12 md:py-14">
          <div class="inline-flex items-center gap-2 rounded-full border border-blue-200/70 dark:border-blue-900/50 bg-blue-50/80 dark:bg-blue-950/30 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-blue-600 dark:text-blue-300">
            <span class="w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse"></span>
            Smart Chat
          </div>

          <div class="mt-6 max-w-3xl">
            <h1 class="text-3xl md:text-5xl font-black tracking-tight text-slate-900 dark:text-white">
              智能会话窗口已准备就绪
            </h1>
            <p class="mt-4 text-base md:text-lg leading-relaxed text-slate-600 dark:text-slate-300">
              进入本页面后会自动拉起右下角的真实智能会话窗口。如果窗口没有自动展开，可以点击下方按钮再次打开。
            </p>
          </div>

          <div class="mt-8 flex flex-wrap items-center gap-3">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl bg-blue-600 px-5 py-3 text-sm font-bold text-white shadow-lg shadow-blue-500/25 transition-all hover:-translate-y-0.5 hover:bg-blue-700"
              @click="openWindow"
            >
              <span class="material-icons-round text-[18px]">open_in_new</span>
              打开智能会话
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 dark:border-slate-700 bg-white/80 dark:bg-slate-950/40 px-5 py-3 text-sm font-semibold text-slate-700 dark:text-slate-200 transition-all hover:border-blue-300 dark:hover:border-blue-700 hover:text-blue-600 dark:hover:text-blue-300"
              @click="scrollHint"
            >
              <span class="material-icons-round text-[18px]">south</span>
              查看使用说明
            </button>
          </div>

          <div id="chat-page-guide" class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="rounded-2xl border border-slate-200/80 dark:border-slate-700/70 bg-slate-50/80 dark:bg-slate-950/35 p-5">
              <div class="text-[11px] uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">Step 1</div>
              <div class="mt-2 text-lg font-bold text-slate-900 dark:text-white">自动拉起窗口</div>
              <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">路由进入后自动触发真实的会话窗口展开，而不是展示静态假页面。</p>
            </div>
            <div class="rounded-2xl border border-slate-200/80 dark:border-slate-700/70 bg-slate-50/80 dark:bg-slate-950/35 p-5">
              <div class="text-[11px] uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">Step 2</div>
              <div class="mt-2 text-lg font-bold text-slate-900 dark:text-white">直接开始提问</div>
              <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">窗口展开后即可直接查看历史记录、继续对话，或重新发起项目相关提问。</p>
            </div>
            <div class="rounded-2xl border border-slate-200/80 dark:border-slate-700/70 bg-slate-50/80 dark:bg-slate-950/35 p-5">
              <div class="text-[11px] uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">Step 3</div>
              <div class="mt-2 text-lg font-bold text-slate-900 dark:text-white">索引异常可重建</div>
              <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">如果遇到上下文缺失或索引滞后，可在窗口内继续使用“更新索引”能力进行重建。</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted } from 'vue'

const openWindow = () => {
  window.dispatchEvent(new CustomEvent('swiftboot-open-ai-assistant'))
}

const scrollHint = async () => {
  await nextTick()
  document.getElementById('chat-page-guide')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  openWindow()
})
</script>

<template>
  <div class="bg-[#fcfcfd] dark:bg-[#0f172a] text-slate-900 dark:text-slate-100 min-h-screen transition-colors duration-300 overflow-x-hidden font-display">
    <nav class="fixed top-0 w-full z-50 flex justify-end items-center px-8 py-6 space-x-6">
      <button 
        class="w-10 h-10 rounded-full bg-white/50 dark:bg-slate-800/50 hover:bg-white dark:hover:bg-slate-800 backdrop-blur-sm border border-slate-200/50 dark:border-slate-700/50 shadow-sm flex items-center justify-center transition-all text-slate-600 dark:text-slate-300"
        id="theme-toggle"
        @click="toggleTheme"
        title="切换主题"
      >
        <!-- Sun Icon -->
        <svg v-if="!isDark" xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        <!-- Moon Icon -->
        <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
        </svg>
      </button>
      <el-dropdown trigger="click" @command="handleLanguageChange">
        <button class="h-10 px-4 rounded-full bg-white/50 dark:bg-slate-800/50 hover:bg-white dark:hover:bg-slate-800 backdrop-blur-sm border border-slate-200/50 dark:border-slate-700/50 shadow-sm flex items-center gap-2 transition-all text-slate-600 dark:text-slate-300">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
          </svg>
          <span class="text-sm font-medium">{{ locale === 'zh-cn' ? '中/EN' : 'En/中' }}</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="zh-cn" :disabled="locale === 'zh-cn'">中文</el-dropdown-item>
            <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>
    <main class="min-h-screen flex items-center justify-center px-4 py-20 lg:py-0">
      <div class="max-w-6xl w-full grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-24 items-center">
        <div class="space-y-10">
          <div class="space-y-6">
            <div class="inline-flex items-center space-x-3 bg-[#10b981]/10 text-[#10b981] px-4 py-2 rounded-full">
              <span class="material-icons-outlined text-[18px]">bolt</span>
              <span class="text-sm font-semibold tracking-wide">VERSION 1.0 现已发布</span>
            </div>
            <div class="space-y-4">
              <h1 class="text-5xl lg:text-7xl font-extrabold tracking-tight">
                Swift<span class="text-[#10b981]">Boot</span>
              </h1>
              <div class="flex flex-wrap gap-2 pt-1">
                <span class="px-3 py-1 bg-slate-100/80 dark:bg-slate-800/80 text-slate-500 dark:text-slate-400 text-[11px] font-medium rounded-full border border-slate-200/50 dark:border-slate-700/50 flex items-center gap-1">
                  <span class="w-1 h-1 rounded-full bg-[#10b981]/60"></span>
                  高性能
                </span>
                <span class="px-3 py-1 bg-slate-100/80 dark:bg-slate-800/80 text-slate-500 dark:text-slate-400 text-[11px] font-medium rounded-full border border-slate-200/50 dark:border-slate-700/50 flex items-center gap-1">
                  <span class="w-1 h-1 rounded-full bg-[#10b981]/60"></span>
                  极简架构
                </span>
                <span class="px-3 py-1 bg-slate-100/80 dark:bg-slate-800/80 text-slate-500 dark:text-slate-400 text-[11px] font-medium rounded-full border border-slate-200/50 dark:border-slate-700/50 flex items-center gap-1">
                  <span class="w-1 h-1 rounded-full bg-[#10b981]/60"></span>
                  AI 原生
                </span>
              </div>
            </div>
            <p class="text-xl text-slate-500 dark:text-slate-400 leading-relaxed max-w-lg">
              专为开发者打造的轻量级全栈框架。融合企业级性能与极致开发体验。
            </p>
            <div class="space-y-6">
              <div class="flex items-start space-x-4">
                <div class="mt-1 bg-white dark:bg-slate-800 shadow-sm p-2 rounded-lg text-[#10b981] border border-slate-100 dark:border-slate-700">
                  <span class="material-icons-outlined">layers</span>
                </div>
                <div>
                  <h3 class="font-semibold text-lg text-slate-800 dark:text-slate-200">Spring Boot 3 + Vue 3</h3>
                  <p class="text-sm text-slate-500 dark:text-slate-400">前后端深度优化，开箱即用的工程模板与极致构建速度。</p>
                </div>
              </div>
              <div class="flex items-start space-x-4">
                <div class="mt-1 bg-white dark:bg-slate-800 shadow-sm p-2 rounded-lg text-[#10b981] border border-slate-100 dark:border-slate-700">
                  <span class="material-icons-outlined">psychology</span>
                </div>
                <div>
                  <h3 class="font-semibold text-lg text-slate-800 dark:text-slate-200">内置本地 RAG 自认知</h3>
                  <p class="text-sm text-slate-500 dark:text-slate-400">私有化部署的 AI 智能，赋予应用深度的知识检索与逻辑理解能力。</p>
                </div>
              </div>
            </div>
          </div>
          <div class="pt-2 space-y-4">
            <p class="text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-[0.2em] opacity-80">前往开源仓库</p>
            <div class="flex items-center space-x-5">
              <a class="group transition-all hover:scale-110" href="#" title="Gitee">
            <div class="w-10 h-10 rounded-full bg-[#c71d23] flex items-center justify-center shadow-lg shadow-[#c71d23]/20 border border-white/10">
              <svg class="w-5 h-5 fill-white" viewBox="0 0 1024 1024">
                <path d="M512 1024C230.4 1024 0 793.6 0 512S230.4 0 512 0s512 230.4 512 512-230.4 512-512 512z m259.2-569.6H480c-12.8 0-25.6 12.8-25.6 25.6v64c0 12.8 12.8 25.6 25.6 25.6h176c12.8 0 25.6 12.8 25.6 25.6v12.8c0 41.6-35.2 76.8-76.8 76.8h-240c-12.8 0-25.6-12.8-25.6-25.6V416c0-12.8 12.8-25.6 25.6-25.6h448c12.8 0 25.6-12.8 25.6-25.6V300.8c0-12.8-12.8-25.6-25.6-25.6H409.6c-89.6 0-160 70.4-160 160v256c0 89.6 70.4 160 160 160h256c89.6 0 160-70.4 160-160V480c0-12.8-12.8-25.6-25.6-25.6z"></path>
              </svg>
            </div>
          </a>
              <a class="group transition-all hover:scale-110" href="#" title="GitHub">
                <div class="w-10 h-10 rounded-full bg-[#333333] flex items-center justify-center shadow-lg shadow-black/20 border border-white/10">
                  <svg class="w-5 h-5 fill-white" viewBox="0 0 24 24">
                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.041-1.416-4.041-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"></path>
                  </svg>
                </div>
              </a>
            </div>
          </div>
        </div>
        <div class="flex justify-center lg:justify-end">
          <div class="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-8 shadow-xl shadow-slate-200/50 dark:shadow-black/50 transition-all">
            <div class="mb-10">
              <h2 class="text-2xl font-bold text-slate-900 dark:text-white mb-2">欢迎回来</h2>
              <p class="text-slate-500 dark:text-slate-400 text-sm">请输入您的账号密码以登录 SwiftBoot 控制台</p>
            </div>
            <form class="space-y-6" @submit.prevent="handleLogin">
              <div class="space-y-2">
                <label class="text-xs font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest px-1">用户名</label>
                <div class="relative group">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-[#10b981] transition-colors flex items-center">
                    <el-icon :size="18"><User /></el-icon>
                  </span>
                  <input 
                    v-model="loginForm.username"
                    class="w-full pl-12 pr-4 py-3.5 bg-slate-50 dark:bg-slate-800/50 border-slate-200 dark:border-slate-700 rounded-xl focus:ring-[#10b981] focus:border-[#10b981] dark:focus:ring-[#10b981] dark:focus:border-[#10b981] transition-all text-sm outline-none" 
                    placeholder="admin / email" 
                    type="text"
                  />
                </div>
              </div>
              <div class="space-y-2">
                <div class="flex justify-between items-center px-1">
                  <label class="text-xs font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest">密码</label>
                </div>
                <div class="relative group">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-[#10b981] transition-colors flex items-center">
                    <el-icon :size="18"><Lock /></el-icon>
                  </span>
                  <input 
                    v-model="loginForm.password"
                    class="w-full pl-12 pr-12 py-3.5 bg-slate-50 dark:bg-slate-800/50 border-slate-200 dark:border-slate-700 rounded-xl focus:ring-[#10b981] focus:border-[#10b981] dark:focus:ring-[#10b981] dark:focus:border-[#10b981] transition-all text-sm outline-none" 
                    placeholder="••••••••" 
                    :type="showPassword ? 'text' : 'password'"
                  />
                  <button 
                    class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors flex items-center" 
                    type="button"
                    @click="showPassword = !showPassword"
                  >
                    <el-icon :size="18">
                      <Hide v-if="showPassword" />
                      <View v-else />
                    </el-icon>
                  </button>
                </div>
              </div>
              <div class="flex items-center space-x-2 px-1">
                <input 
                  v-model="rememberMe"
                  class="w-4 h-4 text-[#10b981] bg-slate-50 border-slate-300 rounded focus:ring-[#10b981] dark:focus:ring-offset-slate-900" 
                  id="remember" 
                  type="checkbox"
                />
                <label class="text-sm text-slate-600 dark:text-slate-400 cursor-pointer" for="remember">记住我</label>
              </div>
              <button 
                class="w-full bg-gradient-to-r from-emerald-500 to-[#10b981] hover:from-emerald-400 hover:to-emerald-500 text-white font-bold py-4 rounded-xl shadow-xl shadow-emerald-500/20 hover:shadow-emerald-500/40 hover:-translate-y-1 transform active:scale-[0.98] transition-all duration-300 flex items-center justify-center gap-2" 
                type="submit"
                :disabled="loading"
              >
                <span class="text-base tracking-wide">{{ loading ? '登录中...' : '立即登录' }}</span>
                <span class="material-icons-round text-[20px] animate-pulse" v-if="!loading">arrow_forward</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </main>
    <footer class="fixed bottom-0 w-full p-8 hidden lg:flex justify-between items-end pointer-events-none">
      <div class="text-[10px] font-mono uppercase tracking-[0.2em] text-slate-400 opacity-50">
        Powered by SwiftBoot Core Engine
      </div>
      <div class="flex space-x-4 opacity-50">
        <div class="w-1 h-1 bg-slate-300 dark:bg-slate-700 rounded-full"></div>
        <div class="w-1 h-1 bg-slate-300 dark:bg-slate-700 rounded-full"></div>
        <div class="w-1 h-1 bg-slate-300 dark:bg-slate-700 rounded-full"></div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t, locale } = useI18n()

const loading = ref(false)
const rememberMe = ref(false)
const showPassword = ref(false)
const isDark = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const handleLanguageChange = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  const htmlElement = document.documentElement
  if (isDark.value) {
    htmlElement.classList.add('dark')
    localStorage.theme = 'dark'
  } else {
    htmlElement.classList.remove('dark')
    localStorage.theme = 'light'
  }
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning(t('login.usernameRequired') || '请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success(t('login.success') || '登录成功')
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: any) {
    ElMessage.error(error.message || t('login.failed') || '登录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  const savedPassword = localStorage.getItem('rememberedPassword')
  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword
    rememberMe.value = true
  }
  
  // Initialize theme
  if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    isDark.value = true
    document.documentElement.classList.add('dark')
  } else {
    isDark.value = false
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style>
/* Font settings matching the HTML */
.font-display {
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>
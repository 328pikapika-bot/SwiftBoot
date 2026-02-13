<template>
  <div class="bg-slate-50 dark:bg-[#0f172a] text-slate-900 dark:text-slate-100 min-h-screen flex font-display">
    <!-- Sidebar -->
    <aside 
      class="border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hidden lg:flex flex-col sticky top-0 h-screen transition-all duration-300 ease-in-out"
      :class="appStore.sidebar.opened ? 'w-48' : 'w-20'"
    >
      <div class="h-16 flex items-center justify-center border-b border-slate-100 dark:border-slate-800 transition-all duration-300">
        <div class="flex items-center gap-3 overflow-hidden whitespace-nowrap" :class="{ 'px-6': appStore.sidebar.opened, 'px-0': !appStore.sidebar.opened }">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center text-white font-bold shrink-0 shadow-lg shadow-blue-500/30">S</div>
          <span class="text-lg font-bold tracking-tight text-slate-800 dark:text-white transition-opacity duration-300" :class="appStore.sidebar.opened ? 'opacity-100 w-auto' : 'opacity-0 w-0 hidden'">
            SwiftBoot
          </span>
        </div>
      </div>
      
      <nav class="flex-1 px-3 space-y-1 mt-4 overflow-y-auto sidebar-scroll custom-scrollbar">
        <!-- Dashboard Link (Static) -->
        <router-link 
          to="/dashboard" 
          class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
          :class="route.path === '/dashboard' 
            ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 font-medium shadow-sm' 
            : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-200'"
        >
          <div class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-blue-500 rounded-r-full transition-all duration-200" 
               :class="route.path === '/dashboard' ? 'opacity-100' : 'opacity-0 -translate-x-full'"></div>
          
          <span class="material-icons-round text-[22px] transition-transform group-hover:scale-110" :class="route.path === '/dashboard' ? 'text-blue-500' : 'text-slate-400 dark:text-slate-500'">dashboard</span>
          
          <span class="whitespace-nowrap transition-all duration-300" 
                :class="appStore.sidebar.opened ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-4 absolute pointer-events-none'">
            首页控制台
          </span>
          
          <!-- Tooltip for collapsed state -->
          <div v-if="!appStore.sidebar.opened" 
               class="absolute left-full ml-4 px-2 py-1 bg-slate-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50">
            首页控制台
          </div>
        </router-link>

        <div class="my-4 border-t border-slate-100 dark:border-slate-800 mx-2"></div>

        <!-- Dynamic Menu -->
        <template v-for="item in menuList" :key="item.path">
          <!-- Group Title / Submenu -->
          <div v-if="item.children && item.children.length > 0 && item.path !== '/dashboard'" class="mb-2">
            <!-- Clickable Header (Expanded) -->
            <div 
              v-if="appStore.sidebar.opened"
              @click="toggleMenu(item.path)"
              class="flex items-center justify-between px-3 py-2.5 mx-2 mb-1 cursor-pointer rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors group select-none"
            >
              <div class="flex items-center gap-2.5 text-slate-600 dark:text-slate-400 group-hover:text-slate-900 dark:group-hover:text-slate-200 transition-colors">
                 <div class="w-[20px] h-[20px] flex items-center justify-center">
                   <el-icon v-if="isElementIcon(item.meta?.icon)" :size="18">
                     <component :is="getElementIconName(item.meta?.icon)" />
                   </el-icon>
                   <span v-else class="material-icons-round text-[20px]">{{ getMaterialIcon(item.meta?.icon) }}</span>
                 </div>
                 <span class="text-sm font-medium">{{ item.meta?.title }}</span>
              </div>
              <span class="material-icons-round text-slate-400 text-lg transition-transform duration-300" :class="{ '-rotate-90': !openMenus.includes(item.path) }">expand_more</span>
            </div>

            <div v-show="appStore.sidebar.opened ? openMenus.includes(item.path) : true" class="space-y-1 mt-1 bg-slate-50/80 dark:bg-slate-800/40 rounded-xl py-1 mx-1">
              <router-link
                v-for="child in item.children"
                :key="child.path"
                :to="resolvePath(item.path, child.path)"
                class="flex items-center gap-3 px-3 py-2 rounded-lg text-[13px] transition-all duration-200 group relative mx-1"
                :class="route.path === resolvePath(item.path, child.path) 
                  ? 'bg-white dark:bg-slate-800 text-blue-600 dark:text-blue-400 font-medium shadow-sm ring-1 ring-slate-200 dark:ring-slate-700' 
                  : 'text-slate-500 dark:text-slate-400 hover:bg-white/50 dark:hover:bg-slate-700/50 hover:text-slate-900 dark:hover:text-slate-200'"
              >
                <div class="w-[18px] h-[18px] flex items-center justify-center transition-transform group-hover:scale-110"
                      :class="[
                        route.path === resolvePath(item.path, child.path) ? 'text-blue-500' : 'text-slate-400 dark:text-slate-500',
                        appStore.sidebar.opened ? '' : ''
                      ]">
                  <el-icon v-if="isElementIcon(child.meta?.icon)" :size="18">
                    <component :is="getElementIconName(child.meta?.icon)" />
                  </el-icon>
                  <span v-else class="material-icons-round text-[20px]">{{ getMaterialIcon(child.meta?.icon) }}</span>
                </div>
                
                <span class="whitespace-nowrap transition-all duration-300"
                      :class="appStore.sidebar.opened ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-4 absolute pointer-events-none'">
                  {{ child.meta?.title }}
                </span>

                <!-- Tooltip for collapsed state -->
                <div v-if="!appStore.sidebar.opened" 
                     class="absolute left-full ml-4 px-2 py-1 bg-slate-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50 shadow-xl">
                  {{ child.meta?.title }}
                </div>
              </router-link>
            </div>
          </div>
          
          <!-- Single Menu Item (Root level) -->
          <router-link 
            v-else-if="item.path !== '/dashboard'"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
            :class="route.path === item.path 
              ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 font-medium shadow-sm' 
              : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-200'"
          >
            <div class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-blue-500 rounded-r-full transition-all duration-200" 
                 :class="route.path === item.path ? 'opacity-100' : 'opacity-0 -translate-x-full'"></div>
            
            <div class="w-[22px] h-[22px] flex items-center justify-center transition-transform group-hover:scale-110" 
                  :class="route.path === item.path ? 'text-blue-500' : 'text-slate-400 dark:text-slate-500'">
              <el-icon v-if="isElementIcon(item.meta?.icon)" :size="20">
                <component :is="getElementIconName(item.meta?.icon)" />
              </el-icon>
              <span v-else class="material-icons-round text-[22px]">{{ getMaterialIcon(item.meta?.icon) }}</span>
            </div>
            
            <span class="whitespace-nowrap transition-all duration-300" 
                  :class="appStore.sidebar.opened ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-4 absolute pointer-events-none'">
              {{ item.meta?.title }}
            </span>
            
            <!-- Tooltip -->
            <div v-if="!appStore.sidebar.opened" 
                 class="absolute left-full ml-4 px-2 py-1 bg-slate-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50 shadow-xl">
              {{ item.meta?.title }}
            </div>
          </router-link>
        </template>
      </nav>

      <div class="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50" v-show="appStore.sidebar.opened">
        <div class="bg-white dark:bg-slate-800 p-3 rounded-xl border border-slate-100 dark:border-slate-700 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
              </span>
              <span class="text-xs font-semibold text-slate-700 dark:text-slate-300">RAG 引擎</span>
            </div>
            <span class="text-[10px] px-1.5 py-0.5 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-md font-medium">在线</span>
          </div>
          <div class="text-[10px] text-slate-400 dark:text-slate-500 flex justify-between items-center">
            <span>索引文档</span>
            <span class="font-mono">12,402</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 flex flex-col min-w-0 bg-[#f8fafc] dark:bg-[#0f172a]">
      <!-- Header -->
      <header class="h-16 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 flex items-center justify-between px-8 z-50 sticky top-0">
        <div class="flex items-center gap-4 text-sm text-slate-500 dark:text-slate-400">
          <button 
            @click="appStore.toggleSidebar" 
            class="w-9 h-9 flex items-center justify-center rounded-xl bg-white/50 dark:bg-slate-800/50 hover:bg-white dark:hover:bg-slate-800 backdrop-blur-sm border border-slate-200/50 dark:border-slate-700/50 shadow-sm text-slate-500 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 hover:shadow-md hover:border-blue-200 dark:hover:border-blue-800 transition-all duration-300 group"
            :title="appStore.sidebar.opened ? '收起侧边栏' : '展开侧边栏'"
          >
            <svg class="w-5 h-5 transition-transform duration-500" :class="{ 'rotate-180': !appStore.sidebar.opened }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 6h11M9 12h11M9 18h11M5 12l4-4M5 12l4 4" class="group-hover:stroke-blue-500 transition-colors" />
            </svg>
          </button>
          
          <div class="h-4 w-px bg-slate-200 dark:bg-slate-700 mx-1"></div>
          
          <div class="flex items-center gap-2 text-slate-600 dark:text-slate-300">
            <span class="material-icons-round text-lg opacity-80">home</span>
            <span class="material-icons-round text-xs opacity-50">chevron_right</span>
            <span class="font-medium">{{ route.meta.title || '首页控制台' }}</span>
          </div>
        </div>

        <div class="flex items-center gap-5">
          <a class="group transition-all hover:scale-110" href="https://gitee.com/cs_shuang/SwiftBoot" target="_blank" title="Gitee">
            <div class="w-9 h-9 rounded-full bg-[#c71d23] flex items-center justify-center shadow-lg shadow-[#c71d23]/20 border border-white/10">
              <svg class="w-5 h-5 fill-white" viewBox="0 0 1024 1024">
                <path d="M512 1024C230.4 1024 0 793.6 0 512S230.4 0 512 0s512 230.4 512 512-230.4 512-512 512z m259.2-569.6H480c-12.8 0-25.6 12.8-25.6 25.6v64c0 12.8 12.8 25.6 25.6 25.6h176c12.8 0 25.6 12.8 25.6 25.6v12.8c0 41.6-35.2 76.8-76.8 76.8h-240c-12.8 0-25.6-12.8-25.6-25.6V416c0-12.8 12.8-25.6 25.6-25.6h448c12.8 0 25.6-12.8 25.6-25.6V300.8c0-12.8-12.8-25.6-25.6-25.6H409.6c-89.6 0-160 70.4-160 160v256c0 89.6 70.4 160 160 160h256c89.6 0 160-70.4 160-160V480c0-12.8-12.8-25.6-25.6-25.6z"></path>
              </svg>
            </div>
          </a>
          <a class="group transition-all hover:scale-110" href="https://github.com/328pikapika-bot/SwiftBoot" target="_blank" title="GitHub">
            <div class="w-9 h-9 rounded-full bg-[#333333] flex items-center justify-center shadow-lg shadow-black/20 border border-white/10">
              <svg class="w-5 h-5 fill-white" viewBox="0 0 24 24">
                <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"></path>
              </svg>
            </div>
          </a>
          
          <el-dropdown trigger="click" @command="handleLanguageChange">
            <button class="h-9 px-3 rounded-full bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 shadow-sm flex items-center gap-2 transition-all text-slate-600 dark:text-slate-300">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
              </svg>
              <span class="text-xs font-medium">{{ locale === 'zh-cn' ? '中文' : 'En' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-cn" :disabled="locale === 'zh-cn'">中文</el-dropdown-item>
                <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <button class="w-9 h-9 rounded-full bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 shadow-sm flex items-center justify-center transition-all text-slate-600 dark:text-slate-300" @click="toggleTheme" title="切换主题">
            <!-- Sun Icon (Light Mode) -->
            <svg class="w-4 h-4 block dark:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            <!-- Moon Icon (Dark Mode) -->
            <svg class="w-4 h-4 hidden dark:block" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
            </svg>
          </button>

          <el-dropdown trigger="click">
            <div class="flex items-center gap-3 pl-4 border-l border-slate-200 dark:border-slate-800 cursor-pointer">
              <div class="text-right hidden sm:block">
                <div class="text-xs font-semibold leading-none text-slate-900 dark:text-slate-100">{{ userStore.userInfo?.nickname || 'Admin' }}</div>
                <div class="text-[10px] text-slate-500 dark:text-slate-400 mt-1">Super User</div>
              </div>
              <img :src="userStore.userInfo?.avatar || 'https://lh3.googleusercontent.com/aida-public/AB6AXuBKKxp4o64VKOOMt5SPw0ePf0XixCWAa-cd_srSjsglvXvFMIXJY2SeSvo8K7CRpA1C0Uyov8aCvmBWX_hx5Q0GnfOvRclbXvP_UM-STIQZ-v8m-qqyXpc7IBjEFbxjJlyHoXuAq92bnLhZGA037m3AwvTPq1e8DAWqS1s1qKt6nkGoaVOPYpH_yIQFq6zwWSuTyh__otMPntjywlTfrmw57JJeTcN9lKePhYU-JDRwHSL-VwN5BjGH_5gAuIS1Z8uMNG_46ATQQ5k'" alt="User Avatar" class="w-9 h-9 rounded-full bg-slate-200 dark:bg-slate-700"/>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Router View -->
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </transition>
      </router-view>
    </main>

    <!-- Global AI Assistant -->
    <AiAssistant />
  </div>
</template>

<script setup lang="ts">
import { computed, provide, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessageBox } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import AiAssistant from '@/components/AiAssistant/index.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const { locale } = useI18n()

// Element Plus Icons Set
const elementIcons = Object.keys(ElementPlusIconsVue)
// 创建小写映射表以支持忽略大小写匹配
const lowerElementIcons = elementIcons.map(name => name.toLowerCase())

const isElementIcon = (iconName: string | undefined) => {
  if (!iconName) return false
  // 1. 精确匹配
  if (elementIcons.includes(iconName)) return true
  // 2. 忽略大小写匹配
  return lowerElementIcons.includes(iconName.toLowerCase())
}

const getElementIconName = (iconName: string | undefined) => {
  if (!iconName) return ''
  // 如果是精确匹配，直接返回
  if (elementIcons.includes(iconName)) return iconName
  // 如果是忽略大小写匹配，返回正确的 PascalCase 名称
  const index = lowerElementIcons.indexOf(iconName.toLowerCase())
  if (index > -1) return elementIcons[index]
  return ''
}

// Theme handling
const toggleTheme = () => {
  const htmlElement = document.documentElement
  if (htmlElement.classList.contains('dark')) {
    htmlElement.classList.remove('dark')
    localStorage.theme = 'light'
  } else {
    htmlElement.classList.add('dark')
    localStorage.theme = 'dark'
  }
}

const handleLanguageChange = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

// 刷新用户信息（供子组件调用）
const refreshMenu = async () => {
  await userStore.getUserInfo()
}
provide('refreshMenu', refreshMenu)

// Icon mapping (Backend -> Material Icons)
const iconMap: Record<string, string> = {
  'setting': 'settings',
  'user': 'person',
  'peoples': 'group',
  'menu': 'menu',
  'tree': 'corporate_fare', // dept
  'dict': 'book',
  'monitor': 'monitor_heart',
  'form': 'description',
  'logininfor': 'login',
  'tool': 'build',
  'code': 'code',
  'star-filled': 'star',
  'list': 'list',
  'message': 'chat',
  'chart': 'bar_chart',
  'server': 'memory',
  'job': 'schedule',
  'druid': 'storage',
  'system': 'settings_suggest'
}

const getMaterialIcon = (iconName: string | undefined) => {
  if (!iconName) return 'circle'
  // Remove el-icon prefix if exists (legacy)
  const name = iconName.toLowerCase().replace('#', '')
  
  // 1. Direct map check
  if (iconMap[name]) return iconMap[name]
  
  // 2. Element Plus icon check (CamelCase to underscore if needed)
  // 如果用户输入的是 Element Plus 图标名 (如 "UserFilled")，我们需要映射到 Material Icon
  // 或者直接返回原名（如果前端做了组件兼容）。但当前代码使用的是 material-icons-round class，所以必须返回 Material Icon 名。
  // 这是一个兼容性问题：菜单管理选的是 Element Plus 图标，但 Sidebar 渲染的是 Material Icons。
  // 临时方案：扩充映射表或支持直接渲染 Element Plus 图标。
  
  // 3. Fallback logic based on name keywords
  if (name.includes('user')) return 'person'
  if (name.includes('log')) return 'history'
  if (name.includes('tool')) return 'build'
  if (name.includes('monitor')) return 'monitor'
  if (name.includes('chat')) return 'chat'
  if (name.includes('ai')) return 'psychology'
  if (name.includes('guide')) return 'map'
  
  // 4. If it looks like a Material Icon name (lowercase, underscores), return it directly
  if (/^[a-z0-9_]+$/.test(name)) return name
  
  return 'circle' // Default dot
}

const resolvePath = (parentPath: string, childPath: string) => {
  if (childPath.startsWith('/')) return childPath
  return `${parentPath}/${childPath}`.replace('//', '/')
}

// Menu List
const menuList = computed(() => {
  const menus = userStore.userInfo?.menus || []
  if (menus.length > 0) {
    const homeMenu = {
      path: '/dashboard',
      meta: { title: '首页控制台', icon: 'dashboard' },
      children: [] as any[]
    }
    const dynamicMenus = menus.map(menu => ({
      path: '/' + menu.path,
      meta: { title: menu.menuName, icon: menu.icon },
      children: menu.children?.map((child: any) => ({
        path: child.path,
        meta: { title: child.menuName, icon: child.icon }
      })) || []
    }))
    return [homeMenu, ...dynamicMenus]
  }
  return []
})

const openMenus = ref<string[]>([])

const toggleMenu = (path: string) => {
  const index = openMenus.value.indexOf(path)
  if (index > -1) {
    openMenus.value.splice(index, 1)
  } else {
    openMenus.value.push(path)
  }
}

watch([() => route.path, menuList], ([newPath, menus]) => {
  menus.forEach(item => {
    if (item.children?.some((child: any) => resolvePath(item.path, child.path) === newPath)) {
      if (!openMenus.value.includes(item.path)) {
        openMenus.value.push(item.path)
      }
    }
  })
}, { immediate: true })

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  })
}

onMounted(() => {
  // Init theme
  if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style>
.font-display {
  font-family: 'Inter', 'Noto Sans SC', sans-serif;
}
.sidebar-scroll::-webkit-scrollbar {
  width: 4px;
}
.sidebar-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.sidebar-scroll::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
.dark .sidebar-scroll::-webkit-scrollbar-thumb {
  background: #1e293b;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
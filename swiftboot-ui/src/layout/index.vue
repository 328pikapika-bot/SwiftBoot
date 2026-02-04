<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'is-collapse': !appStore.sidebar.opened }">
      <div class="logo">
        <div class="logo-wrapper">
          <SwiftLogo class="logo-icon" />
        </div>
        <span v-show="appStore.sidebar.opened" class="app-title">SwiftBoot</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="!appStore.sidebar.opened"
          :collapse-transition="false"
          router
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#fff"
          class="custom-menu"
        >
          <template v-for="item in menuList" :key="item.path">
            <!-- 有子菜单的显示为下拉菜单 -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
              <template #title>
                <el-icon><component :is="item.meta?.icon" /></el-icon>
                <span>{{ item.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="`${item.path}/${child.path}`"
              >
                <el-icon><component :is="child.meta?.icon" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 没有子菜单的直接显示 -->
            <el-menu-item v-else :index="item.path">
              <el-icon><component :is="item.meta?.icon" /></el-icon>
              <span>{{ item.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container" :class="{ 'is-collapse': !appStore.sidebar.opened }">
      <!-- 头部 -->
      <header class="header">
        <div class="header-left">
          <div class="collapse-btn" @click="appStore.toggleSidebar">
            <el-icon :size="20">
              <Fold v-if="appStore.sidebar.opened" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/" class="custom-breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="Gitee 源码" placement="bottom">
            <a href="https://gitee.com/cs_shuang/SwiftBoot" target="_blank" class="header-icon-btn">
              <el-icon :size="20"><GiteeIcon /></el-icon>
            </a>
          </el-tooltip>

          <el-tooltip content="Github 源码" placement="bottom">
            <a href="https://github.com/328pikapika-bot/SwiftBoot" target="_blank" class="header-icon-btn">
              <el-icon :size="20"><GithubIcon /></el-icon>
            </a>
          </el-tooltip>
          
          <el-dropdown trigger="click" @command="handleLanguageChange" class="header-icon-btn">
            <div class="language-btn flex items-center justify-center h-full px-2">
              <span class="text-sm font-medium">{{ locale === 'zh-cn' ? '中文' : 'En' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-cn" :disabled="locale === 'zh-cn'">中文</el-dropdown-item>
                <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar || ''" class="user-avatar" />
              <div class="user-details">
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <span class="role-badge">Admin</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  {{ $t('header.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 全局智能助手 -->
    <AiAssistant />
  </div>
</template>

<script setup lang="ts">
import { computed, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import AiAssistant from '@/components/AiAssistant/index.vue'
import SwiftLogo from '@/components/SwiftLogo/index.vue'
import GithubIcon from '@/components/GithubIcon/index.vue'
import GiteeIcon from '@/components/GiteeIcon/index.vue'
import { Fold, Expand, ArrowDown, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const { locale } = useI18n()

const handleLanguageChange = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

// 刷新用户信息（供子组件调用）
const refreshMenu = async () => {
  await userStore.getUserInfo()
}
provide('refreshMenu', refreshMenu)

// 图标映射（后端图标名 -> Element Plus 图标名）
const iconMap: Record<string, string> = {
  'setting': 'Setting',
  'user': 'User',
  'peoples': 'UserFilled',
  'menu': 'Menu',
  'tree': 'OfficeBuilding',
  'dict': 'Collection',
  'monitor': 'Monitor',
  'form': 'Document',
  'logininfor': 'Tickets',
  'tool': 'Tools',
  'code': 'Document',
  'star-filled': 'StarFilled',
  'list': 'List'
}

// 菜单列表（使用后端动态菜单）
const menuList = computed(() => {
  const menus = userStore.userInfo?.menus || []
  if (menus.length > 0) {
    // 首页菜单（固定，直接显示为单个菜单项）
    const homeMenu = {
      path: '/dashboard',
      meta: { title: '首页', icon: 'HomeFilled' },
      children: [] as any[]
    }
    // 后端动态菜单
    const dynamicMenus = menus.map(menu => ({
      path: '/' + menu.path,
      meta: { title: menu.menuName, icon: iconMap[menu.icon] || menu.icon || 'Document' },
      children: menu.children?.map((child: any) => ({
        path: child.path,
        meta: { title: child.menuName, icon: iconMap[child.icon] || child.icon || 'Document' }
      })) || []
    }))
    return [homeMenu, ...dynamicMenus]
  }
  // 后端菜单为空时使用静态路由
  return router.options.routes.filter(
    (item) => item.path !== '/login' && item.name !== 'NotFound' && !item.meta?.hidden
  )
})

// 面包屑
const breadcrumbs = computed(() => {
  return route.matched.filter((item) => item.meta?.title)
})

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  })
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #f8fafc;
}

/* Sidebar Styling */
.sidebar {
  width: 232px;
  height: 100%;
  background-color: #0f172a; /* Dark blue-grey from login */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.05);
  z-index: 100;
  
  &.is-collapse {
    width: 72px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .app-title {
        opacity: 0;
        width: 0;
        margin: 0;
      }
    }
  }
  
  .logo {
    height: 72px;
    display: flex;
    align-items: center;
    padding: 0 24px;
    gap: 12px;
    transition: all 0.3s;
    
    .logo-wrapper {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    
    .logo-icon {
      transform: scale(0.9);
    }
    
    .app-title {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      white-space: nowrap;
      font-family: 'Outfit', sans-serif;
      letter-spacing: -0.5px;
      background: linear-gradient(135deg, #fff 0%, #a5b4fc 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      transition: opacity 0.3s, width 0.3s;
    }
  }
}

/* Menu Styling */
.custom-menu {
  border-right: none;
  padding: 12px;
  
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
    margin-bottom: 4px;
    border-radius: 12px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
      color: #fff !important;
    }
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, #409eff 0%, #3b82f6 100%) !important;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    font-weight: 600;
    
    .el-icon {
      color: #fff;
    }
  }
}

/* Main Container */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s;
}

/* Header Styling */
.header {
  height: 72px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 90;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .collapse-btn {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      cursor: pointer;
      color: #64748b;
      transition: all 0.2s;
      
      &:hover {
        background-color: #f1f5f9;
        color: #1e293b;
      }
    }
    
    .custom-breadcrumb {
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          font-weight: 500;
          color: #64748b;
          
          &:hover {
            color: #409eff;
          }
        }
        
        &:last-child .el-breadcrumb__inner {
          color: #1e293b;
          font-weight: 600;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .header-icon-btn {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      color: #64748b;
      transition: all 0.2s;
      
      &:hover {
        background-color: #f1f5f9;
        color: #1e293b;
      }
    }
    
    .user-dropdown {
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 40px;
      transition: all 0.2s;
      border: 1px solid transparent;
      
      &:hover {
        background-color: #fff;
        border-color: #e2e8f0;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      }
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .user-avatar {
          border: 2px solid #fff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .user-details {
          display: flex;
          flex-direction: column;
          line-height: 1.2;
          
          .username {
            font-size: 14px;
            font-weight: 600;
            color: #1e293b;
          }
          
          .role-badge {
            font-size: 11px;
            color: #94a3b8;
          }
        }
        
        .dropdown-icon {
          color: #94a3b8;
          font-size: 12px;
          margin-left: 4px;
        }
      }
    }
  }
}

/* Content Area */
.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background-color: #f8fafc;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>

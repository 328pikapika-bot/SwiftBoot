<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'is-collapse': !appStore.sidebar.opened }">
      <div class="logo">
        <img src="/vite.svg" alt="logo" />
        <span v-show="appStore.sidebar.opened">SwiftBoot</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="!appStore.sidebar.opened"
          :collapse-transition="false"
          router
          background-color="#001529"
          text-color="#ffffffa6"
          active-text-color="#fff"
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
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="appStore.sidebar.opened" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || ''" />
              <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

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
}

.sidebar {
  width: 220px;
  height: 100%;
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
  
  &.is-collapse {
    width: 64px;
  }
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    border-bottom: 1px solid #ffffff1a;
    
    img {
      width: 32px;
      height: 32px;
    }
    
    span {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }
  
  .el-menu {
    border-right: none;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      span {
        color: #303133;
      }
    }
  }
}

.content {
  flex: 1;
  overflow: auto;
  background-color: #f5f7fa;
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

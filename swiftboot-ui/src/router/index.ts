import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

// 静态路由
const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { title: '首页', icon: 'HomeFilled' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      }
    ]
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/layout/index.vue'),
    redirect: '/monitor/operlog',
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      {
        path: 'operlog',
        name: 'OperLog',
        component: () => import('@/views/monitor/operlog/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'loginlog',
        name: 'LoginLog',
        component: () => import('@/views/monitor/loginlog/index.vue'),
        meta: { title: '登录日志', icon: 'Tickets' }
      }
    ]
  },
  {
    path: '/tool',
    name: 'Tool',
    component: () => import('@/layout/index.vue'),
    redirect: '/tool/gen',
    meta: { title: '系统工具', icon: 'Tools' },
    children: [
      {
        path: 'gen',
        name: 'Gen',
        component: () => import('@/views/tool/gen/index.vue'),
        meta: { title: '代码生成', icon: 'Document' }
      },
      {
        path: 'icon',
        name: 'Icon',
        component: () => import('@/views/tool/icon/index.vue'),
        meta: { title: '图标参考', icon: 'StarFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 白名单
const whiteList = ['/login']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - SwiftBoot`
  
  const userStore = useUserStore()
  const token = userStore.token
  
  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!userStore.userInfo) {
        try {
          await userStore.getUserInfo()
          next({ ...to, replace: true })
        } catch (error) {
          userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        next()
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router

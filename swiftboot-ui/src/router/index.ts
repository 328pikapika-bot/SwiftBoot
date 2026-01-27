import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

/**
 * 基础路由（只放：登录、首页、404）
 */
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
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

/**
 * 自动加载 modules 下所有路由
 */
const modules = import.meta.glob('./modules/*.ts', { eager: true })

const moduleRoutes: RouteRecordRaw[] = Object.values(modules).flatMap(
  (module: any) => module.default
)

/**
 * 合并路由
 */
const routes: RouteRecordRaw[] = [
  ...constantRoutes,
  ...moduleRoutes
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

/**
 * 路由守卫
 */
const whiteList = ['/login']

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
        } catch (e) {
          userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        next()
      }
    }
  } else {
    whiteList.includes(to.path)
      ? next()
      : next(`/login?redirect=${to.path}`)
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router

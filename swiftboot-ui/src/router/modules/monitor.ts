import { RouteRecordRaw } from 'vue-router'

const monitorRoutes: RouteRecordRaw[] = [
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/layout/index.vue'),
    redirect: '/monitor/operlog',
    meta: {
      title: '系统监控',
      icon: 'Monitor'
    },
    children: [
      {
        path: 'operlog',
        name: 'Operlog',
        component: () => import('@/views/monitor/operlog/index.vue'),
        meta: {
          title: '操作日志',
          icon: 'Document'
        }
      },
      {
        path: 'loginlog',
        name: 'Loginlog',
        component: () => import('@/views/monitor/loginlog/index.vue'),
        meta: {
          title: '登录日志',
          icon: 'User'
        }
      },
      {
        path: 'server',
        name: 'ServerMonitor',
        component: () => import('@/views/monitor/server/index.vue'),
        meta: {
          title: '基础资源',
          icon: 'Coin'
        }
      }
    ]
  }
]

export default monitorRoutes

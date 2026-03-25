import { RouteRecordRaw } from 'vue-router'

const monitorRoutes: RouteRecordRaw[] = [
  {
    path: '/monitor',
    name: 'MonitorCenter',
    component: () => import('@/layout/index.vue'),
    redirect: '/monitor/operlog',
    meta: {
      title: '监控中心',
      icon: 'Monitor'
    },
    children: [
      {
        path: 'operlog',
        name: 'MonitorOperlog',
        component: () => import('@/views/monitor/operlog/index.vue'),
        meta: {
          title: '操作日志',
          icon: 'Document'
        }
      },
      {
        path: 'loginlog',
        name: 'MonitorLoginlog',
        component: () => import('@/views/monitor/loginlog/index.vue'),
        meta: {
          title: '登录日志',
          icon: 'User'
        }
      },
      {
        path: 'ai-dashboard',
        name: 'MonitorAiDashboard',
        component: () => import('@/views/monitor/ai-session/index.vue'),
        meta: {
          title: 'AI看板',
          icon: 'DataAnalysis'
        }
      },
      {
        path: 'server',
        name: 'MonitorServer',
        component: () => import('@/views/monitor/server/index.vue'),
        meta: {
          title: '基础资源',
          icon: 'Coin'
        }
      },
      {
        path: 'ai-block-hit',
        name: 'MonitorAiBlockHit',
        component: () => import('@/views/monitor/ai-block-hit/index.vue'),
        meta: {
          title: '屏蔽词命中日志',
          icon: 'Warning'
        }
      }
    ]
  }
]

export default monitorRoutes

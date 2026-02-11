import { RouteRecordRaw } from 'vue-router'

const aiRoutes: RouteRecordRaw[] = [
  {
    path: '/ai',
    name: 'Ai',
    component: () => import('@/layout/index.vue'),
    redirect: '/ai/dashboard',
    meta: {
      title: '智能会话',
      icon: 'guide'
    },
    children: [
      {
        path: 'dashboard',
        name: 'AiDashboard',
        component: () => import('@/views/monitor/ai-session/index.vue'),
        meta: {
          title: 'AI看板',
          icon: 'histogram'
        }
      },
      {
        path: 'chat',
        name: 'AiChat',
        component: () => import('@/views/ai/chat/index.vue'),
        meta: {
          title: '会话窗口',
          icon: 'message'
        }
      }
    ]
  }
]

export default aiRoutes

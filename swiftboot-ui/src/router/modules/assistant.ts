import { RouteRecordRaw } from 'vue-router'

const assistantRoutes: RouteRecordRaw[] = [
  {
    path: '/assistant',
    name: 'AssistantCenter',
    component: () => import('@/layout/index.vue'),
    redirect: '/assistant/chat',
    meta: {
      title: '智能助手',
      icon: 'Promotion'
    },
    children: [
      {
        path: 'chat',
        name: 'AssistantChat',
        component: () => import('@/views/ai/chat/index.vue'),
        meta: { title: '会话窗口', icon: 'Message' }
      },
      {
        path: 'config',
        name: 'AssistantConfig',
        component: () => import('@/views/tool/config/index.vue'),
        meta: { title: 'AI配置', icon: 'Setting' }
      }
    ]
  }
]

export default assistantRoutes

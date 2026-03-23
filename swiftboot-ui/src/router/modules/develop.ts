import { RouteRecordRaw } from 'vue-router'

const developRoutes: RouteRecordRaw[] = [
  {
    path: '/develop',
    name: 'DevelopTools',
    component: () => import('@/layout/index.vue'),
    redirect: '/develop/gen',
    meta: {
      title: '开发工具',
      icon: 'Tools'
    },
    children: [
      {
        path: 'gen',
        name: 'DevelopGen',
        component: () => import('@/views/tool/gen/index.vue'),
        meta: { title: '代码生成', icon: 'Document' }
      },
      {
        path: 'chart',
        name: 'DevelopChart',
        component: () => import('@/views/tool/chart/index.vue'),
        meta: { title: '图表设计', icon: 'PieChart' }
      },
      {
        path: 'icon',
        name: 'DevelopIcon',
        component: () => import('@/views/tool/icon/index.vue'),
        meta: { title: '图标选择', icon: 'Star' }
      }
    ]
  }
]

export default developRoutes

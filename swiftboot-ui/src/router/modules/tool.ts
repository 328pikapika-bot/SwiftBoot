import { RouteRecordRaw } from 'vue-router'

const toolRoutes: RouteRecordRaw[] = [
  {
    path: '/tool',
    name: 'Tool',
    component: () => import('@/layout/index.vue'),
    redirect: '/tool/gen',
    meta: {
      title: '系统工具',
      icon: 'Setting'
    },
    children: [
      {
        path: 'gen',
        name: 'Gen',
        component: () => import('@/views/tool/gen/index.vue'),
        meta: {
          title: '代码生成',
          icon: 'Document'
        }
      },
      {
        path: 'chart',
        name: 'Chart',
        component: () => import('@/views/tool/chart/index.vue'),
        meta: {
          title: '图表设计',
          icon: 'PieChart'
        }
      },
      {
        path: 'icon',
        name: 'Icon',
        component: () => import('@/views/tool/icon/index.vue'),
        meta: {
          title: '图标选择',
          icon: 'Star'
        }
      }
    ]
  }
]

export default toolRoutes
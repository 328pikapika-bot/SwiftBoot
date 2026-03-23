import { RouteRecordRaw } from 'vue-router'

const testProjectRoutes: RouteRecordRaw[] = [
  {
    path: '/testProject',
    name: 'TestProject',
    component: () => import('@/layout/index.vue'),
    redirect: '/testProject/index',
    meta: {
      title: '项目示例',
      icon: 'Folder'
    },
    children: [
      {
        path: 'index',
        name: 'TestProjectIndex',
        component: () => import('@/views/testProject/index.vue'),
        meta: {
          title: '项目示例首页',
          icon: 'Document'
        }
      }
    ]
  }
]

export default testProjectRoutes

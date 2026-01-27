import { RouteRecordRaw } from 'vue-router'

const testProjectRoutes: RouteRecordRaw[] = [
  {
    path: '/testProject',
    name: 'TestProject',
    component: () => import('@/layout/index.vue'),
    redirect: '/testProject/index',
    meta: {
      title: '测试项目',
      icon: 'Folder'
    },
    children: [
      {
        path: 'index',
        name: 'TestProjectIndex',
        component: () => import('@/views/testProject/index.vue'),
        meta: {
          title: '项目测试首页',
          icon: 'Document'
        }
      }
    ]
  }
]

export default testProjectRoutes

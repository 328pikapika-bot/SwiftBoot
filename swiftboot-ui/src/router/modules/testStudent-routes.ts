import { RouteRecordRaw } from 'vue-router'

const testStudentRoutes: RouteRecordRaw[] = [
  {
    path: '/testStudent',
    name: 'TestStudent',
    component: () => import('@/layout/index.vue'),
    redirect: '/testStudent/index',
    meta: {
      title: '测试学生表',
      icon: 'Folder'
    },
    children: [
      {
        path: 'index',
        name: 'TestStudentIndex',
        component: () => import('@/views/testStudent/index.vue'),
        meta: {
          title: '测试学生表首页',
          icon: 'Document'
        }
      }
    ]
  }
]

export default testStudentRoutes

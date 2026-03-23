import { RouteRecordRaw } from 'vue-router'

const exampleRoutes: RouteRecordRaw[] = [
  {
    path: '/examples',
    name: 'ExampleCenter',
    component: () => import('@/layout/index.vue'),
    redirect: '/examples/project',
    meta: {
      title: '示例业务',
      icon: 'FolderOpened'
    },
    children: [
      {
        path: 'project',
        name: 'ExampleProject',
        component: () => import('@/views/testProject/index.vue'),
        meta: { title: '项目示例', icon: 'Document' }
      },
      {
        path: 'student',
        name: 'ExampleStudent',
        component: () => import('@/views/testStudent/index.vue'),
        meta: { title: '学生示例', icon: 'Document' }
      }
    ]
  }
]

export default exampleRoutes

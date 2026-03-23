import { RouteRecordRaw } from 'vue-router'

const accessRoutes: RouteRecordRaw[] = [
  {
    path: '/access',
    name: 'AccessCenter',
    component: () => import('@/layout/index.vue'),
    redirect: '/access/user',
    meta: {
      title: '权限中心',
      icon: 'AdminPanelSettings'
    },
    children: [
      {
        path: 'user',
        name: 'AccessUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'AccessRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'menu',
        name: 'AccessMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'List' }
      },
      {
        path: 'dept',
        name: 'AccessDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'post',
        name: 'AccessPost',
        component: () => import('@/views/system/post/index.vue'),
        meta: { title: '岗位管理', icon: 'Briefcase' }
      }
    ]
  }
]

export default accessRoutes

import { RouteRecordRaw } from 'vue-router'

const systemRoutes: RouteRecordRaw[] = [
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: {
      title: '系统管理',
      icon: 'Setting'
    },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User'
        }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'UserFilled'
        }
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: {
          title: '菜单管理',
          icon: 'List'
        }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: {
          title: '部门管理',
          icon: 'OfficeBuilding'
        }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: {
          title: '字典管理',
          icon: 'Collection'
        }
      },
      {
        path: 'post',
        name: 'Post',
        component: () => import('@/views/system/post/index.vue'),
        meta: {
          title: '岗位管理',
          icon: 'Briefcase'
        }
      },
      {
        path: 'job',
        name: 'Job',
        component: () => import('@/views/system/job/index.vue'),
        meta: {
          title: '定时任务',
          icon: 'Timer'
        }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: {
          title: '系统公告',
          icon: 'Bell'
        }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/system/message/index.vue'),
        meta: {
          title: '站内消息',
          icon: 'Message'
        }
      },
      {
        path: 'file',
        name: 'File',
        component: () => import('@/views/system-file-manage.vue'),
        meta: {
          title: '文件管理',
          icon: 'Folder'
        }
      }
    ]
  }
]

export default systemRoutes

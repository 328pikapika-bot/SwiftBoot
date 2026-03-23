import { RouteRecordRaw } from 'vue-router'

const platformRoutes: RouteRecordRaw[] = [
  {
    path: '/platform',
    name: 'PlatformCenter',
    component: () => import('@/layout/index.vue'),
    redirect: '/platform/dict',
    meta: {
      title: '平台中心',
      icon: 'Grid'
    },
    children: [
      {
        path: 'dict',
        name: 'PlatformDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'file',
        name: 'PlatformFile',
        component: () => import('@/views/system-file-manage.vue'),
        meta: { title: '文件管理', icon: 'Folder' }
      },
      {
        path: 'notice',
        name: 'PlatformNotice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: { title: '系统公告', icon: 'Bell' }
      },
      {
        path: 'message',
        name: 'PlatformMessage',
        component: () => import('@/views/system/message/index.vue'),
        meta: { title: '站内消息', icon: 'Message' }
      },
      {
        path: 'job',
        name: 'PlatformJob',
        component: () => import('@/views/system/job/index.vue'),
        meta: { title: '定时任务', icon: 'Timer' }
      },
      {
        path: 'config',
        name: 'PlatformConfig',
        component: () => import('@/views/tool-config-page.vue'),
        meta: { title: '配置管理', icon: 'Setting' }
      }
    ]
  }
]

export default platformRoutes

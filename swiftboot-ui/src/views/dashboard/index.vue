<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-banner">
        <div class="banner-content">
          <h1>{{ $t('dashboard.hello') }}, {{ userStore.userInfo?.nickname || 'Admin' }}! </h1>
          <p>{{ $t('dashboard.subtitle') }}</p>
          <div class="quick-stats">
            <div class="stat-badge">
              <span class="dot online"></span>
              {{ $t('dashboard.systemOnline') }}
            </div>
            <div class="stat-badge">
              <span class="dot update"></span>
              {{ $t('dashboard.version') }} v1.0.0
            </div>
          </div>
        </div>
        <div class="banner-illustration">
          <!-- Abstract shapes matching login mesh -->
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-grid">
      <div v-for="item in statCards" :key="item.key" class="stat-card-modern">
        <div class="card-icon-wrapper" :style="{ background: `rgba(${item.rgb}, 0.1)`, color: `rgb(${item.rgb})` }">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <div class="card-info">
          <span class="stat-label">{{ $t(item.title) }}</span>
          <div class="stat-value-group">
            <span class="stat-number">{{ item.value }}</span>
            <span class="stat-trend positive" v-if="true">
              +12% <el-icon><Top /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- 快捷入口 -->
      <div class="grid-section quick-access">
        <div class="section-header">
          <h3>{{ $t('dashboard.quickAccess') }}</h3>
        </div>
        <div class="quick-links-grid">
          <router-link
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="quick-link-item"
          >
            <div class="icon-box" :style="{ background: link.bg }">
              <el-icon :size="24" color="#fff"><component :is="link.icon" /></el-icon>
            </div>
            <span>{{ link.title }}</span>
          </router-link>
        </div>
      </div>

      <!-- 技术栈展示 -->
      <div class="grid-section tech-stack">
        <div class="section-header">
          <h3>{{ $t('dashboard.techStack') }}</h3>
        </div>
        <div class="tech-groups">
          <div class="tech-group">
            <span class="group-label">{{ $t('dashboard.backend') }}</span>
            <div class="tags-flow">
              <span class="tech-tag">Java 17</span>
              <span class="tech-tag">Spring Boot 3</span>
              <span class="tech-tag">MyBatis Plus</span>
              <span class="tech-tag">Sa-Token</span>
            </div>
          </div>
          <div class="tech-group">
            <span class="group-label">{{ $t('dashboard.frontend') }}</span>
            <div class="tags-flow">
              <span class="tech-tag">Vue 3.4</span>
              <span class="tech-tag">Vite 5</span>
              <span class="tech-tag">TypeScript</span>
              <span class="tech-tag">UnoCSS</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import SwiftLogo from '@/components/SwiftLogo/index.vue'
import { getIndexStats } from '@/api/index'
import { Top } from '@element-plus/icons-vue'

const userStore = useUserStore()

const statCards = ref([
  { title: 'dashboard.stats.users', value: '0', icon: 'User', rgb: '64, 158, 255', key: 'userCount' },
  { title: 'dashboard.stats.roles', value: '0', icon: 'UserFilled', rgb: '103, 194, 58', key: 'roleCount' },
  { title: 'dashboard.stats.menus', value: '0', icon: 'Menu', rgb: '230, 162, 60', key: 'menuCount' },
  { title: 'dashboard.stats.depts', value: '0', icon: 'OfficeBuilding', rgb: '245, 108, 108', key: 'deptCount' }
])

const quickLinks = ref([
  { title: '用户管理', path: '/system/user', icon: 'User', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '角色管理', path: '/system/role', icon: 'UserFilled', bg: 'linear-gradient(135deg, #6a11cb 0%, #2575fc 100%)' },
  { title: '菜单管理', path: '/system/menu', icon: 'Menu', bg: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)' },
  { title: '部门管理', path: '/system/dept', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)' },
  { title: '字典管理', path: '/system/dict', icon: 'Collection', bg: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)' },
  { title: '代码生成', path: '/tool/gen', icon: 'Document', bg: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' }
])

const getStats = async () => {
  try {
    const res = await getIndexStats()
    statCards.value.forEach(item => {
      if (res.data[item.key]) {
        item.value = res.data[item.key].toString()
      }
    })
  } catch (error) {
    console.error('获取首页统计失败', error)
  }
}

onMounted(() => {
  getStats()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* Welcome Section */
.welcome-section {
  margin-bottom: 32px;
}

.welcome-banner {
  background: #0f172a;
  border-radius: 24px;
  padding: 48px;
  position: relative;
  overflow: hidden;
  color: white;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.2);
  
  .banner-content {
    position: relative;
    z-index: 2;
    max-width: 600px;
    
    h1 {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 12px;
      letter-spacing: -1px;
    }
    
    p {
      color: #94a3b8;
      font-size: 16px;
      margin-bottom: 24px;
      line-height: 1.6;
    }
    
    .quick-stats {
      display: flex;
      gap: 16px;
      
      .stat-badge {
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        padding: 8px 16px;
        border-radius: 30px;
        font-size: 13px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 8px;
        border: 1px solid rgba(255, 255, 255, 0.05);
        
        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          
          &.online { background-color: #4ade80; box-shadow: 0 0 10px rgba(74, 222, 128, 0.5); }
          &.update { background-color: #60a5fa; box-shadow: 0 0 10px rgba(96, 165, 250, 0.5); }
        }
      }
    }
  }
  
  .banner-illustration {
    position: absolute;
    top: 0;
    right: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    pointer-events: none;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      opacity: 0.6;
    }
    
    .shape-1 {
      width: 300px;
      height: 300px;
      background: #4f46e5;
      top: -100px;
      right: -50px;
    }
    
    .shape-2 {
      width: 250px;
      height: 250px;
      background: #ec4899;
      bottom: -50px;
      right: 20%;
    }
  }
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card-modern {
  background: white;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f1f5f9;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  }
  
  .card-icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .card-info {
    flex: 1;
    
    .stat-label {
      font-size: 14px;
      color: #64748b;
      margin-bottom: 4px;
      display: block;
      font-weight: 500;
    }
    
    .stat-value-group {
      display: flex;
      align-items: baseline;
      gap: 8px;
      
      .stat-number {
        font-size: 24px;
        font-weight: 700;
        color: #1e293b;
      }
      
      .stat-trend {
        font-size: 12px;
        color: #10b981;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 2px;
      }
    }
  }
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  
  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.grid-section {
  background: white;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  
  .section-header {
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
    }
  }
}

.quick-links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
  
  .quick-link-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-radius: 16px;
    transition: all 0.2s;
    
    &:hover {
      background: #f8fafc;
      
      .icon-box {
        transform: scale(1.1);
      }
    }
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    }
    
    span {
      font-size: 13px;
      font-weight: 500;
      color: #475569;
    }
  }
}

.tech-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
  
  .tech-group {
    .group-label {
      display: block;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #94a3b8;
      margin-bottom: 12px;
      font-weight: 600;
    }
    
    .tags-flow {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      .tech-tag {
        background: #f1f5f9;
        color: #475569;
        padding: 6px 12px;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 500;
        border: 1px solid #e2e8f0;
      }
    }
  }
}
</style>

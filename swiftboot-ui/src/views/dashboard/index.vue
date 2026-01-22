<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎使用 SwiftBoot 后台管理系统 👋</h2>
          <p>一个轻量级、现代化的后台管理框架，基于 Spring Boot 3 + Vue 3 构建</p>
        </div>
        <div class="welcome-info">
          <div class="info-item">
            <span class="label">当前用户</span>
            <span class="value">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">角色</span>
            <span class="value">{{ userStore.userInfo?.roles?.join(', ') || '-' }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card v-for="item in statCards" :key="item.title" class="stat-card" shadow="never">
        <div class="stat-content">
          <div class="stat-info">
            <span class="stat-value">{{ item.value }}</span>
            <span class="stat-title">{{ item.title }}</span>
          </div>
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷入口 -->
    <el-card class="quick-card" shadow="never">
      <template #header>
        <span>快捷入口</span>
      </template>
      <div class="quick-links">
        <router-link
          v-for="link in quickLinks"
          :key="link.path"
          :to="link.path"
          class="quick-link"
        >
          <el-icon :size="32" :color="link.color"><component :is="link.icon" /></el-icon>
          <span>{{ link.title }}</span>
        </router-link>
      </div>
    </el-card>

    <!-- 技术栈 -->
    <el-card class="tech-card" shadow="never">
      <template #header>
        <span>技术栈</span>
      </template>
      <div class="tech-list">
        <div class="tech-section">
          <h4>后端技术</h4>
          <div class="tech-tags">
            <el-tag>JDK 17</el-tag>
            <el-tag type="success">Spring Boot 3.2</el-tag>
            <el-tag type="warning">MyBatis Plus</el-tag>
            <el-tag type="danger">Sa-Token</el-tag>
            <el-tag type="info">Redis</el-tag>
            <el-tag>MySQL 8.0</el-tag>
            <el-tag type="success">Knife4j</el-tag>
          </div>
        </div>
        <div class="tech-section">
          <h4>前端技术</h4>
          <div class="tech-tags">
            <el-tag>Vue 3.4</el-tag>
            <el-tag type="success">Vite 5</el-tag>
            <el-tag type="warning">TypeScript</el-tag>
            <el-tag type="danger">Element Plus</el-tag>
            <el-tag type="info">Pinia</el-tag>
            <el-tag>UnoCSS</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const statCards = ref([
  { title: '用户数', value: '128', icon: 'User', color: '#409eff' },
  { title: '角色数', value: '8', icon: 'UserFilled', color: '#67c23a' },
  { title: '菜单数', value: '45', icon: 'Menu', color: '#e6a23c' },
  { title: '部门数', value: '12', icon: 'OfficeBuilding', color: '#f56c6c' }
])

const quickLinks = ref([
  { title: '用户管理', path: '/system/user', icon: 'User', color: '#409eff' },
  { title: '角色管理', path: '/system/role', icon: 'UserFilled', color: '#67c23a' },
  { title: '菜单管理', path: '/system/menu', icon: 'Menu', color: '#e6a23c' },
  { title: '部门管理', path: '/system/dept', icon: 'OfficeBuilding', color: '#f56c6c' },
  { title: '字典管理', path: '/system/dict', icon: 'Collection', color: '#909399' },
  { title: '代码生成', path: '/tool/gen', icon: 'Document', color: '#9c27b0' }
])
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  
  :deep(.el-card__body) {
    padding: 30px;
  }
  
  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
  }
  
  .welcome-text {
    h2 {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 10px;
    }
    
    p {
      font-size: 14px;
      opacity: 0.9;
      margin: 0;
    }
  }
  
  .welcome-info {
    display: flex;
    gap: 40px;
    
    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .label {
        font-size: 12px;
        opacity: 0.8;
      }
      
      .value {
        font-size: 16px;
        font-weight: 500;
      }
    }
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .stat-info {
    display: flex;
    flex-direction: column;
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }
    
    .stat-title {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
}

.quick-card {
  margin-bottom: 20px;
  
  .quick-links {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;
  }
  
  .quick-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    border-radius: 8px;
    background: #f5f7fa;
    transition: all 0.3s;
    text-decoration: none;
    
    &:hover {
      background: #ecf5ff;
      transform: translateY(-2px);
    }
    
    span {
      font-size: 14px;
      color: #606266;
    }
  }
}

.tech-card {
  .tech-list {
    display: flex;
    gap: 40px;
  }
  
  .tech-section {
    flex: 1;
    
    h4 {
      font-size: 14px;
      color: #303133;
      margin: 0 0 12px;
    }
    
    .tech-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
}
</style>

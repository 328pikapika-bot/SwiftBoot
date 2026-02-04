<template>
  <div
    class="ai-assistant-container"
    :class="{ minimized: isMinimized, 'is-resizing': isResizing }"
    :style="containerStyle"
    ref="containerRef"
  >
    <!-- 最小化状态 -->
    <div v-if="isMinimized" class="ai-minimized" @dblclick="toggleMinimize" @mousedown="startDrag">
      <div class="ai-avatar-pulse">
        <div class="ai-logo-text">AI</div>
      </div>
      <div class="status-indicator"></div>
      <!-- 未读消息徽章 (示例) -->
      <div class="unread-badge" v-if="false">1</div>
    </div>

    <!-- 展开状态 -->
    <div v-else class="ai-expanded">
      <!-- 头部 -->
      <div class="ai-header" @mousedown="startDrag" @dblclick="toggleMinimize">
        <div class="header-left">
          <div class="ai-avatar-small">
            <div class="ai-logo-text-small">AI</div>
          </div>
          <div class="header-info">
            <span class="header-title">SwiftBoot问答小助手</span>
            <span class="header-status">
              <span class="status-dot"></span>在线·智能响应
            </span>
          </div>
        </div>
        <div class="header-actions">
          <el-tooltip content="清除历史" placement="bottom">
            <el-icon class="action-icon" @click.stop="clearHistory"><Delete /></el-icon>
          </el-tooltip>
          <el-tooltip content="最小化" placement="bottom">
            <el-icon class="action-icon" @click.stop="toggleMinimize"><Minus /></el-icon>
          </el-tooltip>
          <el-tooltip content="关闭" placement="bottom">
             <el-icon class="action-icon close-icon" @click.stop="toggleMinimize"><Close /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="ai-body" ref="messagesRef">
        <div class="welcome-message" v-if="messages.length === 0">
          <div class="welcome-logo">
             <div class="ai-logo-text-large">AI</div>
          </div>
          <h3>有什么可以帮您？</h3>
          <p class="welcome-desc">我是您的专属智能编程助手，可以解答关于代码逻辑、数据库设计或项目架构的问题。</p>
          <div class="suggestion-chips">
            <div class="chip" @click="sendSuggestion('请详细介绍 SwiftBoot 项目的整体架构和模块划分')">
              项目架构解析
            </div>
            <div class="chip" @click="sendSuggestion('分析一下用户登录和 JWT 认证的完整流程')">
              登录认证流程
            </div>
            <div class="chip" @click="sendSuggestion('如何使用代码生成器快速创建一个 CRUD 模块？')">
              代码生成器使用
            </div>
            <div class="chip" @click="sendSuggestion('请说明 sys_user, sys_role, sys_menu 等核心表的关系')">
              核心库表关系
            </div>
            <div class="chip" @click="sendSuggestion('我想新增一个业务功能，请给出前后端的完整开发步骤')">
              新增业务功能指南
            </div>
            <div class="chip" @click="sendSuggestion('前端是如何封装 Axios 请求并与后端交互的？')">
              前后端交互机制
            </div>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
          <div class="message-avatar" v-if="msg.role === 'assistant'">
            <div class="ai-logo-text-small">AI</div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-bubble">
              <div v-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
            <!-- 复制按钮 (仅 AI 消息显示) -->
            <div v-if="msg.role === 'assistant'" class="message-actions">
              <el-tooltip content="复制内容" placement="top" :show-after="500">
                <div class="action-btn" @click="copyContent(msg.content)">
                  <el-icon><CopyDocument /></el-icon> 复制全文
                </div>
              </el-tooltip>
            </div>
          </div>
        </div>

        <div v-if="loading" class="message-row assistant">
          <div class="message-avatar">
            <div class="ai-logo-text-small">AI</div>
          </div>
          <div class="message-bubble loading">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部输入区 -->
      <div class="ai-footer">
        <div class="input-wrapper">
          <el-input
            v-model="inputContent"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 5 }"
            placeholder="输入您的问题 (Shift+Enter 换行)..."
            @keydown.enter.prevent="(e: KeyboardEvent) => handleEnterKey(e)"
            :disabled="loading"
            class="ai-input"
          />
          <div class="input-actions">
             <el-tooltip content="发送" placement="top">
                <el-button 
                  class="send-btn" 
                  :class="{ 'is-active': inputContent.trim() }"
                  @click="sendMessage" 
                  :loading="loading" 
                  :disabled="!inputContent.trim()"
                  circle
                >
                  <el-icon><Position /></el-icon>
                </el-button>
             </el-tooltip>
          </div>
        </div>
      </div>

      <div v-if="isResizing" class="resize-indicator">
        {{ windowState.width }} × {{ windowState.height }}
      </div>
      <div class="resize-handle corner top-left" @mousedown.stop="startResize($event, 'top-left')"></div>
      <div class="resize-handle corner top-right" @mousedown.stop="startResize($event, 'top-right')"></div>
      <div class="resize-handle corner bottom-left" @mousedown.stop="startResize($event, 'bottom-left')"></div>
      <div class="resize-handle corner bottom-right" @mousedown.stop="startResize($event, 'bottom-right')"></div>
      <div class="resize-handle edge top" @mousedown.stop="startResize($event, 'top')"></div>
      <div class="resize-handle edge right" @mousedown.stop="startResize($event, 'right')"></div>
      <div class="resize-handle edge bottom" @mousedown.stop="startResize($event, 'bottom')"></div>
      <div class="resize-handle edge left" @mousedown.stop="startResize($event, 'left')"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onUnmounted } from 'vue'
import { Minus, Position, CopyDocument, Close, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendAiChat } from '@/api/index'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css/github-markdown.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

// 状态
const isMinimized = ref(true)
const loading = ref(false)
const inputContent = ref('')
const messages = ref<{ role: 'user' | 'assistant', content: string }[]>([])
const containerRef = ref<HTMLElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)

// 窗口尺寸和位置
const windowState = reactive({
  // 默认位置：屏幕右侧，预留足够宽度显示悬浮球
  // 计算逻辑：屏幕宽度 - 窗口宽度的一半(350) - 悬浮球预留(100)
  x: window.innerWidth - 450, 
  y: window.innerHeight / 2 - 350, // 垂直居中
  width: 700,
  height: 700
})

const isDragging = ref(false)
const isResizing = ref(false)
const dragOffset = reactive({ x: 0, y: 0 })
const resizeStart = reactive({ x: 0, y: 0, width: 0, height: 0, left: 0, top: 0 })
const resizeDirection = ref<'top' | 'right' | 'bottom' | 'left' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right'>('bottom-right')

const containerStyle = computed(() => {
  if (isMinimized.value) {
    // 最小化时，基于当前窗口位置计算中心点，使悬浮球相对于窗口中心居中
    const centerX = windowState.x + windowState.width / 2
    const centerY = windowState.y + windowState.height / 2
    return {
      left: `${centerX - 28}px`, // 56/2 = 28
      top: `${centerY - 28}px`
    }
  }
  return {
    left: `${windowState.x}px`,
    top: `${windowState.y}px`,
    width: `${windowState.width}px`,
    height: `${windowState.height}px`
  }
})

// 方法
const clearHistory = () => {
  messages.value = []
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
  if (!isMinimized.value) {
    // 展开时检查边界
    ensureVisible()
    scrollToBottom()
  }
}

const ensureVisible = () => {
  const maxX = window.innerWidth - windowState.width
  const maxY = window.innerHeight - windowState.height
  
  if (windowState.x > maxX) windowState.x = Math.max(0, maxX)
  if (windowState.y > maxY) windowState.y = Math.max(0, maxY)
  if (windowState.x < 0) windowState.x = 0
  if (windowState.y < 0) windowState.y = 0
}

const renderMarkdown = (content: string) => {
  return md.render(content)
}

const copyContent = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const handleEnterKey = (e: KeyboardEvent) => {
  if (e.shiftKey) return // Shift+Enter 换行
  sendMessage()
}

const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content })
  inputContent.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res: any = await sendAiChat(content)
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data })
    } else {
      // 接口返回错误，直接在对话框显示，不弹窗
      messages.value.push({ role: 'assistant', content: `服务暂时不可用，请稍后重试。（${res.msg || '未知错误'}）` })
    }
  } catch (error) {
    // 网络异常，直接在对话框显示
    messages.value.push({ role: 'assistant', content: '网络连接超时或中断，请检查网络后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const sendSuggestion = (content: string) => {
  inputContent.value = content
  sendMessage()
}

// 拖拽逻辑
const startDrag = (e: MouseEvent) => {
  if (e.target instanceof HTMLElement && (
    e.target.closest('.action-icon') || 
    e.target.closest('.ai-body') || 
    e.target.closest('.ai-footer') ||
    e.target.closest('.resize-handle')
  )) return
  
  e.preventDefault() // 防止选择文字
  isDragging.value = true
  // 记录点击位置相对于窗口左上角的偏移
  const rect = (isMinimized.value ? containerRef.value?.getBoundingClientRect() : null) || { left: windowState.x, top: windowState.y }
  dragOffset.x = e.clientX - (isMinimized.value ? rect.left : windowState.x)
  dragOffset.y = e.clientY - (isMinimized.value ? rect.top : windowState.y)
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  
  // 全局添加禁止选择样式
  document.body.style.userSelect = 'none'
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  e.preventDefault() // 进一步防止拖动时的默认行为
  
  let newX = e.clientX - dragOffset.x
  let newY = e.clientY - dragOffset.y
  
  // 简单边界限制
  if (!isMinimized.value) {
    windowState.x = newX
    windowState.y = newY
  } else {
    // 最小化时拖动 (newX, newY 是悬浮球左上角坐标)
    // 目标：保持中心对齐
    // 悬浮球中心 = newX + 28
    // 窗口中心 = windowState.x + windowState.width / 2
    // => windowState.x = newX + 28 - windowState.width / 2
    windowState.x = newX + 28 - windowState.width / 2
    windowState.y = newY + 28 - windowState.height / 2
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.userSelect = '' // 恢复选择
  if (!isMinimized.value) ensureVisible()
}

// Resize 逻辑
const startResize = (e: MouseEvent, direction: typeof resizeDirection.value) => {
  isResizing.value = true
  resizeStart.x = e.clientX
  resizeStart.y = e.clientY
  resizeStart.width = windowState.width
  resizeStart.height = windowState.height
  resizeStart.left = windowState.x
  resizeStart.top = windowState.y
  resizeDirection.value = direction

  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  
  const deltaX = e.clientX - resizeStart.x
  const deltaY = e.clientY - resizeStart.y

  const minWidth = 300
  const maxWidth = 800
  const minHeight = 400
  const maxHeight = 1000

  let newWidth = resizeStart.width
  let newHeight = resizeStart.height
  let newX = resizeStart.left
  let newY = resizeStart.top

  if (resizeDirection.value.includes('right')) {
    newWidth = resizeStart.width + deltaX
  }
  if (resizeDirection.value.includes('left')) {
    newWidth = resizeStart.width - deltaX
    newX = resizeStart.left + deltaX
  }
  if (resizeDirection.value.includes('bottom')) {
    newHeight = resizeStart.height + deltaY
  }
  if (resizeDirection.value.includes('top')) {
    newHeight = resizeStart.height - deltaY
    newY = resizeStart.top + deltaY
  }

  newWidth = Math.min(maxWidth, Math.max(minWidth, newWidth))
  newHeight = Math.min(maxHeight, Math.max(minHeight, newHeight))

  if (resizeDirection.value.includes('left')) {
    newX = resizeStart.left + (resizeStart.width - newWidth)
  }
  if (resizeDirection.value.includes('top')) {
    newY = resizeStart.top + (resizeStart.height - newHeight)
  }

  const maxX = window.innerWidth - newWidth
  const maxY = window.innerHeight - newHeight
  newX = Math.max(0, Math.min(maxX, newX))
  newY = Math.max(0, Math.min(maxY, newY))
  
  windowState.width = newWidth
  windowState.height = newHeight
  windowState.x = newX
  windowState.y = newY
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped lang="scss">
/* 变量定义 - 浅色主题 */
$bg-color-dark: #0f172a;
$bg-color-surface: #ffffff;
$color-primary: #4f46e5;
$color-accent: #0ea5e9;
$color-text-main: #0f172a;
$color-text-secondary: #64748b;
$border-glow: #e2e8f0;
$shadow-glow: rgba(79, 70, 229, 0.18);
$bg-message-ai: #ffffff;
$bg-input: #f8fafc;

.ai-assistant-container {
  position: fixed;
  z-index: 9999;
  font-family: 'Outfit', 'SF Pro Text', 'Segoe UI', sans-serif;
  color: $color-text-main;
  
  &.minimized {
    width: 56px !important;
    height: 56px !important;
    border-radius: 28px; /* 圆形 */
    cursor: pointer;
    /* 醒目橙色渐变 */
    background: linear-gradient(135deg, #4f46e5 0%, #0ea5e9 100%);
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 12px 30px rgba(79, 70, 229, 0.35);
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    
    &:hover {
      transform: scale(1.08);
      box-shadow: 0 16px 36px rgba(79, 70, 229, 0.45);
    }
  }

  .ai-expanded {
    background: linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%) !important;
    background-color: #f8fafc !important;
    border-radius: 26px;
    box-shadow: 0 24px 60px rgba(15, 23, 42, 0.25);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border: 1px solid rgba(226, 232, 240, 0.9);
    height: 100%;
    width: 100%;
    z-index: 10000;
  }
}

.ai-minimized {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  
  .ai-avatar-pulse {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .ai-logo-text {
      font-weight: 900;
      font-size: 20px;
      color: #FFFFFF;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
  
  .status-indicator {
    position: absolute;
    bottom: 6px;
    right: 6px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #67C23A; /* 绿色在线状态 */
    box-shadow: 0 0 2px #fff;
    /* animation: pulse-status 2s infinite; */
  }
  
  .unread-badge {
    position: absolute;
    top: -5px;
    right: -5px;
    background: #FF4D4F;
    color: #fff;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    border: 1px solid #fff;
  }
}

@keyframes pulse-status {
  0% { box-shadow: 0 0 0 0 rgba(0, 255, 224, 0.7); }
  70% { box-shadow: 0 0 0 5px rgba(0, 255, 224, 0); }
  100% { box-shadow: 0 0 0 0 rgba(0, 255, 224, 0); }
}

.ai-header {
  height: 64px;
  padding: 0 22px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: move;
  user-select: none;
  flex-shrink: 0;
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.6);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .ai-avatar-small {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(79, 70, 229, 0.15) 0%, rgba(14, 165, 233, 0.15) 100%);
      border: 1px solid rgba(79, 70, 229, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 6px 16px rgba(79, 70, 229, 0.2);
      
      .ai-logo-text-small {
        font-weight: 700;
        font-size: 12px;
        color: $color-primary;
      }
    }
    
    .header-info {
      display: flex;
      flex-direction: column;
      
      .header-title {
        font-weight: 700;
        font-size: 16px;
        color: $color-text-main;
      }
      
      .header-status {
        font-size: 12px;
        color: #10b981;
        display: flex;
        align-items: center;
        gap: 4px;
        
        .status-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background-color: #10b981;
        }
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;

    .action-icon {
      padding: 6px; /* 减小内边距，保持整体大小 */
      width: 32px; /* 固定宽度 */
      height: 32px; /* 固定高度 */
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px; /* 图标大小 */
      border-radius: 8px; /* 圆角 */
      cursor: pointer;
      color: #475569; /* 深色图标 */
      background: rgba(148, 163, 184, 0.18);
      transition: all 0.2s;
      border: 1px solid transparent; /* 预留边框位置 */
      
      &:hover {
        background: rgba(148, 163, 184, 0.3);
        color: $color-primary;
        transform: scale(1.05);
      }
      
      &.close-icon {
        &:hover {
          background: rgba(239, 68, 68, 0.12);
          color: #F56C6C;
          border-color: rgba(239, 68, 68, 0.3);
        }
      }
    }
  }
}

.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background: transparent; /* 透出底色 */
  scroll-behavior: smooth;

  /* 滚动条样式 */
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #DCDFE6;
    border-radius: 3px;
    
    &:hover {
      background: #C0C4CC;
    }
  }
}

.welcome-message {
  text-align: center;
  padding: 46px 0;
  
  .welcome-logo {
    width: 64px;
    height: 64px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, rgba(79, 70, 229, 0.12) 0%, rgba(14, 165, 233, 0.12) 100%);
    border: 1px solid rgba(79, 70, 229, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 24px rgba(79, 70, 229, 0.18);
    
    .ai-logo-text-large {
      font-size: 24px;
      font-weight: 900;
      color: $color-primary;
    }
  }

  h3 {
    margin: 0 0 10px;
    font-size: 20px;
    color: $color-text-main;
    font-weight: 600;
  }

  .welcome-desc {
    color: $color-text-secondary;
    font-size: 14px;
    margin-bottom: 30px;
    line-height: 1.6;
    max-width: 80%;
    margin-left: auto;
    margin-right: auto;
  }

  .suggestion-chips {
    display: flex;
    flex-direction: row; /* 改为水平排列 */
    flex-wrap: wrap; /* 允许换行 */
    justify-content: center; /* 居中 */
    gap: 10px; /* 减小间距 */
    
    .chip {
      background: rgba(255, 255, 255, 0.9);
      padding: 9px 14px; /* 减小内边距 */
      border-radius: 18px; /* 更圆润 */
      cursor: pointer;
      font-size: 13px;
      color: $color-text-main;
      text-align: center; /* 居中对齐 */
      transition: all 0.2s;
      border: 1px solid rgba(226, 232, 240, 0.9);
      display: inline-block; /* 改为 inline-block 或 flex */
      box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
      
      &:hover {
        background: #eef2ff;
        color: $color-primary;
        border-color: rgba(99, 102, 241, 0.35);
        transform: translateY(-2px); /* 向上浮动 */
      }
    }
  }
}

.message-row {
  display: flex;
  margin-bottom: 22px;
  gap: 14px;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-bubble {
      /* 用户消息：右侧对齐，科技蓝渐变背景，右侧有白色竖条装饰 */
      background: linear-gradient(135deg, #4f46e5 0%, #1d4ed8 100%);
      color: #fff;
      border-radius: 16px;
      border-right: 3px solid rgba(255, 255, 255, 0.4);
      box-shadow: 0 10px 24px rgba(37, 99, 235, 0.25);
    }
  }
  
  &.assistant {
    .message-bubble {
      /* AI消息：左侧对齐，浅灰色背景，左侧有科技蓝竖条装饰 */
      background: rgba(255, 255, 255, 0.95);
      color: $color-text-main;
      border-radius: 16px;
      border-left: 3px solid rgba(79, 70, 229, 0.6);
      border: 1px solid rgba(226, 232, 240, 0.9);
      box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08);
    }
    
    .message-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(79, 70, 229, 0.15) 0%, rgba(14, 165, 233, 0.15) 100%);
      border: 1px solid rgba(79, 70, 229, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 6px 14px rgba(79, 70, 229, 0.18);
      
      .ai-logo-text-small {
        font-size: 12px;
        font-weight: 700;
        color: $color-primary;
      }
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.message-content-wrapper {
      max-width: 85%;
      min-width: 0; /* 防止 flex 子项溢出 */
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      
      .user & {
        align-items: flex-end;
      }
    }

.message-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
  word-break: break-word;
  overflow-wrap: break-word;
  word-wrap: break-word;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
  max-width: 100%;
  
  &.loading {
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.95);
    border-left: 3px solid rgba(79, 70, 229, 0.6);
    border: 1px solid rgba(226, 232, 240, 0.9);
  }
}

.message-actions {
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
  
  .action-btn {
      font-size: 14px; /* 文字大小 */
      color: #475569;
      cursor: pointer;
      padding: 8px 16px; /* 增大内边距 */
      border-radius: 18px; /* 胶囊形状 */
      background: rgba(255, 255, 255, 0.95);
      border: 1px solid rgba(226, 232, 240, 0.9);
      box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
      display: inline-flex;
      align-items: center;
      gap: 6px; /* 图标文字间距 */
      transition: all 0.2s;
      
      &:hover {
        color: $color-primary;
        border-color: rgba(99, 102, 241, 0.35);
        background: #eef2ff;
        transform: translateY(-2px);
        box-shadow: 0 10px 22px rgba(79, 70, 229, 0.18);
      }
    }
}

.message-row:hover .message-actions {
  opacity: 1;
}

.ai-footer {
  padding: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  flex-shrink: 0;

  .input-wrapper {
    position: relative;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    border-radius: 14px;
    padding: 12px;
    border: 1px solid rgba(226, 232, 240, 0.9);
    transition: all 0.2s;
    box-shadow: 0 10px 22px rgba(15, 23, 42, 0.06);
    
    &:focus-within {
      border-color: rgba(99, 102, 241, 0.5);
      box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15);
      background: #ffffff;
    }
    
    .input-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;
    }
  }

  .ai-input {
    width: 100%;
    
    :deep(.el-textarea__inner) {
      box-shadow: none;
      background: transparent;
      padding: 0;
      resize: vertical; /* 允许垂直调整 */
      min-height: 40px;
      max-height: 200px;
      border: none;
      color: $color-text-main;
      font-family: inherit;
      
      &::placeholder {
        color: $color-text-secondary;
      }
      
      &:focus {
        box-shadow: none;
      }
      
      /* 自定义滚动条，使其不突兀 */
      &::-webkit-scrollbar {
        width: 4px;
      }
      &::-webkit-scrollbar-thumb {
        background: #DCDFE6;
        border-radius: 2px;
      }
    }
  }

  .send-btn {
    width: 40px;
    height: 40px;
    min-height: 40px;
    border: none;
    background: rgba(148, 163, 184, 0.2);
    color: $color-text-secondary;
    transition: all 0.2s;
    
    &.is-active {
      background: linear-gradient(135deg, #4f46e5 0%, #0ea5e9 100%);
      color: #fff;
      box-shadow: 0 10px 24px rgba(79, 70, 229, 0.35);
      
      &:hover {
        background: linear-gradient(135deg, #6366f1 0%, #38bdf8 100%);
      }
    }
  }
}

.resize-indicator {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  font-size: 12px;
  z-index: 11;
}

.resize-handle {
  position: absolute;
  z-index: 20; /* 提高层级，确保易于点击 */
  background: transparent; /* 完全透明，去除方框视觉 */
}

/* 移除 hover 显示背景的逻辑，仅保留光标变化 */
/* .ai-expanded:hover .resize-handle, */
/* .ai-assistant-container.is-resizing .resize-handle { */
/*   opacity: 1; */
/* } */

.resize-handle.corner {
  width: 16px; /* 增大点击区域 */
  height: 16px;
}

.resize-handle.edge {
  background: transparent;
}

.resize-handle.corner.top-left {
  top: 0;
  left: 0;
  cursor: nwse-resize;
}

.resize-handle.corner.top-right {
  top: 0;
  right: 0;
  cursor: nesw-resize;
}

.resize-handle.corner.bottom-left {
  bottom: 0;
  left: 0;
  cursor: nesw-resize;
}

.resize-handle.corner.bottom-right {
  bottom: 0;
  right: 0;
  cursor: nwse-resize;
}

.resize-handle.edge.top {
  top: 0;
  left: 12px;
  right: 12px;
  height: 6px;
  cursor: ns-resize;
}

.resize-handle.edge.right {
  top: 12px;
  right: 0;
  bottom: 12px;
  width: 6px;
  cursor: ew-resize;
}

.resize-handle.edge.bottom {
  bottom: 0;
  left: 12px;
  right: 12px;
  height: 6px;
  cursor: ns-resize;
}

.resize-handle.edge.left {
  top: 12px;
  left: 0;
  bottom: 12px;
  width: 6px;
  cursor: ew-resize;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  
  span {
    width: 8px;
    height: 8px;
    background: $color-primary;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* Markdown 样式微调 - 浅色模式适配 */
:deep(.markdown-body) {
  background: transparent !important;
  font-size: 14px;
  color: $color-text-main;
  
  p {
    color: inherit;
    line-height: 1.6;
    margin-bottom: 12px;
  }
  
  pre {
      background: #F5F7FA;
      border: 1px solid #E4E7ED;
      border-radius: 8px;
      overflow-x: auto;
      max-width: 100%;
      margin: 12px 0;
      white-space: pre-wrap; /* 允许换行 */
      word-wrap: break-word; /* 允许长单词换行 */
    }
  
  code {
    background: rgba(58, 123, 255, 0.1);
    color: $color-primary;
    border-radius: 4px;
    padding: 2px 4px;
    font-family: 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 85%;
  }

  pre code {
    background: transparent;
    padding: 0;
    color: inherit;
    font-size: 100%;
  }
  
  img {
    max-width: 100%;
    border-radius: 8px;
    margin: 8px 0;
  }

  table {
    display: block;
    width: 100%;
    overflow-x: auto;
    border-collapse: collapse;
    margin: 12px 0;
    
    th, td {
      border: 1px solid #EBEEF5;
      padding: 8px 12px;
    }
    
    th {
      background: #F5F7FA;
      font-weight: 600;
    }
  }
  
  a {
    color: $color-primary;
    text-decoration: none;
    border-bottom: 1px solid $color-primary;
    
    &:hover {
      opacity: 0.8;
    }
  }
  
  ul, ol {
    padding-left: 20px;
    margin-bottom: 12px;
  }
}
</style>

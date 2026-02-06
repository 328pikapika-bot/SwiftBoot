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
            <div class="message-bubble" :class="{ loading: msg.role === 'assistant' && !msg.content && loading }">
              <div v-if="msg.role === 'assistant' && !msg.content && loading" class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div v-else-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
            <!-- 复制按钮 (仅 AI 消息显示且内容不为空时) -->
            <div v-if="msg.role === 'assistant' && msg.content" class="message-actions">
              <el-tooltip content="复制内容" placement="top" :show-after="500">
                <div class="action-btn" @click="copyContent(msg.content)">
                  <el-icon><CopyDocument /></el-icon> 复制全文
                </div>
              </el-tooltip>
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
                  :loading="false" 
                  :disabled="!inputContent.trim() || loading"
                  circle
                >
                  <el-icon><Position /></el-icon>
                </el-button>
             </el-tooltip>
             <el-tooltip content="停止回答" placement="top">
               <el-button 
                 v-if="loading"
                 class="stop-btn" 
                 type="danger" 
                 plain 
                 @click="stopStreaming" 
                 circle
               >
                 <el-icon><Close /></el-icon>
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
import { ref, reactive, computed, nextTick, onUnmounted, watch } from 'vue'
import { Minus, Position, CopyDocument, Close, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { sendAiChat } from '@/api/index'
import { useUserStore } from '@/stores/user'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css/github-markdown.css'
import request from '@/utils/request'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true // 开启换行符转换
})

// 状态
const isMinimized = ref(true)
const loading = ref(false)
const currentStream = ref<{ abort: () => void } | null>(null)
const inputContent = ref('')
const messages = ref<{ role: 'user' | 'assistant', content: string }[]>([])
const containerRef = ref<HTMLElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)

const userStore = useUserStore()

// 加载历史记录 (从后端 Redis 获取)
const loadHistory = async () => {
  if (!userStore.token) {
      messages.value = []
      return
  }
  try {
    const res: any = await request.get('/system/ai/history')
    if (res.code === 200 && res.data) {
        messages.value = res.data
        scrollToBottom()
    } else {
        messages.value = []
    }
  } catch (e) {
    console.error('Failed to load chat history:', e)
    messages.value = []
  }
}

// 保存历史记录 (已移至后端 Redis，前端无需存储)
const saveHistory = () => {
  // No-op
}

// 监听用户ID变化，重新加载历史
watch(() => userStore.userInfo?.id, () => {
    loadHistory()
})

// 初始化时加载
loadHistory()

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
const clearHistory = async () => {
  // TODO: 调用后端接口清除 Redis 历史 (当前仅清空前端显示)
  messages.value = []
}

const toggleMinimize = (e?: MouseEvent) => {
  if (e) {
    e.stopPropagation() // 阻止事件冒泡，防止触发其他点击事件
  }
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

const streamAiChat = async (content: string, history: any[], onChunk: (chunk: string) => void) => {
  const userStore = useUserStore()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  if (userStore.token) {
    headers['Authorization'] = userStore.token
  }
  const controller = new AbortController()
  currentStream.value = { abort: () => controller.abort() }
  const response = await fetch('/api/system/ai/chat/stream', {
    method: 'POST',
    headers,
    body: JSON.stringify({ content, history }),
    signal: controller.signal
  })
  if (!response.ok || !response.body) {
    throw new Error('stream_failed')
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue
        const data = trimmed.slice(5).trim()
        if (!data) continue
        if (data === '[DONE]') return
        try {
          const json = JSON.parse(data)
          if (json.content) {
            onChunk(json.content)
          }
        } catch (e) {
          onChunk(data)
        }
      }
    }
  } catch (err: any) {
    if (err && err.name === 'AbortError') {
      throw new Error('stream_aborted')
    }
    throw err
  } finally {
    currentStream.value = null
  }
}

const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content })
  inputContent.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const assistantMessage = reactive({ role: 'assistant', content: '' })
    messages.value.push(assistantMessage as any)
    
    // 平滑打字机效果相关变量
    let pendingText = ''
    let isTyping = false
    let isStreamFinished = false
    
    // 启动打字机循环
    const typeLoop = () => {
      if (pendingText.length > 0) {
        // 动态调整打字速度：缓冲区越长，打字越快，避免积压太多
        const speed = pendingText.length > 50 ? 5 : pendingText.length > 20 ? 2 : 1
        const chunk = pendingText.slice(0, speed)
        pendingText = pendingText.slice(speed)
        assistantMessage.content += chunk
        scrollToBottom()
      }
      
      if (isStreamFinished && pendingText.length === 0) {
        isTyping = false
        loading.value = false
        return
      }
      
      requestAnimationFrame(typeLoop)
    }

    try {
      // 提取历史记录 (不再需要前端传递，后端从 Redis 获取)
      const history: any[] = [] // 空数组，后端会自动从 Redis 加载
      
      await streamAiChat(content, history, (chunk) => {
        pendingText += chunk
        if (!isTyping) {
          isTyping = true
          typeLoop()
        }
      })
      isStreamFinished = true
      
      if (!assistantMessage.content && !pendingText) {
        assistantMessage.content = '服务暂时不可用，请稍后重试。'
        loading.value = false
      }
      // 对话结束，保存历史
      saveHistory()
    } catch (streamError: any) {
      if (streamError?.message === 'stream_aborted') {
        pendingText = ''
        isStreamFinished = true
        loading.value = false
      } else {
        // 如果流式失败，降级为非流式
        isStreamFinished = true // 停止打字机等待
        const res: any = await sendAiChat(content)
        if (res.code === 200) {
          assistantMessage.content = res.data
        } else {
          assistantMessage.content = `服务暂时不可用，请稍后重试。（${res.msg || '未知错误'}）`
        }
        loading.value = false
        // 对话结束，保存历史
        saveHistory()
      }
    }
  } catch (error) {
    // 网络异常，直接在对话框显示
    messages.value.push({ role: 'assistant', content: '网络连接超时或中断，请检查网络后重试。' })
    loading.value = false
    // 异常也保存，防止丢失用户输入
    saveHistory()
  } finally {
    // loading 状态在 typeLoop 结束时处理，或者异常时处理
    // 此处不做统一处理，因为流式需要在打字完成后才算结束
  }
}

const stopStreaming = () => {
  if (currentStream.value) {
    currentStream.value.abort()
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
      align-items: center;
      gap: 10px;
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
      color: #94a3b8;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      
      &:not(:disabled) {
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }
        
        &.is-active {
          background: linear-gradient(135deg, #4f46e5 0%, #0ea5e9 100%);
          color: #ffffff;
        }
      }
      
      &:disabled {
        cursor: not-allowed;
        opacity: 0.6;
      }
    }

  .stop-btn {
      width: 40px;
      height: 40px;
      min-height: 40px;
      border-radius: 12px;
      border-color: rgba(239, 68, 68, 0.35);
      box-shadow: 0 6px 16px rgba(239, 68, 68, 0.18);
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 22px rgba(239, 68, 68, 0.25);
      }
    }
  }

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  
  span {
    width: 6px;
    height: 6px;
    background: #4f46e5;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* 调整 Markdown 样式 */
.markdown-body {
  background: transparent !important;
  font-family: inherit !important;
  font-size: 15px !important;
  line-height: 1.7 !important;
  color: #334155 !important;
  
  p {
    margin-bottom: 12px;
    &:last-child { margin-bottom: 0; }
  }
  
  pre {
    background: #1e293b !important;
    border-radius: 12px !important;
    border: 1px solid #334155;
    margin: 16px 0 !important;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    
    code {
      color: #e2e8f0 !important;
      font-family: 'JetBrains Mono', 'Fira Code', monospace !important;
      font-size: 13px !important;
    }
  }
  
  code {
    background: rgba(99, 102, 241, 0.1) !important;
    color: #4f46e5 !important;
    padding: 2px 6px !important;
    border-radius: 6px !important;
    font-family: 'JetBrains Mono', monospace !important;
    font-size: 0.9em !important;
  }
  
  ul, ol {
    padding-left: 24px !important;
    margin-bottom: 12px !important;
    
    li {
      margin-bottom: 6px !important;
      &::marker { color: #64748b; }
    }
  }
  
  h1, h2, h3, h4 {
    color: #0f172a !important;
    font-weight: 700 !important;
    margin-top: 24px !important;
    margin-bottom: 16px !important;
    padding-bottom: 8px !important;
    border-bottom: 1px solid #e2e8f0 !important;
    
    &:first-child { margin-top: 0 !important; }
  }
  
  blockquote {
    border-left: 4px solid #4f46e5 !important;
    background: #f8fafc !important;
    padding: 12px 16px !important;
    color: #475569 !important;
    border-radius: 0 8px 8px 0 !important;
    margin: 16px 0 !important;
  }
  
  a {
    color: #0ea5e9 !important;
    text-decoration: none !important;
    border-bottom: 1px dashed #0ea5e9;
    
    &:hover {
      color: #0284c7 !important;
      border-bottom-style: solid;
    }
  }
  
  table {
    display: block;
    width: 100%;
    overflow: auto;
    margin: 16px 0;
    border-spacing: 0;
    border-collapse: collapse;
    
    th {
      font-weight: 600;
      background: #f1f5f9;
      color: #0f172a;
    }
    
    td, th {
      padding: 8px 16px;
      border: 1px solid #e2e8f0;
    }
    
    tr:nth-child(2n) {
      background: #f8fafc;
    }
  }
}

.resize-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(15, 23, 42, 0.8);
  color: #fff;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  pointer-events: none;
  z-index: 10001;
  backdrop-filter: blur(4px);
}

.resize-handle {
  position: absolute;
  z-index: 10000;
  
  &.edge {
    &.top { top: -4px; left: 0; right: 0; height: 10px; cursor: ns-resize; }
    &.bottom { bottom: -4px; left: 0; right: 0; height: 10px; cursor: ns-resize; }
    &.left { left: -4px; top: 0; bottom: 0; width: 10px; cursor: ew-resize; }
    &.right { right: -4px; top: 0; bottom: 0; width: 10px; cursor: ew-resize; }
  }
  
  &.corner {
    width: 16px;
    height: 16px;
    
    &.top-left { top: -6px; left: -6px; cursor: nwse-resize; }
    &.top-right { top: -6px; right: -6px; cursor: nesw-resize; }
    &.bottom-left { bottom: -6px; left: -6px; cursor: nesw-resize; }
    &.bottom-right { bottom: -6px; right: -6px; cursor: nwse-resize; }
  }
}
::v-deep(.markdown-body) {
  background-color: transparent;
  font-size: 14px;
  line-height: 1.6;
  color: inherit;
  
  p {
    margin-bottom: 10px;
    white-space: pre-wrap; /* 保留换行 */
  }
  
  pre {
    background-color: #f6f8fa;
    border-radius: 8px;
    padding: 12px;
    margin: 10px 0;
    overflow-x: auto;
  }
  
  code {
    font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
    font-size: 12px;
  }
  
  ul, ol {
    padding-left: 20px;
    margin-bottom: 10px;
  }
}
</style>

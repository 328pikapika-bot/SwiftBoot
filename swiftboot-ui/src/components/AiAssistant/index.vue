<template>
  <div
    class="ai-assistant-container"
    :class="{ minimized: isMinimized, 'is-resizing': isResizing }"
    :style="containerStyle"
    ref="containerRef"
  >
    <!-- 最小化状态 (悬浮球) -->
    <div v-if="isMinimized" class="ai-minimized-new group" @mousedown="startDrag" @dblclick.stop="openAssistant">
      <div class="absolute inset-0 rounded-full bg-primary/20 ai-pulse pointer-events-none -z-10 scale-150"></div>
      <div class="absolute inset-0 rounded-full bg-primary/10 ai-pulse pointer-events-none -z-10 scale-125" style="animation-delay: 1.5s;"></div>
      
      <div class="relative w-16 h-16 bg-gradient-to-br from-primary to-indigo-600 text-white rounded-full shadow-lg flex items-center justify-center hover:scale-110 transition-all duration-300 backdrop-blur-sm cursor-grab active:cursor-grabbing select-none">
        <span class="material-symbols-outlined text-4xl group-hover:drop-shadow-glow transition-all">neurology</span>
        <div class="absolute top-0 right-0 w-4 h-4 bg-emerald-400 rounded-full border-2 border-white shadow-sm"></div>
      </div>

      <span class="absolute right-full mr-4 top-1/2 -translate-y-1/2 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 px-3 py-1.5 rounded-lg text-sm font-medium shadow-xl opacity-0 group-hover:opacity-100 translate-x-2 group-hover:translate-x-0 transition-all pointer-events-none whitespace-nowrap border border-slate-100 dark:border-slate-700">
        双击进入智能会话
      </span>
    </div>

    <!-- 展开状态 (参考设计图) -->
    <div v-else class="ai-window-card">
      <!-- Header -->
      <header class="ai-window-header" @mousedown="startDrag" @dblclick="toggleMinimize">
        <div class="header-left">
          <div class="brand-icon">
            <span class="material-symbols-outlined text-primary">auto_awesome</span>
          </div>
          <div class="brand-info">
            <h2 class="title">SwiftBoot 智能会话</h2>
            <div class="status-indicator">
              <span class="pulse-dot"></span>
              <span class="status-text">本地 RAG 索引已就绪</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="model-tag">模型: {{ currentModelName }}</div>
          <div class="header-actions">
            <button class="icon-btn" @click.stop="clearHistory" title="历史记录"><span class="material-symbols-outlined">history</span></button>
            <button class="icon-btn" @click.stop="toggleMinimize" title="最小化"><span class="material-symbols-outlined">remove</span></button>
            <button class="icon-btn close" @click.stop="toggleMinimize" title="关闭"><span class="material-symbols-outlined">close</span></button>
          </div>
        </div>
      </header>

      <div class="ai-window-content">
        <!-- Chat Area -->
        <div class="chat-main">
          <div class="chat-messages custom-scrollbar" ref="messagesRef">
            <div v-if="messages.length === 0" class="welcome-empty">
              <div class="welcome-logo">
                <span class="material-symbols-outlined">neurology</span>
              </div>
              <h3>有什么可以帮您？</h3>
              <div class="suggestion-list">
                <div v-for="s in suggestions" :key="s" class="suggestion-item" @click="sendSuggestion(s)">
                  {{ s }}
                </div>
              </div>
            </div>

            <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
              <div class="msg-avatar" v-if="msg.role === 'assistant'">
                <span class="material-symbols-outlined">auto_awesome</span>
              </div>
              <div class="msg-content-box">
                <template v-if="msg.role === 'assistant'">
                  <!-- 思考过程区块 -->
                  <div v-if="parseMessage(msg.content).thought" 
                       class="ds-thought-container" 
                       :class="{ thinking: parseMessage(msg.content).isThinking }">
                    <div class="ds-thought-header">
                      <span class="material-symbols-outlined ds-thought-icon">psychology</span>
                      <span class="ds-thought-title">
                        {{ parseMessage(msg.content).isThinking ? '正在思考...' : '已完成深度思考' }}
                      </span>
                      <button v-if="!parseMessage(msg.content).isThinking" 
                              class="mini-copy-btn" 
                              @click="copyPart(parseMessage(msg.content).rawThought, '思考')"
                              title="复制思考过程">
                        <span class="material-symbols-outlined">content_copy</span>
                      </button>
                    </div>
                    <div class="ds-thought-body markdown-content" v-html="parseMessage(msg.content).thought"></div>
                  </div>

                  <!-- 回答区块 -->
                  <div class="msg-bubble ai-bubble">
                    <div v-if="parseMessage(msg.content).answer" class="ds-answer-body markdown-content" v-html="parseMessage(msg.content).answer"></div>
                    <div v-if="!msg.content && loading" class="loading-shimmer">
                      <div class="shimmer-line w-3/4"></div>
                      <div class="shimmer-line w-1/2"></div>
                    </div>
                    
                    <!-- 独立复制回答按钮 -->
                    <div v-if="parseMessage(msg.content).answer && !loading" class="msg-footer">
                      <button class="footer-action" @click="copyPart(parseMessage(msg.content).rawAnswer, '回答')">
                        <span class="material-symbols-outlined">content_copy</span> 复制回答
                      </button>
                    </div>
                  </div>
                </template>

                <template v-else>
                  <div class="msg-bubble user-bubble">
                    {{ msg.content }}
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- Input Area -->
          <footer class="chat-footer">
            <div class="input-wrapper-new">
              <textarea
                v-model="inputContent"
                placeholder="询问关于您项目的问题..."
                @keydown.enter.prevent="handleEnterKey"
                :disabled="loading"
                rows="2"
                class="custom-textarea"
              ></textarea>
              <div class="input-toolbar">
                <div class="toolbar-left">
                  <!-- 移除原有的三个功能图标 -->
                </div>
                <button 
                  class="send-btn-v2" 
                  :class="{ active: inputContent.trim() && !loading }"
                  @click="sendMessage"
                  :disabled="!inputContent.trim() || loading"
                >
                  <span>发送</span>
                  <span class="material-symbols-outlined">send</span>
                </button>
              </div>
            </div>
            <div class="footer-note">SWIFTBOOT 智能全栈引擎 · RAG 增强型 AI</div>
          </footer>
        </div>

        <!-- Sidebar -->
        <aside class="chat-sidebar" v-if="windowState.width > 600">
          <div class="sidebar-section">
            <h3 class="section-title">
              <span class="material-symbols-outlined">account_tree</span>
              当前知识环境
            </h3>
            <div class="file-tree-mini">
              <div class="tree-node parent">
                <span class="material-symbols-outlined text-amber-400">folder_open</span>
                <span>swiftboot-backend</span>
              </div>
              <div class="tree-node child">
                <span class="material-symbols-outlined text-amber-400">folder</span>
                <span>swiftboot-admin</span>
              </div>
              <div class="tree-node child ml-10">
                <span class="material-symbols-outlined text-blue-400">description</span>
                <span>SysAiController.java</span>
              </div>
              <div class="tree-node parent mt-2">
                <span class="material-symbols-outlined text-amber-400">folder</span>
                <span>swiftboot-ui</span>
              </div>
              <div class="tree-node child">
                <span class="material-symbols-outlined text-amber-400">folder</span>
                <span>src/components/AiAssistant</span>
              </div>
              <div class="tree-node parent mt-2">
                <span class="material-symbols-outlined text-slate-400">settings</span>
                <span>pom.xml</span>
              </div>
            </div>
          </div>

          <div class="sidebar-section">
            <h3 class="section-title">RAG 向量空间</h3>
            <div class="vector-space-box">
              <div class="grid-overlay"></div>
              <div class="vector-dots">
                <div v-for="i in 8" :key="i" class="v-dot" :style="randomDotPos()"></div>
                <div class="v-dot center-dot"></div>
              </div>
              <div class="scan-line"></div>
              <div class="space-status">
                <span class="dot-pulse"></span>
                <span>实时检索中...</span>
              </div>
            </div>
            <div class="space-meta">
              <span>索引总量: {{ indexStatsSummary.total }}</span>
              <span :title="indexStatsSummary.detail">{{ indexStatsSummary.detail }}</span>
            </div>
          </div>

          <div class="sidebar-footer">
            <button class="refresh-btn" @click="handleRefreshIndex">
              <span class="material-symbols-outlined">refresh</span>
              查看索引统计
            </button>
          </div>
        </aside>
      </div>

      <!-- Resize Handles -->
      <div class="resize-handle nw" @mousedown.stop="startResize($event, 'top-left')"></div>
      <div class="resize-handle ne" @mousedown.stop="startResize($event, 'top-right')"></div>
      <div class="resize-handle sw" @mousedown.stop="startResize($event, 'bottom-left')"></div>
      <div class="resize-handle se" @mousedown.stop="startResize($event, 'bottom-right')"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { Minus, Position, CopyDocument, Close, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/tokyo-night-dark.css' // 采用更高级的深色主题
import 'github-markdown-css/github-markdown.css'
import request from '@/utils/request'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs custom-code-block"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs custom-code-block"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
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

const suggestions = [
  '请详细介绍 SwiftBoot 项目的整体架构',
  '分析一下用户登录和 JWT 认证的完整流程',
  '如何使用代码生成器快速创建一个 CRUD 模块？',
  '请说明核心表 sys_user 和 sys_role 的关系'
]

const currentModelName = ref('加载中...')
const knowledgeStats = ref<{ total_chunks?: number; file_types?: Record<string, number> }>({})

// 加载历史记录和配置
const initChat = async () => {
  if (!userStore.token) {
      messages.value = []
      return
  }
  try {
    // 并行请求历史记录和配置
    const [historyRes, configRes] = await Promise.all([
      request.get('/system/ai/history'),
      request.get('/system/ai/config')
    ])
    
    if (historyRes.code === 200 && historyRes.data) {
        messages.value = historyRes.data
        scrollToBottom(true)
    }
    
    if (configRes.code === 200 && configRes.data) {
        currentModelName.value = configRes.data.model || '未配置模型'
    }
  } catch (e) {
    console.error('Failed to init chat:', e)
  }
}

watch(() => userStore.userInfo?.userId, () => initChat())
initChat()

// 窗口尺寸和位置 (进一步加宽默认尺寸)
const windowState = reactive({
  x: window.innerWidth - 1350, 
  y: window.innerHeight / 2 - 450,
  width: 1300,
  height: 850
})

const isDragging = ref(false)
const isResizing = ref(false)
const dragOffset = reactive({ x: 0, y: 0 })
const resizeStart = reactive({ x: 0, y: 0, width: 0, height: 0, left: 0, top: 0 })
const resizeDirection = ref('')

const containerStyle = computed(() => {
  if (isMinimized.value) {
    const centerX = windowState.x + windowState.width / 2
    const centerY = windowState.y + windowState.height / 2
    return { left: `${centerX - 32}px`, top: `${centerY - 32}px` }
  }
  return {
    left: `${windowState.x}px`,
    top: `${windowState.y}px`,
    width: `${windowState.width}px`,
    height: `${windowState.height}px`
  }
})

const randomDotPos = () => {
  return {
    top: `${Math.random() * 80 + 10}%`,
    left: `${Math.random() * 80 + 10}%`
  }
}

const formatCount = (value: number) => Number(value || 0).toLocaleString()

const getAggregateLabel = (key: string) => {
  const map: Record<string, string> = {
    java: 'Java',
    py: 'Python',
    vue: 'Vue',
    ts: 'TypeScript',
    js: 'JavaScript',
    md: 'Markdown',
    doc: 'Doc',
    txt: 'Text',
    sql: 'SQL',
    xml: 'XML'
  }
  return map[key] || key.toUpperCase()
}

const buildKnowledgeStatsSummary = () => {
  const fileTypes = knowledgeStats.value.file_types || {}
  const entries = Object.entries(fileTypes)
    .map(([key, value]) => `${getAggregateLabel(key)} ${formatCount(Number(value || 0))}`)
    .sort((a, b) => a.localeCompare(b, 'zh-CN'))

  const total = formatCount(Number(knowledgeStats.value.total_chunks || 0))
  return {
    total,
    detail: entries.length ? entries.join(' / ') : '等待索引统计同步'
  }
}

const indexStatsSummary = computed(() => buildKnowledgeStatsSummary())

const fetchKnowledgeStats = async () => {
  try {
    const res: any = await request.get('/system/ai/stats')
    if (res.code === 200 && res.data) {
      knowledgeStats.value = {
        total_chunks: Number(res.data.total_chunks || res.data.knowledge_count || 0),
        file_types: res.data.file_types || {}
      }
    }
  } catch (error) {
    console.error('Failed to fetch knowledge stats:', error)
  }
}

const openAssistant = () => {
  isMinimized.value = false
  ensureVisible()
  scrollToBottom(true)
}

// 【工具函数】清除幻觉标签
const cleanHallucinationTags = (text: string): string => {
  if (!text) return ''
  let cleaned = text
    // DSML标签（全角竖线，各种变体）
    .replace(/<?\|?[｜]?DSML[｜|]?[\s\S]*?[｜|]?\/?DSML[｜|]?>?/gi, '')
    // function_calls标签（完整闭合，带或不带尖括号）
    .replace(/<?function_calls>?[\s\S]*?<?\/?function_calls>?/gi, '')
    // invoke标签（完整闭合，带或不带尖括号）
    .replace(/<?invoke[^>]*>?[\s\S]*?<?\/?invoke>?/gi, '')
    // parameter标签（完整闭合，带或不带尖括号）
    .replace(/<?parameter[^>]*>?[\s\S]*?<?\/?parameter>?/gi, '')
    // 未闭合的function_calls标签
    .replace(/<?function_calls>?[\s\S]*$/gi, '')
    // 未闭合的invoke标签
    .replace(/<?invoke[\s\S]*$/gi, '')
    // 未闭合的parameter标签
    .replace(/<?parameter[\s\S]*$/gi, '')
    // 残留的无尖括号标签文本
    .replace(/function_calls>/gi, '')
    .replace(/invokename=/gi, '')
    .replace(/parametername=/gi, '')
    .replace(/string="true">/gi, '')
    // 清除残留的标签片段和属性
    .replace(/name="[^"]*"/gi, '')
    .replace(/string="[^"]*"/gi, '')
    .replace(/<?\/?function_calls>?/gi, '')
    .replace(/<?\/?invoke>?/gi, '')
    .replace(/<?\/?parameter>?/gi, '')
  
  // 直接返回清洗后的内容，保留可能存在的空格和正常标点
  return cleaned
}

const parseMessage = (content: string) => {
  if (!content) return { thought: '', answer: '', isThinking: false }
  
  let thoughtContent = ''
  const thoughtMatch = content.match(/<thought>([\s\S]*?)<\/thought>/i)
  const thoughtStartMatch = content.match(/<thought>([\s\S]*)$/i)
  
  let cleanContent = content
  let isThinking = false
  
  if (thoughtMatch) {
    thoughtContent = thoughtMatch[1].trim()
    cleanContent = content.replace(thoughtMatch[0], '').trim()
  } else if (thoughtStartMatch) {
    thoughtContent = thoughtStartMatch[1].trim()
    cleanContent = content.substring(0, thoughtStartMatch.index).trim()
    isThinking = true
  }
  
  // 【前端强化过滤】：对思考内容和回答内容都进行清洗
  thoughtContent = cleanHallucinationTags(thoughtContent)
  cleanContent = cleanHallucinationTags(cleanContent)
  
  return {
    thought: thoughtContent ? md.render(thoughtContent) : '',
    answer: cleanContent ? md.render(cleanContent) : '',
    isThinking,
    rawThought: thoughtContent,
    rawAnswer: cleanContent
  }
}

const scrollToBottom = (force = false) => {
  nextTick(() => {
    if (messagesRef.value) {
      const { scrollTop, scrollHeight, clientHeight } = messagesRef.value
      const isNearBottom = scrollHeight - scrollTop - clientHeight < 150
      if (force || isNearBottom) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    }
  })
}

const handleEnterKey = (e: KeyboardEvent) => {
  if (e.shiftKey) return
  sendMessage()
}

const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content })
  inputContent.value = ''
  loading.value = true
  scrollToBottom(true)

  const assistantMessage = reactive({ role: 'assistant', content: '' })
  messages.value.push(assistantMessage as any)
  
  let pendingText = ''
  let isTyping = false
  let isStreamFinished = false
  
  // 流式缓冲区，用于检测跨chunk的幻觉标签
  let streamBuffer = ''
  
  // 检测是否包含可能的幻觉标签前缀
  const hasPossibleHallucinationPrefix = (text: string): boolean => {
    const lower = text.toLowerCase()
    return lower.endsWith('<') || 
           lower.endsWith('</') ||
           lower.endsWith('<f') ||
           lower.endsWith('<fu') ||
           lower.endsWith('<fun') ||
           lower.endsWith('<func') ||
           lower.includes('<function') ||
           lower.includes('<invoke') ||
           lower.includes('<param')
  }
  
  const typeLoop = () => {
    if (pendingText.length > 0) {
      // 动态速度：更丝滑
      const speed = Math.max(1, Math.floor(pendingText.length / 8))
      const chunk = pendingText.slice(0, speed)
      pendingText = pendingText.slice(speed)
      
      // 将chunk加入缓冲区
      streamBuffer += chunk
      
      // 检测并清洗幻觉标签
      let safeContent = cleanHallucinationTags(streamBuffer)
      
      // 如果缓冲区末尾可能正在形成幻觉标签，保留等待
      if (hasPossibleHallucinationPrefix(safeContent) && streamBuffer.length < 100) {
        // 保持缓冲，不输出
      } else {
        // 输出安全内容
        if (safeContent) {
          assistantMessage.content += safeContent
        }
        streamBuffer = '' // 清空缓冲区
      }
      scrollToBottom()
    }
    if (isStreamFinished && pendingText.length === 0) {
      // 流结束时，输出缓冲区剩余内容（清洗后）
      if (streamBuffer) {
        assistantMessage.content += cleanHallucinationTags(streamBuffer)
      }
      isTyping = false
      loading.value = false
      return
    }
    requestAnimationFrame(typeLoop)
  }

  try {
    const controller = new AbortController()
    currentStream.value = { abort: () => controller.abort() }
    
    const response = await fetch('/api/system/ai/chat/stream', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json', 
        'Accept': 'text/event-stream',
        'Authorization': userStore.token || '' 
      },
      body: JSON.stringify({ content, history: [] }),
      signal: controller.signal
    })

    if (!response.ok) throw new Error('Network error')
    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    if (reader) {
      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          if (!line.startsWith('data:')) continue
          const data = line.slice(5).trim()
          if (data === '[DONE]') break
          try {
            const json = JSON.parse(data)
            if (json.content) {
              pendingText += json.content
              if (!isTyping) { isTyping = true; typeLoop() }
            }
          } catch (e) {}
        }
      }
    }
    isStreamFinished = true
  } catch (error: any) {
    isStreamFinished = true
    if (error.name !== 'AbortError') {
      assistantMessage.content += `\n\n[连接异常: ${error.message}]`
    }
    loading.value = false
  } finally {
    currentStream.value = null
  }
}

const copyPart = async (text: string, type: '思考' | '回答') => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`已复制${type}内容`)
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const clearHistory = async () => {
  try {
    await request.delete('/system/ai/history/clean')
    messages.value = []
    ElMessage.success('历史记录已清除')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
  if (!isMinimized.value) {
    ensureVisible()
    scrollToBottom()
  }
}

const ensureVisible = () => {
  const maxX = window.innerWidth - windowState.width
  const maxY = window.innerHeight - windowState.height
  windowState.x = Math.max(0, Math.min(maxX, windowState.x))
  windowState.y = Math.max(0, Math.min(maxY, windowState.y))
}

const startDrag = (e: MouseEvent) => {
  if (e.target instanceof HTMLElement && (
    e.target.closest('.header-actions') || 
    e.target.closest('.chat-messages') || 
    e.target.closest('.chat-footer') ||
    e.target.closest('.chat-sidebar') ||
    e.target.closest('.resize-handle')
  )) return
  
  isDragging.value = true
  const rect = containerRef.value?.getBoundingClientRect()
  dragOffset.x = e.clientX - (isMinimized.value ? (rect?.left || 0) : windowState.x)
  dragOffset.y = e.clientY - (isMinimized.value ? (rect?.top || 0) : windowState.y)
  
  const onDrag = (e: MouseEvent) => {
    if (!isDragging.value) return
    let newX = e.clientX - dragOffset.x
    let newY = e.clientY - dragOffset.y
    if (!isMinimized.value) {
      windowState.x = newX
      windowState.y = newY
    } else {
      windowState.x = newX + 32 - windowState.width / 2
      windowState.y = newY + 32 - windowState.height / 2
    }
  }
  
  const stopDrag = () => {
    isDragging.value = false
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', stopDrag)
    if (!isMinimized.value) ensureVisible()
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const startResize = (e: MouseEvent, direction: string) => {
  isResizing.value = true
  resizeStart.x = e.clientX
  resizeStart.y = e.clientY
  resizeStart.width = windowState.width
  resizeStart.height = windowState.height
  resizeStart.left = windowState.x
  resizeStart.top = windowState.y
  resizeDirection.value = direction

  const onResize = (e: MouseEvent) => {
    if (!isResizing.value) return
    const dx = e.clientX - resizeStart.x
    const dy = e.clientY - resizeStart.y
    
    if (resizeDirection.value.includes('right')) windowState.width = Math.max(400, resizeStart.width + dx)
    if (resizeDirection.value.includes('left')) {
      const w = Math.max(400, resizeStart.width - dx)
      windowState.x = resizeStart.left + (resizeStart.width - w)
      windowState.width = w
    }
    if (resizeDirection.value.includes('bottom')) windowState.height = Math.max(400, resizeStart.height + dy)
    if (resizeDirection.value.includes('top')) {
      const h = Math.max(400, resizeStart.height - dy)
      windowState.y = resizeStart.top + (resizeStart.height - h)
      windowState.height = h
    }
  }

  const stopResize = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', onResize)
    document.removeEventListener('mouseup', stopResize)
  }

  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const sendSuggestion = (content: string) => {
  inputContent.value = content
  sendMessage()
}

const handleRefreshIndex = () => {
  const summary = indexStatsSummary.value
  if (!knowledgeStats.value.total_chunks && !Object.keys(knowledgeStats.value.file_types || {}).length) {
    ElMessage.warning('当前索引统计还在同步中，请稍后再查看')
    return
  }
  ElMessage.success(
    `当前知识索引共 ${summary.total} 个切片，分类情况为：${summary.detail}`
  )
}

const handleExternalOpen = () => {
  openAssistant()
}

onMounted(() => {
  window.addEventListener('swiftboot-open-ai-assistant', handleExternalOpen)
  fetchKnowledgeStats()
})

onUnmounted(() => {
  window.removeEventListener('swiftboot-open-ai-assistant', handleExternalOpen)
})
</script>

<style scoped lang="scss">
$primary: #2b2bee;
$border: #f0f2f5;
$bg-light: #f8fafc;
$text-main: #1e293b;
$text-muted: #64748b;

.ai-assistant-container {
  position: fixed;
  z-index: 9999;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 最小化状态 */
.ai-minimized-new {
  width: 64px; height: 64px; position: relative; display: flex; align-items: center; justify-content: center;
}

/* 主窗口卡片 */
.ai-window-card {
  width: 100%; height: 100%;
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 60px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid $border;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* Header */
.ai-window-header {
  height: 72px;
  padding: 0 24px;
  border-bottom: 1px solid $border;
  display: flex; align-items: center; justify-content: space-between;
  cursor: grab; flex-shrink: 0;
  
  .header-left {
    display: flex; align-items: center; gap: 14px;
    .brand-icon {
      width: 44px; height: 44px;
      background: rgba(43, 43, 238, 0.05);
      border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      span { font-size: 24px; }
    }
    .brand-info {
      .title { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0; }
      .status-indicator {
        display: flex; align-items: center; gap: 6px;
        .pulse-dot { width: 6px; height: 6px; background: #10b981; border-radius: 50%; animation: pulse 2s infinite; }
        .status-text { font-size: 11px; color: $text-muted; font-weight: 500; }
      }
    }
  }
  
  .header-right {
    display: flex; align-items: center; gap: 16px;
    .model-tag {
      padding: 4px 10px; background: $bg-light; border: 1px solid $border;
      border-radius: 8px; font-size: 11px; color: $text-muted; font-weight: 600;
    }
    .header-actions {
      display: flex; gap: 4px;
      .icon-btn {
        width: 36px; height: 36px; border-radius: 10px;
        display: flex; align-items: center; justify-content: center;
        color: $text-muted; background: transparent; border: none; cursor: pointer;
        transition: all 0.2s;
        span { font-size: 20px; }
        &:hover { background: #f1f5f9; color: $primary; }
        &.close:hover { background: #fee2e2; color: #ef4444; }
      }
    }
  }
}

/* Content Layout */
.ai-window-content {
  flex: 1; display: flex; overflow: hidden;
}

/* Chat Main */
.chat-main {
  flex: 1; display: flex; flex-direction: column; background: white;
}

.chat-messages {
  flex: 1; overflow-y: auto; padding: 24px; display: flex; flex-direction: column; gap: 24px;
}

/* Welcome Empty */
.welcome-empty {
  padding: 60px 20px; text-align: center;
  .welcome-logo {
    width: 72px; height: 72px; margin: 0 auto 24px;
    background: linear-gradient(135deg, rgba(43, 43, 238, 0.1), rgba(79, 70, 229, 0.1));
    border-radius: 20px; display: flex; align-items: center; justify-content: center;
    span { font-size: 36px; color: $primary; }
  }
  h3 { font-size: 20px; font-weight: 700; color: #0f172a; margin-bottom: 32px; }
  .suggestion-list {
    display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-width: 600px; margin: 0 auto;
    .suggestion-item {
      padding: 16px; background: $bg-light; border: 1px solid $border; border-radius: 16px;
      font-size: 13px; color: $text-main; cursor: pointer; transition: all 0.2s;
      &:hover { border-color: $primary; background: white; box-shadow: 0 4px 15px rgba(0,0,0,0.05); transform: translateY(-2px); }
    }
  }
}

/* Message Rows */
.msg-row {
  display: flex; gap: 16px;
  &.user { flex-direction: row-reverse; }
}

.msg-avatar {
  width: 32px; height: 32px; flex-shrink: 0;
  background: $primary; color: white; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  span { font-size: 18px; }
}

.msg-content-box {
  max-width: 85%; display: flex; flex-direction: column; gap: 8px;
}

.msg-bubble {
  padding: 12px 20px; font-size: 14px; line-height: 1.6;
  &.ai-bubble { background: $bg-light; border: 1px solid $border; border-radius: 4px 20px 20px 20px; }
  &.user-bubble { background: white; border: 1px solid $border; border-radius: 20px 4px 20px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
}

.msg-footer {
  display: flex; gap: 12px; padding-left: 4px;
  .footer-action {
    display: flex; align-items: center; gap: 4px; font-size: 11px; color: $text-muted;
    background: transparent; border: none; cursor: pointer;
    &:hover { color: $primary; }
    span { font-size: 14px; }
  }
}

/* 深度思考组件 - DeepSeek 风格 */
:deep(.ds-thought-container) {
  margin-bottom: 20px;
  border-left: 3px solid #e2e8f0;
  background: #f8fafc;
  padding: 12px 16px;
  border-radius: 0 12px 12px 0;
  transition: all 0.3s ease;
  position: relative;
  
  &.thinking {
    border-left-color: $primary;
    background: linear-gradient(90deg, rgba(43, 43, 238, 0.04), transparent);
    
    .ds-thought-header .ds-thought-title::after {
      content: '...';
      animation: dots 1.5s infinite;
    }
  }
  
  .ds-thought-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
    user-select: none;
    
    .ds-thought-icon {
      font-size: 18px;
      color: $text-muted;
    }
    
    .ds-thought-title {
      font-size: 13px;
      font-weight: 600;
      color: $text-muted;
      letter-spacing: 0.5px;
    }

    .mini-copy-btn {
      margin-left: auto;
      background: transparent;
      border: none;
      color: #94a3b8;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      transition: all 0.2s;
      
      span { font-size: 14px; }
      &:hover { background: #e2e8f0; color: $primary; }
    }
  }
  
  .ds-thought-body {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    
    /* 思考过程中的 Markdown 特殊样式 */
    &.markdown-content {
      font-size: 13px;
      color: #64748b;
      
      p { margin-bottom: 8px; }
      strong { color: #475569; font-weight: 600; }
      code { background: #f1f5f9; padding: 2px 4px; border-radius: 4px; font-family: monospace; }
    }
  }
}

@keyframes dots {
  0%, 20% { content: '.'; }
  40% { content: '..'; }
  60% { content: '...'; }
  80%, 100% { content: ''; }
}

.ds-answer-body {
  &.markdown-content {
    font-size: 14px;
    color: #1e293b;
    line-height: 1.7;
    
    /* 最终回答的 Markdown 样式 */
    h1, h2, h3 { color: #0f172a; margin-top: 20px; margin-bottom: 12px; font-weight: 700; }
    p { margin-bottom: 16px; }
    
    /* 表格样式 */
    :deep(table) {
      width: 100%;
      margin: 16px 0;
      border-collapse: collapse;
      font-size: 14px;
      
      th, td {
        padding: 10px 12px;
        border: 1px solid #e2e8f0;
      }
      
      th {
        background-color: #f8fafc;
        font-weight: 600;
        text-align: left;
        color: #334155;
      }
      
      tr:nth-child(even) {
        background-color: #f8fafc;
      }
      
      tr:hover {
        background-color: #f1f5f9;
      }
    }
    
    /* 核心优化：深色代码块 */
    :deep(pre.custom-code-block) {
      background: #1a1b26 !important; /* Tokyo Night Dark 背景 */
      border-radius: 12px;
      padding: 16px;
      margin: 16px 0;
      overflow-x: auto;
      border: 1px solid rgba(255, 255, 255, 0.1);
      
      code {
        font-family: 'Fira Code', 'Consolas', monospace;
        font-size: 13px;
        line-height: 1.5;
        background: transparent !important;
        padding: 0;
        color: #a9b1d6;
      }
    }
    
    :deep(code:not(pre code)) {
      background: #f1f5f9;
      padding: 2px 6px;
      border-radius: 4px;
      color: #e11d48;
      font-family: monospace;
    }
  }
}

/* Footer Input Area */
.chat-footer {
  padding: 0 24px 24px; background: white;
  .input-wrapper-new {
    background: white; border: 1px solid $border; border-radius: 20px;
    padding: 12px; transition: all 0.3s;
    box-shadow: 0 10px 30px rgba(0,0,0,0.04);
    &:focus-within { border-color: $primary; box-shadow: 0 10px 30px rgba(43,43,238,0.08); }
    
    .custom-textarea {
      width: 100%; border: none; background: transparent; resize: none;
      padding: 8px 12px; font-size: 14px; color: $text-main;
      &:focus { outline: none; }
    }
    
    .input-toolbar {
      display: flex; align-items: center; justify-content: space-between; margin-top: 8px;
      .toolbar-left {
        display: flex; gap: 4px;
        .tool-icon {
          width: 32px; height: 32px; border-radius: 8px;
          display: flex; align-items: center; justify-content: center;
          color: $text-muted; background: transparent; border: none; cursor: pointer;
          &:hover { background: $bg-light; color: $primary; }
        }
      }
      .send-btn-v2 {
        padding: 8px 20px; background: #e2e8f0; color: white; border-radius: 12px;
        border: none; display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 13px;
        cursor: not-allowed; transition: all 0.2s;
        &.active { background: $primary; cursor: pointer; &:hover { transform: scale(1.02); box-shadow: 0 4px 15px rgba(43,43,238,0.3); } }
      }
    }
  }
  .footer-note { text-align: center; font-size: 10px; color: #cbd5e1; margin-top: 16px; letter-spacing: 1px; }
}

/* Sidebar */
.chat-sidebar {
  width: 300px; background: $bg-light; border-left: 1px solid $border;
  display: flex; flex-direction: column; flex-shrink: 0;
  
  .sidebar-section {
    padding: 24px; border-bottom: 1px solid $border;
    .section-title {
      font-size: 12px; font-weight: 700; color: #0f172a; text-transform: uppercase;
      letter-spacing: 1px; display: flex; align-items: center; gap: 8px; margin-bottom: 20px;
      span { font-size: 18px; color: $primary; }
    }
  }
  
  .file-tree-mini {
    .tree-node {
      display: flex; align-items: center; gap: 8px; font-size: 13px; padding: 6px 8px;
      border-radius: 8px; cursor: pointer; transition: all 0.2s;
      &:hover { background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
      &.child { margin-left: 20px; font-size: 12px; color: $text-muted; }
      span { font-size: 16px; }
    }
  }
  
  .vector-space-box {
    height: 160px; background: white; border: 1px solid $border; border-radius: 16px;
    position: relative; overflow: hidden;
    .grid-overlay {
      position: absolute; inset: 0; opacity: 0.05;
      background-image: linear-gradient($primary 1px, transparent 1px), linear-gradient(90deg, $primary 1px, transparent 1px);
      background-size: 20px 20px;
    }
    .v-dot {
      position: absolute; width: 4px; height: 4px; background: rgba(43,43,238,0.3); border-radius: 50%;
      &.center-dot { top: 50%; left: 50%; transform: translate(-50%, -50%); background: $primary; box-shadow: 0 0 10px $primary; }
    }
    .scan-line {
      position: absolute; left: 0; right: 0; height: 2px; background: rgba(43,43,238,0.2);
      top: 50%; animation: scan 4s infinite linear;
    }
    .space-status {
      position: absolute; bottom: 12px; left: 12px; display: flex; align-items: center; gap: 6px;
      font-size: 9px; font-weight: 700; color: $text-muted; text-transform: uppercase;
      .dot-pulse { width: 6px; height: 6px; background: $primary; border-radius: 50%; animation: pulse 1.5s infinite; }
    }
  }
  .space-meta { display: flex; justify-content: space-between; font-size: 10px; color: #cbd5e1; margin-top: 12px; font-weight: 500; }
  
  .sidebar-footer {
    margin-top: auto; padding: 20px;
    .refresh-btn {
      width: 100%; padding: 10px; background: white; border: 1px solid $border; border-radius: 12px;
      font-size: 12px; font-weight: 700; color: $text-muted; display: flex; align-items: center; justify-content: center; gap: 8px;
      cursor: pointer; transition: all 0.2s;
      &:hover { border-color: $primary; color: $primary; background: rgba(43,43,238,0.02); }
    }
  }
}

/* Animations */
@keyframes pulse { 0% { transform: scale(0.95); opacity: 0.5; } 50% { transform: scale(1.05); opacity: 1; } 100% { transform: scale(0.95); opacity: 0.5; } }
@keyframes scan { 0% { top: 0; } 100% { top: 100%; } }

/* Resize Handles - 增加命中区域 */
.resize-handle {
  position: absolute;
  width: 20px;
  height: 20px;
  z-index: 100;
  background: transparent;
  
  &::after {
    content: '';
    position: absolute;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #cbd5e1;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  &:hover::after {
    opacity: 1;
  }

  &.nw { top: -5px; left: -5px; cursor: nwse-resize; &::after { top: 8px; left: 8px; } }
  &.ne { top: -5px; right: -5px; cursor: nesw-resize; &::after { top: 8px; right: 8px; } }
  &.sw { bottom: -5px; left: -5px; cursor: nesw-resize; &::after { bottom: 8px; left: 8px; } }
  &.se { bottom: -5px; right: -5px; cursor: nwse-resize; &::after { bottom: 8px; right: 8px; } }
}

/* Shimmer Loading */
.loading-shimmer {
  display: flex; flex-direction: column; gap: 8px;
  .shimmer-line { height: 12px; background: #f1f5f9; border-radius: 4px; position: relative; overflow: hidden;
    &::after { content: ""; position: absolute; inset: 0; transform: translateX(-100%); background: linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent); animation: shimmer 1.5s infinite; }
  }
}
@keyframes shimmer { 100% { transform: translateX(100%); } }

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
</style>

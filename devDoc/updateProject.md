## [2026-02-04 16:45] 更新摘要

### 🚀 核心变更

- **Skills 库**: 更新了 `project-update-logger` 技能，支持中文输出、精确到分钟的时间戳，并将触发词更改为“更新变化”。
- **后端 (AI)**: 在 `SysAiController` 中实现了 DeepSeek 和 Gemini 的 SSE 流式传输，解决了 20 秒延迟问题。
- **前端 (UI/UX)**: 全面重构 AI 助手界面，对标 DeepSeek 官网风格（沉浸式布局、平滑打字机效果、无缝 Loading 状态）。

### 🐛 问题修复

- 修复了 `SysAiController.java` Lambda 表达式中的 "variable must be final" 错误。
- 修复了前端 Markdown 渲染时换行符丢失的问题。
- 修复了 `index.vue` 中的 SCSS 语法错误 (`unmatched "}"`)。
- 修复了 AI 加载状态下出现双头像/气泡的问题。

### 🔧 技术细节

- 修改: `SKILL.md`, `SysAiController.java`, `index.vue`
- 技术栈: Java (SSE), Vue 3, SCSS, Trae Skills

---

## [2026-03-20 15:05] 更新摘要

### 📝 变更综述

本次更新重点收敛了 RAG 向量索引日志链路与首页系统动态展示。AI Engine 不再把初始化和监听过程中的中间切片处理反复写入操作日志，而是统一改为聚合后的索引完成日志；首页向量索引动态同步改成紫色标识、悬停可看全文、点击直达日志详情。同时修正智能会话悬浮球误触发问题，拖动只移动，双击才打开。

### 🚀 核心变更

- **AI Engine 日志聚合**: 重构 `file_watcher.py` 与 `vector_store.py`，初始化阶段只按聚合类型写入索引完成日志，监听阶段仅写一条 `RAG 向量索引更新完成` 聚合结果日志。
- **首页系统动态**: 向量索引动态只展示新规范日志，统一使用紫色圆点，并支持悬停查看完整内容与点击跳转日志详情。
- **操作日志详情直达**: 后端新增 `/monitor/operlog/{operLogId}` 接口，前端操作日志页支持根据 `logId` 自动打开对应详情。
- **智能会话交互**: 最小化悬浮球改为双击打开，拖动悬浮球时不再在松手后误打开窗口。

### 🐛 问题修复

- 修复了初始化索引和监听索引过程中混入 `[Doc] 知识库更新完成`、`检测到前后端变更` 等中间日志，导致首页系统动态文案脏乱的问题。
- 修复了首页向量索引动态内容被截断后无法查看完整信息的问题。
- 修复了点击向量索引动态后无法直达对应日志详情的问题。
- 修复了智能会话悬浮球拖动结束后自动展开窗口的误触发问题。
- 清理了数据库中 6865 条旧格式 `VectorStore.*` 向量索引日志，避免首页继续读取历史脏数据。

### 🔧 技术细节

- 修改: `ai-engine/file_watcher.py`, `ai-engine/vector_store.py`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysOperLogController.java`
- 修改: `swiftboot-ui/src/views/dashboard/index.vue`, `swiftboot-ui/src/views/monitor/operlog/index.vue`, `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Python Watchdog, ChromaDB, Java Spring Boot, Vue 3, Element Plus, MySQL

---

## [2026-03-19 22:11] 更新摘要

### 📝 变更综述

本次迭代重点收敛了首页 Dashboard 与智能会话入口体验，重新整理了首页卡片分组与快捷入口布局，并修正 AI 索引统计口径，让首页看到的知识切片总量与本地向量库真实数据保持一致。同时将智能会话路由页改为自动拉起真实会话窗口，解决“页面能进但窗口没打开”的体验断层问题。

### 🚀 核心变更

- **首页 Dashboard**: 重构首页中段卡片布局，形成“系统动态 / RAG 洞察”与“智能索引总览 / 实时运行总览 / 快捷入口”的分层展示结构。
- **快捷入口**: 重排快捷入口卡片样式与入口集合，移除部门管理，补充 AI 看板与配置管理入口，提升高频功能直达效率。
- **智能会话**: 将 `ai/chat` 页面从静态示意页改为真实会话启动页，进入路由后自动拉起全局智能会话窗口。
- **索引统计**: 修复 Python 端知识统计接口，仅统计代码集合的问题，改为合并代码库与文档库切片总量，并在前端增加总量兜底与文档类型补偿展示。

### 🐛 问题修复

- 修复了首页“智能索引总览”只显示 `swiftboot_codebase` 统计、遗漏文档切片的问题。
- 修复了智能会话窗口需要双击最小化悬浮球才能展开的问题，改为单击即可打开。
- 修复了进入 `AI 会话窗口` 路由后只展示静态页面、未自动加载真实会话窗口的问题。

### 🔧 技术细节

- 修改: `ai-engine/main.py`, `swiftboot-ui/src/views/dashboard/index.vue`
- 修改: `swiftboot-ui/src/views/ai/chat/index.vue`, `swiftboot-ui/src/components/AiAssistant/index.vue`, `swiftboot-ui/src/views/monitor/ai-session/index.vue`
- 技术栈: Python FastAPI, ChromaDB, Vue 3, TypeScript, Element Plus

---

## [2026-02-04 17:35] 更新摘要

### 🚀 核心变更

- **AI 引擎 (RAG)**: 从零搭建了基于 ChromaDB 的本地 RAG 架构，实现了代码库的向量化存储与检索。
- **自动同步**: 引入 Watchdog 监听服务，实现了 Java 代码变更后自动更新向量数据库的功能。
- **智能问答集成**: 修改后端 `SysAiController`，在 AI 对话流程中增加了“上下文检索”步骤，使 AI 能根据项目源码回答问题。

### 🔧 技术细节

- 新增: `ai-engine/` 目录下的 `vector_store.py`, `file_watcher.py`, `main.py`, `requirements.txt`
- 修改: `SysAiController.java` (集成 `http://localhost:8001/retrieve` 接口)
- 技术栈: Python (FastAPI, ChromaDB, Watchdog), Java Spring Boot

---

## [2026-02-05 11:00] 更新摘要

### 📝 变更综述

本次迭代重点对项目的智能问答技能库进行了架构调整与标准化整理，将私有配置迁移为项目共享资源，并构建了自动化的版本日志与发布工作流，显著提升了团队协作效率与文档维护的便捷性。

### 🚀 核心变更

- **Skills 库迁移**: 将 AI 技能库从 `.trae/skills` 迁移至项目根目录 `project-skills`，确保团队共享。
- **知识库体系化**: 梳理并创建了 6 大核心 Skill 库（架构、安全、CRUD、数据库、前端、字典），实现“业务即文档”。
- **自动化工作流**: 新增 `release-notes` 技能与优化 `project-update-logger` 技能，实现了从“代码变更”到“版本发布”的全流程自动化。
- **后端适配**: 更新 `SysAiController`，支持动态加载项目级技能库，移除硬编码提示词。

### 🔧 技术细节

- 新增: `project-skills/` 下的 `project-guide`, `auth-security`, `crud-guide`, `database-schema`, `frontend-interaction`, `dict-codegen`, `release-notes`, `project-update-logger`
- 修改: `SysAiController.java`
- 技术栈: Markdown, Java IO, Trae Skills

---

## [2026-02-06 10:30] 更新摘要

### 🚀 核心变更

- **前端 (智能问答)**: 新增“停止回答”能力，支持中断流式输出并可立刻重新提问。
- **前端 (交互体验)**: 流式请求加入可取消控制，完善输入区按钮布局与样式。
- **后端 (上下文记忆)**: 历史对话改为 Redis 按用户隔离存储与读取，避免跨用户串话。

### 🐛 问题修复

- 修复了 `SysAiController.java` 中 `historyObj` 未定义导致的编译错误。
- 修复了 `SysAiController.java` 非流式对话历史加载时 `userId` 未定义问题。

### 🔧 技术细节

- 修改: `swiftboot-ui/src/components/AiAssistant/index.vue`, `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 技术栈: Vue 3, Fetch AbortController, Java Spring Boot, Redis

---

## [2026-02-06 11:45] 更新摘要

### 🚀 核心变更

- **启动脚本**: 新增 Windows 一键启动脚本与可配置 Redis 地址的配置文件，支持一键与分块启动。
- **快速跑通指南**: 重组启动流程说明，明确一键启动包含的服务与分块启动拆分逻辑。
- **AI 运维提示**: 向量检索与监听日志增加时间戳输出，方便定位变更时间。

### 🐛 问题修复

- 修复了 `SysAiController.java` 中 RAG 结果 metadata 为空导致的空指针风险。
- 修复了一键启动脚本在 PowerShell 下路径切换失败的问题。

### 🔧 技术细节

- 新增: `快速启动/start_all.bat`, `快速启动/start_config.ini`, `devDoc/用户提问到检索在回答流程.md`
- 修改: `快速启动/快速跑通文档.md`, `ai-engine/file_watcher.py`, `ai-engine/vector_store.py`, `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 技术栈: Windows BAT, PowerShell, Python, Java, Markdown

---

## [2026-02-06 14:15] 更新摘要

### 🚀 核心变更

- **后端 (AI)**: 重构 `SysAiController`，**彻底移除 Gemini 模型支持**，系统纯净化为 DeepSeek 单模型架构。
- **记忆增强 (RAG)**: 修复 AI 记忆丢失问题，通过在 DeepSeek 流式响应中捕获完整回复并存入 **Redis (短期)** 和 **ChromaDB 向量库 (长期)**，实现了真正持久化的上下文记忆与知识检索。
- **配置净化**: 清理 `application.yml` 和设计文档，移除所有废弃的 Gemini 配置项。

### 🐛 问题修复

- 修复了 AI 助手无法记住上一轮对话的问题 (SysAiController 记忆存储逻辑优化)。
- 修复了后端启动时 Maven 找不到主类 (`Unable to find a suitable main class`) 的问题。
- 修复了后端日志乱码及无法换行的问题 (Logback 编码配置)。

### 🔧 技术细节

- 修改: `SysAiController.java`, `application.yml`, `start_all.bat`, `智能问答中心设计文档.md`, `file_watcher.py`
- 技术栈: Java Spring Boot, DeepSeek API, Logback, Maven

---

## [2026-02-06 15:40] 更新摘要

### 🚀 核心变更

- **AI 引擎**: 优化 AI 上下文记忆机制，集成 Redis 短期历史对话，解决“记忆错乱”和无法追问的问题。
- **启动脚本**: `start_all.bat` 支持通过配置文件 (`start_config.ini`) 读取 Python 路径，提升跨环境兼容性。
- **文档**: 新增 v0.1.5 本地 RAG 特性推广文章，并更新快速启动文档。

### 🐛 问题修复

- 修复了 `AiAssistant` 前端组件 Markdown 渲染换行失效的问题。
- 修复了 AI 助手窗口最小化按钮点击穿透导致自动弹出的问题。
- 修复了 `SysAiController` 仅依赖 RAG 导致忽略即时对话上下文的问题。

### 🔧 技术细节

- 修改: `SysAiController.java` (集成 Redis History), `index.vue` (Markdown & Event Bubbling), `start_all.bat`
- 新增: `SwiftBoot-v0.1.5-Local-RAG-Promo.md`
- 技术栈: Java, Spring Boot, Redis, Vue 3, Batch

---

## [2026-02-07 02:40] 更新摘要

### 🚀 核心变更

- **智能会话 (AI)**: 优化 AI 问答体验，调整 System Prompt 策略（功能优先，代码次之），并将 RAG 规则抽离为独立配置文件 `rag_rule.md`。
- **操作审计**: 将 AI 对话纳入 system 操作日志审计范围，支持自动扫描 `@Log` 注解获取模块名称，解决了新模块日志筛选问题。
- **历史清除**: 实现了真正的 AI 历史记录清除功能（后端 Redis + 前端联动），不再只是清空页面。
- **开发文档**: 新增 `SwiftBoot开发使用文档.md`，总结了日志审计、统一响应、权限控制等核心设计模式与隐式规则。

### 🐛 问题修复

- 修复了 AI 助手“垃圾桶”按钮仅清空前端显示而未清除后端记忆的问题。
- 修复了“智能会话”模块因无历史数据而无法在操作日志筛选框中显示的问题。

### 🔧 技术细节

- 新增: `rag_rule.md`, `SwiftBoot开发使用文档.md`, `.trae/skills/menu-system/`
- 修改: `SysAiController.java` (Log注解 & 规则加载), `SysOperLogServiceImpl.java` (自动扫描), `index.vue` (调用清除接口)
- 技术栈: Java Spring Boot (AOP, Reflection), Vue 3, Markdown

---

## [2026-02-07 03:30] 更新摘要

### 🚀 核心变更

- **智能监控大屏**: 从零构建了“智能问答中心”大屏页面，集成了今日提问、Token 消耗、响应耗时等核心指标，以及趋势图和活跃用户榜。
- **UI/UX 重构**: 采用 Glassmorphism (毛玻璃) 风格全面重构了监控页面，优化了数据卡片、图表和列表的视觉体验，使其更具科技感。
- **侧边栏优化**: 修复了侧边栏折叠后图标不居中、弹窗过宽等问题，提升了整体布局的精致度。
- **数据持久化**: 实现了 `SysAiSession` 的全套 CRUD 与统计接口，确保 AI 对话数据可追溯、可审计。

### 🐛 问题修复

- 修复了 `InvalidCharacterError` 前端报错 (由非法图标字符 `#` 引起)。
- 修复了 `SysAiSessionMapper.xml` 中 MyBatis 映射类型错误与 SQL 关联查询逻辑。
- 修复了前端 ECharts 依赖缺失导致的构建错误。
- 修复了 TypeScript 类型检查报错 (highlight 函数类型、KeyboardEvent 类型)。

### 🔧 技术细节

- 新增: `SysAiSession` (Entity/Mapper/Service/Controller), `monitor/ai-session/index.vue`
- 修改: `layout/index.vue` (Sidebar & Popup Style), `SysAiController.java` (Async Logging)
- 技术栈: Vue 3 (Element Plus, ECharts, Tailwind), Java Spring Boot (MyBatis Plus)

---

## [2026-02-07 04:27] 更新摘要

### 🚀 核心变更

- **基础资源监控**: 增加服务器基础资源监控展示与接口对接，覆盖 CPU/内存/JVM/磁盘与趋势图。
- **文档**: 新增基础资源获取实现文档，沉淀采集与展示流程。

### 🐛 问题修复

- 修复了服务器监控页面访问 404 与动态路由加载问题。
- 修复了监控页面图标导入与路由类型报错。

### 🔧 技术细节

- 新增: `devDoc/基础资源获取实现.md`
- 修改: `swiftboot-ui/src/views/monitor/server/index.vue`, `swiftboot-ui/src/router/index.ts`, `swiftboot-ui/src/stores/user.ts`
- 技术栈: Vue 3, Element Plus, ECharts, TypeScript, Java Spring Boot, Markdown

---

## [2026-02-07 23:50] 更新摘要

### 🚀 核心变更

- **智能会话 (审计与清理)**: 实现了 AI 会话记录的**精准删除**与**三端同步清理**（数据库 + Redis 短期记忆 + 向量库长期记忆），彻底解决了删除后数据残留或误删其他用户记忆的问题。
- **AI 引擎 (性能优化)**: 彻底重构了 `file_watcher.py` 监听脚本，实现了**增量同步**机制与状态持久化，重启服务不再全量重建索引，启动速度提升至秒级。
- **开发体验**: 优化了 IDE 配置，自动锁定 Conda `ai_agent` 环境，并修复了 Windows 路径大小写导致的同步失效问题。

### 🐛 问题修复

- 修复了 `testStudent` 和 `testProject` 页面表格横向滚动时表头文字重叠的 UI 问题。
- 修复了 `file_watcher.py` 因路径大小写敏感导致每次重启都误判为全量更新的 Bug。
- 修复了后端删除 Redis 缓存时因 JSON 格式差异导致匹配失败的问题。

### 🔧 技术细节

- 修改: `SysAiSessionServiceImpl.java` (增强删除逻辑), `file_watcher.py` (增量同步), `vector_store.py` (精准删除接口)
- 修改: `monitor/ai-session/index.vue` (交互优化), `.vscode/settings.json`
- 技术栈: Python Watchdog, JSON State, Java (Redis Template), Vue 3

---

## [2026-02-10 10:30] 更新摘要

### 🚀 核心变更

- **AI 意图识别升级**: 彻底重构了 Python 侧的代码解析逻辑 (`knowledge_ingest.py`)，不仅支持 Java，现在也支持 **Python 文件的全量解析**（自动提取函数、类、注释），确保 RAG 引擎能“认识”自己。
- **流式响应重构**: 移除了非流式响应逻辑，全面拥抱流式（Stream）通路；优化了前端错误处理，网络中断时不再清空已生成的回答，而是追加错误提示。
- **稳定性增强**: 后端流式请求超时时间从 90s 延长至 **300s**，解决了 RAG 深度检索后长文本生成被截断的问题。

### 🐛 问题修复

- **修复工具反复调用导致回复为空**:
  - **现象**: 在 AI 调用工具（如 `search_codebase`）后，经常出现反复调用或最终输出为空的情况。
  - **原因**: 后端在强制结束工具调用阶段后，为了防止模型继续调用，移除了 `tools` 参数并添加了严格的 `stop` 词。但这导致模型上下文不一致（历史消息有 tool_calls 但当前请求无 tools），且 `stop` 词误杀了正常输出。
  - **解决**: 策略调整为 **保留 tools 参数但强制 tool_choice="none"**，明确告知模型“我知道你有工具，但现在禁止使用”，同时移除了可能误杀的 `stop` 参数。
- **修复 RAG 无法检索 Python 代码**: 之前 RAG 引擎只索引 Java 代码，导致 AI 无法回答关于向量数据库实现的问题。现已支持 Python 代码索引。
- **修复前端流式中断覆盖**: 优化了 `AiAssistant/index.vue`，流式失败时保留现有内容。

### 🔧 技术细节

- 修改: `SysAiController.java` (Stream逻辑重构), `AiAssistant/index.vue` (Error Handling), `knowledge_ingest.py` (PythonParser), `file_watcher.py` (Multi-language Support)
- 技术栈: Java Spring Boot, Python (AST/Regex), Vue 3, DeepSeek API Stream

---

## [2026-02-11 16:30] 更新摘要

### 🚀 核心变更

- **UI/UX 体验升级**: 首页（Dashboard）全面拥抱 Glassmorphism (毛玻璃) 风格，优化了整体视觉层次与科技感。
- **系统动态感知**: 首页新增“系统动态”实时流，对接 RAG 引擎的索引更新日志与操作审计，让 AI 的每一次“思考”与“学习”都可视化。
- **实时数据回显**: 实现了服务器基础指标（CPU、内存、JVM）在首页的实时回显，增强了系统的可观测性。
- **AI 引擎架构统一**: 实现了 Python 引擎与 Java 后端的配置统一。Python 脚本现在直接读取后端的 `application-dev.yml`，不再维护独立的 `config.yml`，简化了部署流程。
- **日志同步机制**: 重构了 RAG 索引更新的日志上报逻辑，实现了批量更新聚合（防抖），避免了频繁变更导致的日志刷屏，并优化了时间格式兼容性。

### 🐛 问题修复

- 修复了 Python 引擎回调接口 `/monitor/operlog/inner/add` 的 401 鉴权问题（添加 Sa-Token 白名单）。
- 修复了 `vector_store.py` 上报日志时间格式与后端 `LocalDateTime` 反序列化不兼容的问题（统一为 `yyyy-MM-dd HH:mm:ss`）。
- 修复了前端 `dashboard/index.vue` 中 `AxiosResponse` 类型定义的 TypeScript 报错。

### 🔧 技术细节

- 修改: `dashboard/index.vue` (UI & API Integration), `file_watcher.py` (Log Sync), `SaTokenConfig.java` (Auth), `application-dev.yml`
- 技术栈: Vue 3 (Glassmorphism), Python (Threading), Java (Spring Boot Security)

---

## [2026-02-11 20:50] 更新摘要

### 🚀 核心变更

- **权限控制 (RBAC)**: 严格化 RBAC 权限模型，彻底移除了代码中的“超级管理员特权”硬编码，改为完全依赖数据库配置。
- **菜单体系优化**: 新增“智能会话”一级菜单，整合了 AI 相关功能；修复了角色编辑时“按钮级权限”丢失的问题，优化了菜单树查询逻辑，确保权限分配的完整性。

### 🐛 问题修复

- 修复了 `selectMenuList` 方法过滤了“按钮”类型权限，导致编辑角色时自动取消所有按钮权限的 Bug。
- 修复了修改角色权限后需要用户强制重新登录的问题，实现了 Session 权限的静默刷新机制。
- 修复了超级管理员因移除硬编码特权且数据库权限缺失，导致“预览”、“编辑”等按钮失效的问题。

### 🔧 技术细节

- 修改: `SysRoleServiceImpl.java` (Session 刷新), `SysMenuServiceImpl.java` (移除按钮过滤), `SysUserRoleMapper.xml`
- 数据库: 补全了 `sys_role_menu` 表中超级管理员的完整权限 (50 项)，调整了 AI 相关菜单结构。

---

## [2026-02-11 22:30] 更新摘要

### 🚀 核心变更

- **前端 (Dashboard)**: 重新设计了首页 Hero 模块，采用 Glassmorphism (毛玻璃) 风格，增加了立体感阴影与渐变边框，提升了视觉精致度。
- **前端 (交互体验)**: 优化了系统动态刷新按钮，去除边框并增加了旋转动画；修复了首页悬浮装饰元素遮挡顶部 Header 的层级问题。
- **图标渲染**: 修复了侧边栏菜单图标因大小写不匹配导致的空白问题，增加了忽略大小写的匹配逻辑。

### 🐛 问题修复

- 修复了 `layout/index.vue` 中 `z-index` 层级配置错误导致 Header 交互被遮挡的问题。
- 修复了 Element Plus 图标组件名称匹配逻辑过严导致的渲染失败。

### 🔧 技术细节

- 修改: `layout/index.vue`, `views/dashboard/index.vue`
- 技术栈: Vue 3, Tailwind CSS (z-index, backdrop-blur), Element Plus Icons

---

## [2026-02-13 10:55] 更新摘要

### 🚀 核心变更

- **AI 引擎**: 优化文件监听器 (`file_watcher.py`)，引入 MD5 内容指纹校验和增强防抖机制，彻底解决未修改文件触发重索引的问题。
- **监控大屏**: 修复 AI 会话监控页面的“热点词云”不显示问题，支持单字/单词提问的统计展示。

### 🐛 问题修复

- 修复了 `ai-engine` 在后台无意义重复索引未变更文件的问题。
- 修复了 `SysAiSessionMapper.xml` SQL 逻辑无法提取不含空格的短问题关键词的问题。
- 修复了前端词云组件可能的内存泄漏问题（增加 `dispose` 逻辑）。

### 🔧 技术细节

- 修改: `ai-engine/file_watcher.py`, `SysAiSessionMapper.java`, `index.vue`
- 技术栈: Python Watchdog, MyBatis SQL, ECharts 3D, Vue 3

---

## [2026-02-14 17:15] 更新摘要

### 🚀 核心变更

- **AI 看板 (UI/UX)**: 新增“突触活跃度”详情弹窗，集成 ECharts 柱状图与动态趋势分析建议，支持按周/月/季/年多维度筛选。
- **演示模式**: 在突触活跃度弹窗中增加“真实/演示数据”一键切换功能，方便演示汇报。
- **视觉优化**: 调整了弹窗描述文字排版（两行展示、字体优化），移除了对话框关闭按钮边框，取消了雷达图 Tooltip，提升整体精致度。
- **版本发布**: 完成 v0.1.8 版本发布流程，生成了 Release Notes 与推广文章，并将项目地址更新为 Gitee 仓库。

### 🐛 问题修复

- 修复了 `SysAiSessionServiceImpl.java` 编译错误（方法签名不匹配、注解冗余）。
- 修复了前后端知识储备基准值不一致的问题（统一调整为 10,000）。
- 修复了 AI 看板雷达图 Tooltip 遮挡视线的问题。

### 🔧 技术细节

- 新增: `monitor/ai-session/index.vue` (Activity Dialog), `SysAiSessionMapper.java` (Aggregation SQL), `release_notes/v0.1.8.md`
- 修改: `SysAiSessionServiceImpl.java`, `devDoc/推广文章/AI看板升级发布.md`
- 技术栈: Vue 3, ECharts, Java Spring Boot, Markdown

---

## [2026-02-14 18:25] 更新摘要

### 🚀 核心变更

- **后端 (AI)**: 在 `ai-engine` 中新增 `/knowledge/stats` 接口，从 ChromaDB 实时拉取真实的知识库统计数据（切片数量、语言分布），并在 Java 后端 `SysAiSessionServiceImpl` 中完成对接，彻底替换了模拟数据。
- **前端 (UI/UX)**: 全面重构“脑容量”监控弹窗，采用严格的左右双栏布局修复了上下对齐问题，并增强了视觉层级（渐变卡片、图标装饰）。
- **智能可视化**: 新增“智能切分策略”可视化模块，直观展示 Java (AST解析) 与 Vue (语义块) 的切片逻辑；新增“记忆留存策略”模块，解释 LRU 淘汰机制。

### 🐛 问题修复

- 修复了“脑容量”弹窗中顶部卡片与底部图表垂直对齐错位的问题。
- 修复了知识库统计数据为静态假数据的问题，实现了 Python 引擎与 Java 后端的实时数据互通。

### 🔧 技术细节

- 修改: `monitor/ai-session/index.vue`, `SysAiSessionServiceImpl.java`, `ai-engine/main.py`
- 技术栈: Vue 3 (Grid Layout), Java Spring Boot (HTTP Client), Python FastAPI, ChromaDB

---

## [2026-02-14 19:10] 更新摘要

### 🐛 问题修复

- **Markdown 渲染器类型修复**:
  - 修复了 `index.vue` 中 `MarkdownIt` 配置对象的 `highlight` 函数隐式 `any` 返回类型错误。
  - 为 `highlight` 函数的参数 `(str: string, lang: string)` 和返回类型 `string` 添加了明确的 TypeScript 类型注解。

### 🔧 技术细节

- 修改: `monitor/ai-session/index.vue`
- 技术栈: Vue 3, TypeScript

---

## [2026-03-03 10:30] 更新摘要

### 🚀 核心变更

- **AI问答**: 实现AI问答向量索引同步通知功能，确保同步成功后在AI看板和首页系统动态中显示通知。
- **权限控制**: 为向量索引同步通知实现了严格的数据权限控制，支持普通用户、部门管理员和超级管理员按权限查看。

### 🐛 问题修复

- 修复了 `SysAiController.java`中异步线程 `SecurityUtils.getLoginUser()`导致的 `NullPointerException`。
- 修复了AI看板“索引构建流”中历史向量索引同步日志无法显示的问题。

### 🔧 技术细节

- 修改: `SysAiController.java`, `SysOperLogServiceImpl.java`, `SysUserMapper.java`, `index.vue`
- 技术栈: Java, Spring Boot, Vue 3, SSE, MySQL

---

## [2026-03-03 16:35] 更新摘要

### 🚀 核心变更

- **AI 可解释性 (Trace)**: 实现了全链路 AI Trace 追踪系统。新增 `sys_ai_trace` 数据库表及实体层，支持记录思考路径、工具调用（含参数与耗时）及 RAG 检索上下文。
- **AI 思考逻辑增强**:
  - **深度推理**: 重构了 System Prompt 约束与后端 SSE 注入机制，强制 AI 按照意图拆解、执行策略、逻辑推演三个维度进行深度思考。
  - **Agent 2.0**: 将工具调用上限提升至 2 轮，支持 AI 在多轮检索中自我纠偏并获取更精准的项目代码上下文。
  - **透明化引用**: 思考过程中实时展示引用的具体文件名，解决“黑盒生成”问题。
- **前端 UI/UX (对标 DeepSeek)**:
  - **沉浸式窗口**: 全面重构 AiAssistant 弹窗 UI，支持自由缩放、双栏布局（会话区+侧边栏知识树），还原 DeepSeek 级极简美感。
  - **差异化渲染**: 实现思考过程与正式回答的物理分离展示，支持独立复制思考逻辑或回答内容。
  - **交互优化**: 引入“智能滚动 (Smart Scroll)”逻辑，支持生成时手动翻阅历史；集成 **Tokyo Night Dark** 风格深色代码块与多语言语法高亮。

### 🐛 问题修复

- 修复了模型输出中 DSML 内部标签泄漏导致的界面乱码问题。
- 优化了对话开启时的相似度检查逻辑，彻底消除了无历史记录时的 40s 超时延迟。
- 修复了后端 ThreadLocal 上下文在异常情况下无法自动清理导致的内存泄漏隐患。

### 🔧 技术细节

- 修改: `SysAiController.java`, `AiAssistant/index.vue`, `AiTraceContext.java`
- 新增: `SysAiTraceController.java`, `SysAiTraceService.java`, `AI问答深度思考与链路追踪设计实现.md`
- 技术栈: Java Spring Boot (ThreadLocal, SSE), Vue 3 (Markdown-it, Highlight.js), Python FastAPI, MySQL

---

## [2026-03-19 09:55] 更新摘要

### 🚀 核心变更

- **切片索引重构**: 重构了 RAG 向量索引架构，原来所有切片混合索引在一起，现在按语言/类型分开存储（Java、Vue、Python 等独立集合），提升了检索精度与查询效率。
- **定时任务系统**: 新增定时任务管理模块，支持在线编写任务脚本、查看执行日志、设置执行策略（Cron 表达式）。
- **站内消息**: 新增站内消息模块，支持系统公告和即时消息推送。
- **系统公告**: 新增系统公告管理，支持富文本编辑、发布状态管理。
- **岗位管理**: 新增岗位管理模块，支持组织架构中的岗位配置。
- **文件管理**: 新增文件管理模块，支持本地上传、文件浏览与删除。

### 🐛 问题修复

- 修复了 `SysAiController.java` 异步线程中 `SecurityUtils.getLoginUser()` 导致的空指针问题。

### 🔧 技术细节

- 新增: `SysJob`, `SysJobLog`, `SysMessage`, `SysNotice`, `SysPost`, `SysFile` (Entity/Mapper/Service/Controller)
- 新增: `SysAiTrace`, `AiTraceContext` (AI Trace 增强)
- 修改: `ai-engine/main.py`, `vector_store.py` (切片索引重构)
- 技术栈: Vue 3, Java Spring Boot, Python FastAPI, ChromaDB

---

## [2026-03-20 00:19] 更新摘要

### 📝 变更综述

本次更新聚焦智能问答路由精度与交互稳定性：补齐了历史问题查询的真实数据链路，强化了高置信度闲聊场景的工具调用约束，并将 RAG 检索升级为意图感知重排；同时打通记忆引用回写与追问建议链路，降低无效检索并增强连续对话质量。

### 🚀 核心变更

- **历史提问查询**: 新增历史问题识别与直连会话表查询能力，普通用户查询本人历史，管理员可按请求查看全用户历史。
- **意图驱动路由**: 新增 CHAT && confidence>=0.75 的高置信度判定分支，命中后禁用工具调用并限制工具循环路径。
- **RAG 重排**: Python 检索接口支持 intent/tool_name 入参，按意图与类型做召回加权重排，提升代码/文档检索命中质量。
- **记忆闭环**: 新增 /memory/update_citation，在命中长期记忆后回写引用次数，并生成可继续追问建议。

### 🐛 问题修复

- 修复了闲聊高置信度场景仍可能触发工具调用、造成无关检索与额外耗时的问题。
- 修复了“历史问题查询”依赖模型猜测而非真实会话数据的问题。
- 修复了记忆命中 ID 与检索上下文共用字段导致覆盖冲突的问题（拆分独立字段传递）。

### 🔧 技术细节

- 修改: `ai-engine/main.py`, `ai-engine/vector_store.py`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/context/AiTraceContext.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/service/SysAiSessionService.java`, `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/service/impl/SysAiSessionServiceImpl.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/mapper/SysAiSessionMapper.java`, `swiftboot-backend/swiftboot-admin/src/main/resources/mapper/SysAiSessionMapper.xml`
- 技术栈: Java Spring Boot, Python FastAPI, ChromaDB, SSE, Redis, MyBatis

---

## [2026-03-20 15:26] 更新摘要

### 📝 变更综述

本次更新继续细化首页“系统动态”中向量索引更新列表的展示方式，将其完全对齐到“用户操作动态”的同款列表结构，只保留紫色圆点、悬停显示全文和点击跳转详情这三个差异点，避免列表视觉风格割裂。

### 🚀 核心变更

- **首页系统动态**: 向量索引更新列表改为复用操作动态同款行结构与排版逻辑，统一了文字层级、时间位置和截断展示方式。

### 🐛 问题修复

- 修复了向量索引更新列表 UI 与操作动态不一致、看起来像独立按钮条目的问题。

### 🔧 技术细节

- 修改: `swiftboot-ui/src/views/dashboard/index.vue`
- 技术栈: Vue 3, Element Plus, Tailwind CSS

---

## [2026-03-20 15:47] 更新摘要

### 📝 变更综述

本次更新继续修正首页系统动态与智能会话窗口的交互细节。首页两列动态列表都改为稳定的原生悬停全文与点击跳详情逻辑，避免提示内容在滚轮滚动时跟随漂移；智能会话窗口的“更新索引”则改为触发即提示，直接基于当前聚合统计反馈索引总量与分类数量，提升交互流畅度。

### 🚀 核心变更

- **首页系统动态**: 向量索引更新与用户操作动态统一支持悬停展示完整内容、点击跳转日志详情。
- **智能会话窗口**: “更新索引”触发后立即提示当前总切片数与各聚合类型数量，不再等待后端轮询完成，侧边栏继续展示当前索引摘要。

### 🐛 问题修复

- 修复了向量索引更新悬停提示在滚轮滚动时跟随列表上移或下移的问题。
- 修复了用户操作动态只能截断显示、无法查看全文和跳转详情的问题。
- 修复了智能会话窗口索引按钮反馈过慢、为提示语额外轮询后端导致交互迟滞的问题。

### 🔧 技术细节

- 修改: `swiftboot-ui/src/views/dashboard/index.vue`, `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Vue 3, Element Plus, Tailwind CSS

---

## [2026-03-20 15:52] 更新摘要

### 📝 变更综述

本次更新继续收敛智能会话窗口中“更新索引”按钮的交互语义。按钮不再触发后端索引重建请求，而是改为纯前端统计提示入口，直接展示当前已加载的总切片数与各聚合类型数量，减少额外请求并增强即时反馈。

### 🚀 核心变更

- **智能会话窗口**: “更新索引”按钮改为直接展示当前索引统计，不再请求 `/system/ai/index/rebuild`。

### 🐛 问题修复

- 修复了点击“更新索引”仍会触发后端重建请求、与期望的轻交互提示不一致的问题。

### 🔧 技术细节

- 修改: `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Vue 3, Element Plus

---

## [2026-03-20 15:56] 更新摘要

### 📝 变更综述

本次更新继续优化智能会话窗口索引统计入口的用户表达，将按钮与提示语都改为更清晰的终端用户文案，避免“查看统计”被误解为“触发更新”。

### 🚀 核心变更

- **智能会话窗口**: 将按钮文案调整为“查看索引统计”，并把提示语改为更直观的当前索引总量与分类说明。

### 🐛 问题修复

- 修复了按钮文案仍然带有“更新”语义、容易让用户误以为会触发索引重建的问题。

### 🔧 技术细节

- 修改: `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Vue 3, Element Plus

---

## [2026-03-20 17:29] 更新摘要

### 📝 变更综述

本次更新完成了 RAG 稳定性第一阶段优化，从“方案建议”真正落到了代码层。主链路改为并行预检与总预算降级，首轮 LLM 请求增加轻量重试，Python 侧补齐 embedding 预热与缓存机制；同时将这次优化沉淀为原设计文档补充和一份独立的 `优化升级v1.0` 报告。

### 🚀 核心变更

- **Java 主链路**: `SysAiController` 将 `intent`、`similarity`、`memory` 预检改为并行执行，并引入总预算降级机制。
- **外部模型调用**: 首轮 LLM 非流式请求增加一次轻量重试，提升 `Connection reset / timeout` 场景下的成功率。
- **Python AI Engine**: 为相似度计算和长期记忆召回补充缓存，并在启动时预热 embedding function，降低冷启动和重复查询抖动。
- **设计文档**: 在原升级版设计方案中追加“优化升级 V1.0 落地补充”，并新增独立升级报告。

### 🐛 问题修复

- 修复了本地预检串行执行导致关键路径过长、任何一段超时都容易连锁退化的问题。
- 修复了外部模型首轮请求遇到短时网络抖动时直接整轮失败的问题。
- 修复了 Python 侧相似度与长期记忆重复计算过多、容易放大本地超时抖动的问题。

### 🔧 技术细节

- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 修改: `ai-engine/vector_store.py`
- 修改: `devDoc/设计实现/智能问答RAG引擎升级版设计方案(动态权重与防僵化机制).md`
- 新增: `devDoc/设计实现/智能问答RAG引擎升级版设计方案(动态权重与防僵化机制)-【优化升级v1.0】.md`
- 技术栈: Java Spring Boot, Python FastAPI, ChromaDB, Thread Pool, Retry, Cache, Markdown

---

## [2026-03-22 20:21] 更新摘要

### 📝 变更综述

本次更新补齐了智能会话的“简单常规问题快路径”。对于“你好”“你是谁”“谢谢”“再见”“你能干什么”这类短句，后端现在会直接本地回答，不再进入并行预检与 RAG 主链路；同时修正了安全规则中对身份类自然问句的误拦截。

### 🚀 核心变更

- **智能会话快路径**: `SysAiController` 新增简单常规问题本地直返逻辑，命中后直接输出固定模板回答。
- **安全规则修正**: 从 `security_rules.json` 中移除 `你到底是谁` 的越权拦截规则，避免身份问句被误挡。

### 🐛 问题修复

- 修复了简单闲聊虽然有 CHAT 规则，但仍要进入并行预检的问题。
- 修复了“你到底是谁”这类自然问法会被安全层误判为指令越权的问题。

### 🔧 技术细节

- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 修改: `ai_rule/security_rules.json`
- 技术栈: Java Spring Boot, JSON Rule Config

---

## [2026-03-22 20:50] 更新摘要

### 📝 变更综述

本次更新将简单常规问题快路径正式规则化。对于“你好”“你是谁”“你能干什么”“谢谢”“再见”等问题，系统现在会依据独立规则文件直接走轻量 LLM 回答，不再进入并行预检与复杂 RAG 流程；前端也同步去掉了这类问题的空助手占位，体感更轻。

### 🚀 核心变更

- **快路径规则文件**: 新增 `ai_rule/quick_chat_rules.json`，统一维护简单常规问题的匹配模式、回答指令和兜底答案。
- **后端快路径升级**: `SysAiController` 对命中规则的问题直接走无工具、无预检、无记忆召回的轻量回答流程。
- **前端交互优化**: `AiAssistant` 对命中快路径的问题延迟创建助手消息，不再先显示空白占位。
- **系统规则补充**: `system_identity.md` 同步明确这类问题应直接自然回答，不进入复杂检索链路。

### 🐛 问题修复

- 修复了简单常规问题虽然已识别为闲聊，但仍会进入并行预检和复杂处理的问题。
- 修复了简单常规问题在前端仍会先出现空助手占位、影响丝滑体验的问题。

### 🔧 技术细节

- 新增: `ai_rule/quick_chat_rules.json`
- 修改: `ai_rule/system_identity.md`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java`
- 修改: `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Java Spring Boot, Vue 3, JSON Rule Config

---

## [2026-03-22 21:45] 更新摘要

### 📝 变更综述

本次更新完成了第一步安全整改，重点收口仓库明文凭据、默认密码自动回写、后端跨域与文档暴露，以及前端 token 存储和 Markdown 渲染带来的风险。前后端和 AI 引擎都做了编译校验，确保这轮收口不是停留在配置层。

### 🚀 核心变更

- **配置安全收口**: `application.yml` 与 `application-dev.yml` 改为使用环境变量承接数据库、Redis、DeepSeek 和上传目录等敏感配置。
- **默认账号初始化收口**: `PasswordInitializer` 改为显式开关模式，默认不再自动把账户密码回写为固定值。
- **后端暴露面控制**: `CorsConfig`、`SaTokenConfig`、`Knife4jConfig` 统一改为基于配置控制白名单、匿名访问和文档装配条件。
- **前端敏感信息处理**: 登录 token 从 `localStorage` 改为 `sessionStorage`，登录页移除默认账号密码预填，AI 助手 Markdown 渲染关闭原始 HTML 并补充安全清洗逻辑。

### 🐛 问题修复

- 修复了仓库配置文件中存在明文密码和 API Key 的问题。
- 修复了后端启动时默认账户可能被自动重置为弱密码的问题。
- 修复了 Swagger、Knife4j 和公网文件访问默认暴露过宽的问题。
- 修复了前端 token 长驻和 Markdown 渲染过宽带来的潜在安全风险。

### 🔧 技术细节

- 修改: `swiftboot-backend/pom.xml`, `swiftboot-backend/swiftboot-admin/pom.xml`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/resources/application.yml`, `swiftboot-backend/swiftboot-admin/src/main/resources/application-dev.yml`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/SwiftBootAdminApplication.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/config/Knife4jConfig.java`, `PasswordInitializer.java`
- 修改: `swiftboot-backend/swiftboot-common/common-core/src/main/java/com/swiftboot/common/core/config/CorsConfig.java`
- 修改: `swiftboot-backend/swiftboot-common/common-security/src/main/java/com/swiftboot/common/security/config/SaTokenConfig.java`
- 修改: `swiftboot-ui/src/stores/user.ts`, `swiftboot-ui/src/views/login/index.vue`, `swiftboot-ui/src/components/AiAssistant/index.vue`
- 技术栈: Java Spring Boot, Redis, Vue 3, Markdown-it, Maven

---

## [2026-03-22 22:05] 更新摘要

### 📝 变更综述

本次更新把原先演示级的本地上传能力升级成了可复用的文件存储底座，形成了“统一存储抽象 + 文件元数据 + 存储配置 + 通用附件组件”的完整链路。系统现在已经支持本地与云存储切换，也能把附件能力直接嵌进业务表单使用。

### 🚀 核心变更

- **文件存储底座**: 新增统一存储抽象，支持 `local / minio / oss / cos` 四种存储类型，文件接口统一改为按 `fileId` 管理。
- **存储配置管理**: 增加存储配置接口和 Redis 持久化配置，前端配置页新增“存储配置”标签，可切换当前生效存储并维护云端参数。
- **文件元数据升级**: `sys_file` 增加 `storage_bucket`、`mime_type`、`visibility`、`biz_type`、`biz_id` 等字段，并补充数据库增量脚本 `upgrade_file_storage_20260322.sql`。
- **附件复用能力**: 新增 `AttachmentManager` 组件，支持按 `bizType + bizId` 自动加载附件，并已在 `testProject` 表单弹窗中完成示例接入。

### 🐛 问题修复

- 修复了旧文件模块依赖文件名操作、难以支持同名文件和业务绑定的问题。
- 修复了前端原生预览和下载与 token 请求头机制不兼容的问题，改为后端签发临时访问地址。
- 修复了对象存储 SDK 接入过程中的依赖坐标和类型兼容问题，确保后端可以正常编译。

### 🔧 技术细节

- 修改: `swiftboot-backend/pom.xml`, `swiftboot-backend/sql/swiftboot.sql`, `swiftboot-backend/sql/upgrade_file_storage_20260322.sql`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysFileController.java`, `SysStorageConfigController.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/service/impl/SysFileServiceImpl.java`, `SysStorageConfigServiceImpl.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/storage/` 下本地、MinIO、OSS、COS 存储实现
- 修改: `swiftboot-ui/src/api/system/file.ts`, `swiftboot-ui/src/api/system/storage.ts`
- 修改: `swiftboot-ui/src/views/system-file-manage.vue`, `swiftboot-ui/src/views/tool-config-page.vue`, `swiftboot-ui/src/views/storage-config-panel.vue`
- 新增: `swiftboot-ui/src/components/AttachmentManager/index.vue`
- 示例接入: `swiftboot-ui/src/views/testProject/index.vue`
- 技术栈: Java Spring Boot, MyBatis-Plus, Redis, Vue 3, Element Plus, MinIO SDK, 阿里云 OSS SDK, 腾讯 COS SDK

---

## [2026-03-22 22:23] 更新摘要

### 📝 变更综述

本次更新把 `快速启动` 目录整体迁移为 `quick-start`，统一收口到英文路径，减少 Windows 工具链在中文目录下可能出现的兼容问题。与此同时，启动脚本和 IDEA 直接启动后端的配置源也被合并到同一份 `start_config.ini`。

### 🚀 核心变更

- **启动目录英文统一**: 原 `快速启动` 目录迁移为 `quick-start`，脚本和文档同步改成英文命名。
- **配置源统一**: `start_all.bat`、`start_frontend_backend.bat`、`start_backend_only.bat`、`start_ai_engine.bat` 全部改为读取 `quick-start/start_config.ini`。
- **IDEA 直启兜底**: `SwiftBootAdminApplication` 新增启动前查找逻辑，IDEA 直接启动后端时也会自动读取同一份配置文件。
- **索引白名单同步**: AI 引擎索引白名单已补入 `quick-start`，避免目录改名后 watcher 和重建链路漏掉新目录。

### 🐛 问题修复

- 修复了中文目录名在 Windows 工具链里可能引发路径兼容问题的隐患。
- 修复了脚本启动和 IDEA 直启使用两套配置源、密码类配置需要重复维护的问题。
- 修复了目录改名后 AI 引擎索引白名单可能遗漏新路径的问题。

### 🔧 技术细节

- 修改: `quick-start/start_all.bat`, `quick-start/start_frontend_backend.bat`, `quick-start/start_backend_only.bat`, `quick-start/start_ai_engine.bat`
- 修改: `quick-start/start_config.ini`, `quick-start/quick-start-guide.md`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/SwiftBootAdminApplication.java`
- 修改: `ai-engine/file_watcher.py`, `ai-engine/main.py`
- 修改: `README.md`, `TRAE快速熟悉项目参考上下文.md`, `快速了解项目文档.md`
- 技术栈: Windows BAT, Java Spring Boot, Python, Markdown

---

## [2026-03-23 10:50] 更新摘要

### 📝 变更综述

本次更新继续做配置类接口的安全收口，把“敏感密钥直接回显给前端”和“接口只有登录态、没有按钮级权限控制”这两个高优先级问题先处理掉。同时补了一份接口规范与落地改造方案文档，方便后面继续往下收。

### 🚀 核心变更

- **存储配置脱敏展示**: `SysStorageConfigServiceImpl` 将展示配置和运行时配置拆开处理，前端读取时不再直接拿到真实密钥。
- **密钥保存保护**: 当前端传空字符串或脱敏占位时，保存逻辑会自动保留旧密钥，避免把真实值覆盖掉。
- **配置接口权限收口**: `SysStorageConfigController` 与 `SysAiConfigController` 补齐 `tool:config:list` / `tool:config:edit` 权限控制。
- **接口规范文档**: 新增 `devDoc/设计实现/接口规范清单与落地改造方案.md`，明确 JSON、Binary、Stream 三类接口的治理方向。

### 🐛 问题修复

- 修复了云存储密钥会被原样回显到前端配置页的问题。
- 修复了前端展示脱敏值后再次保存，可能把真实密钥覆盖掉的问题。
- 修复了 AI 配置与存储配置接口只依赖登录态、缺少细粒度权限控制的问题。

### 🔧 技术细节

- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/service/impl/SysStorageConfigServiceImpl.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysStorageConfigController.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiConfigController.java`
- 修改: `swiftboot-backend/sql/add_menus.sql`
- 修改: `swiftboot-ui/src/views/storage-config-panel.vue`
- 新增: `devDoc/设计实现/接口规范清单与落地改造方案.md`
- 技术栈: Java Spring Boot, Sa-Token, Redis, Vue 3, SQL, Markdown

---


## [2026-03-23 14:00] 更新摘要

### 📝 变更综述

本次更新继续按《接口规范清单与落地改造方案》推进接口治理，从“配置类安全收口”进一步延伸到“业务异常 HTTP 语义统一、前端错误提示兼容、部分高频写接口 DTO 化、控制器失败分支收口”。这一轮改造的目标是让接口契约更清晰、错误反馈更稳定、前后端对业务失败的理解更一致。

### 🚀 核心变更

- **异常语义统一**: 扩展 BusinessException 以支持携带 HttpStatus，并让 GlobalExceptionHandler 对业务异常按真实 HTTP 状态返回，不再默认混用 HTTP 200。
- **前端错误消费兼容**: 重写 request.ts 响应拦截器，优先读取后端返回的 msg，使 4xx/5xx 下的业务提示能被前端正确展示。
- **高频写接口 DTO 化**: 为 岗位 / 任务 / 公告 / 用户 补齐写入 DTO，并在对应控制器中用 DTO + @Valid 替代直接接收 Entity。
- **失败分支收口**: 清理了部分控制器中的 return R.fail(...) 分支，改由业务异常统一返回，例如岗位编码冲突、AI 测试连接失败、AI trace 不存在、AI 索引重建失败、越权清理缓存等场景。
- **批量删除风格统一**: 将 岗位 / 任务 / 公告 / 用户 删除接口统一为 List `<Long>` 风格，向项目内现有主流模式靠拢。

### 🐛 问题修复

- 修复了业务异常虽有错误码但仍返回 HTTP 200，导致前端与网关层难以准确识别失败语义的问题。
- 修复了前端在 4xx/5xx 场景下优先显示通用状态文案、丢失后端具体业务提示的问题。
- 修复了 SysPostController 中岗位编码重复仍走控制器手写返回的问题，改为由服务层统一抛出冲突异常。
- 修复了 AI 配置测试、AI Trace 查询、AI 索引重建等接口仍使用 R.fail(...) 直接返回导致语义不统一的问题。

### 🔧 技术细节

- 修改: swiftboot-backend/swiftboot-common/common-core/src/main/java/com/swiftboot/common/core/exception/BusinessException.java
- 修改: swiftboot-backend/swiftboot-common/common-core/src/main/java/com/swiftboot/common/core/exception/GlobalExceptionHandler.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysPostController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysJobController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysNoticeController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysUserController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiConfigController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/controller/SysAiTraceController.java
- 修改: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/service/impl/SysPostServiceImpl.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysPostDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysJobDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysJobStatusDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysJobRunDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysNoticeDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysUserSaveDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysUserResetPasswordDTO.java
- 新增: swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/admin/domain/dto/SysUserStatusDTO.java
- 修改: swiftboot-ui/src/utils/request.ts
- 验证: mvn -pl swiftboot-admin -am -DskipTests compile、npx.cmd vite build

---

## [2026-03-23 17:20] 更新摘要

### 📝 变更综述

本次更新把“测试学生表 / 测试菜单”从项目中彻底清理，并把“示例_项目表”升级为更完整的业务示范页。除了基础 CRUD，这一轮还补齐了附件上传、Excel 导入导出、勾选导出、模板下载，以及可配置化的通用操作栏组件，让该示例更适合作为后续业务模块的复制模板。

### 🚀 核心变更

- **测试模块清理**: 删除 `testStudent` 前端页面、接口、路由与后端 `com.swiftboot.student` 全链路代码，移除 `test_student.sql`，并补充旧库清理脚本。
- **项目示例增强**: `testProject` 新增导入、导出、模板下载、勾选导出、附件管理联动与保存后继续上传附件的编辑流程。
- **后端能力补齐**: `TestProjectController` / `TestProjectServiceImpl` 增加 Excel 导入导出、导入结果回执、项目编号唯一校验、预算与日期合法性校验。
- **操作区组件化**: 新增 `CrudActionToolbar` 通用组件，通过配置驱动按钮权限、禁用态、加载态和图标，方便其它列表页复用。
- **下载流兼容**: 前端 `request.ts` 增加 `blob` 响应兼容和下载失败提示解析，支持文件导出和模板下载场景。

### 🐛 问题修复

- 修复了“学生示例”仍作为正式菜单能力存在，影响演示与初始化基线纯净度的问题。
- 修复了项目示例页只有基础 CRUD、缺少导入导出与模板下载能力，示范价值不足的问题。
- 修复了附件必须先退出弹窗再重新进入才能继续管理的体验问题，新增后可直接留在弹窗继续上传附件。
- 修复了前端请求封装对二进制下载不友好、导出失败时难以正确提示的问题。

### 🔧 技术细节

- 修改: `swiftboot-backend/sql/swiftboot.sql`, `swiftboot-backend/sql/add_menus.sql`
- 新增: `swiftboot-backend/sql/remove_test_student_cleanup.sql`
- 删除: `swiftboot-backend/sql/test_student.sql`
- 删除: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/student/...`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/controller/TestProjectController.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/domain/entity/TestProject.java`
- 新增: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/domain/excel/TestProjectExcelVO.java`
- 新增: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/domain/vo/TestProjectImportResultVO.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/service/TestProjectService.java`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/service/impl/TestProjectServiceImpl.java`
- 新增: `swiftboot-ui/src/components/CrudActionToolbar/index.vue`
- 修改: `swiftboot-ui/src/views/testProject/index.vue`
- 修改: `swiftboot-ui/src/api/testProject.ts`
- 修改: `swiftboot-ui/src/utils/request.ts`
- 修改: `swiftboot-ui/src/router/modules/examples.ts`
- 删除: `swiftboot-ui/src/router/modules/testProject.ts`, `swiftboot-ui/src/router/modules/testStudent-routes.ts`
- 删除: `swiftboot-ui/src/api/testStudent.ts`, `swiftboot-ui/src/views/testStudent/index.vue`
- 修改: `swiftboot-ui/src/locales/zh-cn.ts`, `swiftboot-ui/src/locales/en.ts`
- 验证: `mvn -pl swiftboot-admin -am -DskipTests compile`、`npx.cmd vite build`

---

## [2026-03-23 15:32] 更新摘要

### 📝 变更综述

本次更新把“菜单树与产品分组建议”从说明文档真正落到了项目代码里。菜单初始化来源已统一到主 SQL，历史菜单补丁脚本已转为兼容迁移脚本；同时前端新增了新的产品化路由分组，示例模块也从测试命名升级为正式示例命名，菜单总览文档和 README 入口一并补齐。

### 🚀 核心变更

- **菜单初始化收口**: `swiftboot.sql` 升级为菜单权威基线，纳入岗位、任务、公告、消息、文件、配置、AI、监控、示例业务等完整菜单与按钮权限。
- **兼容迁移脚本**: `add_menus.sql` 重构为历史数据库兼容迁移脚本，统一修正 `sys_role_menu`、菜单父子关系与产品分组迁移。
- **产品化菜单重组**: 菜单分组调整为 `权限中心 / 平台中心 / 监控中心 / 开发工具 / 智能助手 / 示例业务`，前端新增对应路由模块承接。
- **示例模块升级**: “测试项目 / 测试学生”升级为“项目示例 / 学生示例”，前端页面标题、路由文案、后端控制器说明、实体与服务注释同步收口。
- **文档官方化**: 新增并完善 `SwiftBoot产品视角菜单树与重分组建议.md`，README 增加文档入口，并在文档中标注本轮 6 项事项已全部完成。

### 🐛 问题修复

- 修复了主菜单来源分散在 `swiftboot.sql`、`add_menus.sql`、历史 SQL 和前端路由中的问题。
- 修复了 `add_menus.sql` 中使用错误表名 `role_menu` 的问题，统一改为 `sys_role_menu`。
- 修复了配置管理菜单父级挂载错误的问题，避免继续落在旧的“系统管理”分组下。
- 修复了 AI 看板、基础资源、示例业务等真实能力未完整纳入主菜单基线的问题。
- 修复了示例模块长期使用“测试”命名，不利于对外演示和内部示范的问题。

### 🔧 技术细节

- 修改: `swiftboot-backend/sql/swiftboot.sql`, `swiftboot-backend/sql/add_menus.sql`
- 修改: `swiftboot-ui/src/router/modules/access.ts`, `platform.ts`, `develop.ts`, `assistant.ts`, `examples.ts`, `monitor.ts`
- 修改: `swiftboot-ui/src/views/dashboard/index.vue`, `swiftboot-ui/src/views/testProject/index.vue`, `swiftboot-ui/src/views/testStudent/index.vue`
- 修改: `swiftboot-ui/src/router/modules/testProject.ts`, `swiftboot-ui/src/router/modules/testStudent-routes.ts`
- 修改: `swiftboot-ui/src/api/testProject.ts`, `swiftboot-ui/src/api/testStudent.ts`, `swiftboot-ui/src/locales/zh-cn.ts`, `swiftboot-ui/src/locales/en.ts`
- 修改: `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/test/...`, `swiftboot-backend/swiftboot-admin/src/main/java/com/swiftboot/student/...`
- 新增/完善: `devDoc/说明文档/SwiftBoot产品视角菜单树与重分组建议.md`, `README.md`
- 验证: `npm.cmd run build`

---

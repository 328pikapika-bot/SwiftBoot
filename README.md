# SwiftBoot 现代化轻量级框架 🚀

> **不只是又一个后台管理框架，而是专注解决“重复劳动”与“上手成本”的开发利器。**

SwiftBoot 是一个基于 Spring Boot 3 + Vue 3 的轻量级后台框架，但我们不聊技术栈，只聊它**帮你省了什么事**：
**代码生成 + 字典治理 + 前后端一致性 + AI 辅助理解** —— 这是我们打磨最深的四个点。

---

## ✨ 为什么选择 SwiftBoot？

### 1. 📖 字典不再是“配置”，是“一等公民”
你是否厌倦了：前端写一遍枚举，后端写一遍枚举，数据库存个 `1`，查出来还得翻译？
*   **统一治理**：在系统管理中维护字典，前后端自动同步。
*   **前端零逻辑**：使用 `useDict` 钩子，一行代码获取字典数据；使用 `<DictTag>` 组件，自动回显样式。
*   **生成器联动**：代码生成器直接读取字典配置，生成的代码自动包含字典下拉框/单选框，**无需手写任何字典绑定逻辑**。

### 2. 🤖 AI 助手是“项目导师”，不仅是 ChatGPT
新接手项目最怕什么？不知道表关系、找不到代码入口、不懂业务逻辑。
*   **项目级理解**：内置 AI 助手（SwiftMate），它“认识”你的项目。
*   **懂你所问**：直接问“用户登录流程是怎么实现的？”、“`sys_user` 表和 `sys_dept` 怎么关联的？”，它能基于当前代码库给出准确答案。
*   **开箱即用**：全局悬浮窗，随时随地答疑解惑，新人上手速度提升 50%。

### 3. ⚙️ 真正“能用”的代码生成
*   **拒绝半成品**：生成的代码包含完整的增删改查、字典绑定、表单验证、权限控制。
*   **所见即所得**：字段类型、组件类型（下拉/单选/日期）、必填校验，全部可视化配置。

---

## ⚡ 30秒 快速启动

> 如果 5 分钟内跑不起来，那就是我们的失败。

### 1️⃣ 准备工作
*   JDK 17+
*   MySQL 8.0+
*   Redis 7.x+
*   Node.js 16+

### 2️⃣ 获取代码
```bash
git clone https://github.com/328pikapika-bot/SwiftBoot.git
cd SwiftBoot
```

### 3️⃣ 初始化数据库
1.  创建数据库 `swiftboot`。
2.  导入 SQL 脚本：`swiftboot-backend/sql/swiftboot.sql`。

### 4️⃣ 启动后端
```bash
cd swiftboot-backend
# 修改 application.yml 中的数据库/Redis配置
mvn spring-boot:run
```

### 5️⃣ 启动前端
```bash
cd swiftboot-ui
npm install
npm run dev
```

访问地址：`http://localhost:5173` (默认账号: `admin` / `123456`)

---

## 📸 功能预览

> *(此处建议补充 GIF/截图：展示代码生成器配置页面、字典管理页面、AI 助手对话浮窗)*

| 字典驱动代码生成 | AI 项目助手 |
| :---: | :---: |
| *[待补充截图]* | *[待补充截图]* |
| 配置字典，自动生成下拉框代码 | 询问项目架构，即时获得解答 |

---

## 🛠️ 技术栈清单

我们选择了最稳健的现代化技术栈，确保未来 5 年不过时。

*   **后端**：Spring Boot 3.2 + JDK 17 + MyBatis Plus + Sa-Token + Knife4j
*   **前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia
*   **工具**：Hutool + MapStruct + Undertow

---

## 🤝 参与共建

SwiftBoot 处于 **v0.x 高速迭代期**，这是最适合参与开源的阶段！
我们非常欢迎：
*   🐛 提交 Issue 反馈 Bug 或建议
*   📝 完善文档与注释
*   🌟 **Star 支持**，这是对我们最大的鼓励！

**作者信息**：
*   Gitee：[https://gitee.com/cs_shuang/SwiftBoot](https://gitee.com/cs_shuang/SwiftBoot)
*   GitHub：[https://github.com/328pikapika-bot/SwiftBoot](https://github.com/328pikapika-bot/SwiftBoot)
*   联系方式：17334981104 (同微信)

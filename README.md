# SwiftBoot 轻量级后台管理系统

一个基于 **Spring Boot 3 + Vue 3** 的现代化轻量级后台管理框架。

## 🚀 技术栈

### 后端
- **JDK 17** - 长期支持版本
- **Spring Boot 3.2.x** - 最新稳定版
- **MyBatis Plus 3.5.5** - ORM 框架
- **Sa-Token 1.37.0** - 轻量级权限认证
- **MySQL 8.0** - 数据库
- **Redis 7.x** - 缓存
- **Knife4j 4.4.0** - API 文档
- **Undertow** - Web 容器

### 前端
- **Vue 3.4.x** - 渐进式框架
- **Vite 5.x** - 构建工具
- **TypeScript** - 类型安全
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **UnoCSS** - 原子化 CSS

## 📁 项目结构

```
SwiftBoot/
├── swiftboot-backend/              # 后端项目
│   ├── swiftboot-common/           # 公共模块
│   │   ├── common-core/            # 核心工具
│   │   ├── common-redis/           # Redis 封装
│   │   ├── common-log/             # 日志切面
│   │   └── common-security/        # Sa-Token 封装
│   ├── swiftboot-admin/            # 后台管理模块
│   ├── swiftboot-generator/        # 代码生成器
│   └── sql/                        # 数据库脚本
└── swiftboot-ui/                   # 前端项目
```

## 🛠️ 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 7.x

### 后端启动

1. 创建数据库并执行 SQL 脚本：
```sql
source swiftboot-backend/sql/swiftboot.sql
```

2. 修改配置文件：
```yaml
# swiftboot-backend/swiftboot-admin/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/swiftboot
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
```

3. 启动应用：
```bash
cd swiftboot-backend
mvn clean install
mvn spring-boot:run -pl swiftboot-admin
```

### 前端启动

```bash
cd swiftboot-ui
npm install
npm run dev
```

## 📌 功能模块

### V1.0 核心功能
- ✅ 登录认证 - Sa-Token 实现
- ✅ 用户管理 - 增删改查、密码重置
- ✅ 角色管理 - 角色权限分配
- ✅ 菜单管理 - 动态路由、权限标识
- ✅ 部门管理 - 组织架构树
- ✅ 字典管理 - 字典类型、字典数据
- ✅ 操作日志 - AOP 自动记录
- ✅ 登录日志 - 登录记录
- ✅ 代码生成 - 前后端代码一键生成

## 🔐 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| swiftboot | 123456 | 普通用户 |

## 📖 API 文档

启动后端后访问：http://localhost:8080/doc.html

## 📄 License

MIT License

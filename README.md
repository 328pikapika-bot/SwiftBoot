# SwiftBoot

SwiftBoot 是一套面向 **开源 / 个人技术展示 / 中小型项目快速落地** 的现代化 Java 后端开发框架，基于 **JDK 17 + Spring Boot 3** 构建，强调 **轻量、规范、可扩展、易二次开发**。

> 目标不是“造一个大而全的轮子”，而是沉淀一套 **可长期维护、可直接复用、符合当前主流技术栈** 的工程化基础框架。

---

## ✨ 核心特性

* 🚀 **现代技术栈**：JDK 17 + Spring Boot 3.x（面向未来 5 年）
* 🧱 **模块化设计**：清晰的模块拆分，适合扩展为多业务系统
* 🔐 **轻量级权限体系**：基于 Sa-Token，学习成本低、代码侵入少
* 📄 **接口文档即开即用**：Knife4j（OpenAPI 3），开发调试友好
* ⚡ **性能优先**：Undertow 替代 Tomcat，启动更快、内存占用更低
* 🧰 **工程化友好**：统一异常、日志、配置、代码规范

---

## 🧩 技术栈

### 后端

| 组件           | 版本       | 说明                      |
| ------------ | -------- | ----------------------- |
| JDK          | 17 (LTS) | 长期支持版本，Spring Boot 3 必备 |
| Spring Boot  | 3.2.x    | 基于 Spring Framework 6   |
| MyBatis Plus | 3.5.x    | ORM 框架，简化 CRUD          |
| Sa-Token     | 1.37.x   | 轻量级认证与权限框架              |
| Redis        | 7.x      | 缓存 / Token / 分布式能力      |
| MySQL        | 8.0      | 主流关系型数据库                |
| Knife4j      | 4.x      | 接口文档（OpenAPI 3）         |
| Hutool       | 5.8.x    | Java 工具库                |
| MapStruct    | 1.5.x    | 对象映射（编译期生成）             |
| Undertow     | 内置       | 高性能 Web 容器              |

### 前端（可选）

| 组件           | 版本  | 说明     |
| ------------ | --- | ------ |
| Vue          | 3.x | 主流前端框架 |
| Vite         | 5.x | 极速构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Pinia        | 2.x | 状态管理   |
| TypeScript   | 5.x | 类型安全   |

---

## 📦 项目结构

```text
SwiftBoot
├── swift-common            # 公共模块
│   ├── common-core         # 核心工具（异常、常量、通用类）
│   ├── common-redis        # Redis 封装
│   ├── common-security     # Sa-Token 权限封装
│   └── common-log          # 日志与审计
│
├── swift-admin             # 后台管理模块
│   ├── admin-api           # Controller 层
│   └── admin-service       # 业务逻辑层
│
├── swift-generator         # 代码生成器（可选）
│
├── swift-storage           # 文件存储模块
│   ├── storage-local       # 本地存储
│   ├── storage-oss         # 阿里云 OSS
│   ├── storage-cos         # 腾讯云 COS
│   └── storage-minio       # MinIO
│
└── swift-ui                # 前端项目（Vue 3）
```

---

## 🚀 快速启动

### 1️⃣ 环境要求

* JDK 17+
* Maven 3.9+
* MySQL 8.0+
* Redis 7.x+

### 2️⃣ 获取代码

```bash
git clone https://github.com/328pikapika-bot/SwiftBoot.git
cd SwiftBoot
```

### 3️⃣ 数据库配置

创建数据库：

```sql
CREATE DATABASE swiftboot DEFAULT CHARACTER SET utf8mb4;
```

修改配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/swiftboot
    username: root
    password: root
```

### 4️⃣ 启动后端

```bash
mvn clean install
mvn spring-boot:run
```

启动成功后：

* 后端接口：`http://localhost:8080`
* 接口文档：`http://localhost:8080/swagger-ui.html`

---

## 🔐 权限设计（Sa-Token）

* Token 登录认证
* 角色 / 权限标识控制
* 注解式鉴权（`@SaCheckLogin`, `@SaCheckPermission`）
* 支持 Redis 持久化

---

## 🧠 设计理念

* **约定优于配置**：减少无意义配置
* **模块解耦**：每个模块可独立演进
* **低学习成本**：不引入复杂、重型框架
* **面向实际项目**：不是 Demo，而是可直接用于生产的骨架

---

## 📌 适用场景

* 中后台管理系统
* 企业内部系统
* 教育 / 业务管理平台
* 个人开源项目 / 技术展示
* 二次开发基础框架

---

## 📄 开源协议

本项目基于 **Apache License 2.0** 开源，欢迎学习、使用和二次开发。

---

## 🙌 作者

* Author：cs_shuang
* Mobile: 17334981104(同微信)
* Gitee：[https://gitee.com/cs_shuang/SwiftBoot.git](https://github.com/328pikapika-bot)
* GitHub：[https://github.com/328pikapika-bot/SwiftBoot](https://github.com/328pikapika-bot)

如果你觉得 SwiftBoot 对你有帮助，欢迎 ⭐ Star 支持。

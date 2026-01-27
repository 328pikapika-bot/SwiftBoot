明白，你只想要 **围绕 Git 仓库、分支策略、版本规划和操作指南** 的专业 README 文档，不涉及技术栈和目录结构。我帮你整理如下 Markdown 文档：

---

# SwiftBoot

**SwiftBoot** 是一个面向中小系统 / 教学项目 / 个人开发者的 **Spring Boot 快速开发框架**，旨在提供开箱即用、结构清晰、易于二次开发的工程模板。

> 仓库主仓：Gitee（国内开发）
> 镜像仓库：GitHub（对外展示）

---

## 仓库信息

| 类型            | 地址                                                     |
| ------------- | ------------------------------------------------------ |
| 主仓库（默认 push）  | [Gitee](git@gitee.com:cs_shuang/SwiftBoot.git)         |
| 镜像仓库（备用 / 展示） | [GitHub](git@github.com:328pikapika-bot/SwiftBoot.git) |

**远程仓库查看命令：**

```bash
git remote -v
```

输出示例：

```text
origin  git@gitee.com:cs_shuang/SwiftBoot.git (fetch)
origin  git@gitee.com:cs_shuang/SwiftBoot.git (push)
github  git@github.com:328pikapika-bot/SwiftBoot.git (fetch)
github  git@github.com:328pikapika-bot/SwiftBoot.git (push)
```

---

## 分支策略

| 分支     | 作用                                  |
| ------ | ----------------------------------- |
| `main` | 稳定分支，用于发布版本（默认 push 到 Gitee）        |
| `dev`  | 日常开发分支（可选）                          |
| Tag    | `v0.1.0`, `v0.2.0`, `v1.0.0` 用于版本标记 |

> 提示：IDEA 默认 push 会使用 `origin`，即 Gitee 仓库。

---

## 版本规划

### v0.1.0 – 初始骨架

* 框架骨架可运行
* README 初步说明
* 打 Tag：`v0.1.0`

### v0.2.0 – 实用增强

* 添加示例模块（如 user / demo）
* 全局异常处理
* 统一返回体 / 基础日志
* 对外演示可直接使用

### v0.3.0 – 开发效率版

* 参数校验
* 分页封装
* 枚举规范
* Swagger / Knife4j 接口文档（可选）

### v1.0.0 – 正式稳定版

* 核心结构稳定
* 核心工具类稳定
* README 完整
* 示例模块清晰，便于二次开发

---

## 推送 & 同步指南

```bash
# 默认 push 到 Gitee
git push origin main

# 同步到 GitHub（可选）
git push github main

# 查看远程仓库
git remote -v
```

> 建议：将 Gitee 作为主仓库，GitHub 仅做展示和镜像。

---
##SwiftBoot 版本打标签指南

```bash
# 切到 main 分支并拉取最新代码
git checkout main
git pull origin main
# 打标签
git tag -a v0.1.0 -m "SwiftBoot v0.1.0 初始骨架"
# 推送代码到远程
git push origin main
git push github main
# 推送标签到远程
git push origin --tags
git push github --tags
---------------------------------------------------------
#推送单个标签
# 推送到 Gitee
git push origin v0.1.0
# 推送到 GitHub
git push github v0.1.0
#查看本地 Tag
git tag
git show v0.1.0   # 查看具体 commit 信息

```

---
## 使用建议

1. 提交时遵循 Commit 规范，如 `feat: add user module`
2. 版本发布时打 Tag，例如：

```bash
git tag v0.1.0
git push origin v0.1.0
```

3. 避免在 main 分支直接开发，可在 dev 分支完成后合并

---

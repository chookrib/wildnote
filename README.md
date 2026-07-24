# WildNote

一个自托管的本地笔记浏览与管理 Web 应用，基于本地文件系统，无需数据库。

## 核心功能

- **本地文件即笔记** — 扫描指定目录下文件，以树状结构展示，支持目录嵌套。实时监听笔记目录变化（新增/删除/修改）
- **Markdown** — 支持 Markdown 格式笔记编辑与预览
- **定时提醒** — 在笔记中使用 cron 表达式设置提醒任务，自动触发通知，支持钉钉、阿里云短信等多渠道推送
- **全文搜索** — 支持 ripgrep 全文检索笔记
- **文件搜索** — 支持 Everything（HTTP Server）文件搜索
- **Webhook记录信息** — 支持追加、插入内容到笔记
- **Webhook记录网址** — 支持识别网址并保存到笔记，通过 Playwright + Chrome CDP 访问网页后使用 AI 提取标题和日期

## 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Java 17, Spring Boot 3.5 |
| 前端 | TypeScript, Vite, Vue 3, Ant Design Vue |

## 项目结构

```
wildnote/
├── wildnote-backend/      # 后端项目 (Java / Spring Boot)
│   ├── src/main/java/     # 后端源码
│   ├── src/main/resources/# Spring Boot 配置
│   └── pom.xml            # Maven 依赖
├── wildnote-frontend/     # 前端项目 (TypeScript / Vue 3)
│   ├── src/               # 前端源码
│   └── package.json       # npm 依赖
└── README.md
```

## 快速开始

以下说明以 Windows 系统为例。

### 运行环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.9+ | 后端构建工具 |
| Node.js | 22+ | 前端构建环境 |

### 外部工具要求

外部工具需在后端项目配置文件中配置正确，外部工具均为可选，不影响程序运行，只影响外部工具涉及的功能。

- 全文检索笔记功能需要有 ripgrep 可执行文件。
- 文件搜索功能需要运行 Everything 并启用 HTTP Server。
- Webhook记录网址功能需要运行 Chrome（启用RDP）浏览器，可使用以下命令启动：
    ```bat
    start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" ^
    --remote-debugging-port=9222 ^
    --user-data-dir="C:\your-path\chrome-rdp-9222-user-data-dir"
    ```

---

### 1. 打包后端

```bat
cd wildnote-backend
mvnw package
```

打包产物位于 `wildnote-backend\target\` 目录，jar 文件命名格式为 `wildnote-backend-RELEASE-20260101120000.jar`。

### 2. 打包前端

```bat
cd wildnote-frontend
npm install
npm run build
```

构建产物位于 `wildnote-frontend\dist\` 目录。运行后端需配置 `spring.config.additional-location` 指向前端构建产物目录。

### 3. 开发环境启动

后端开发模式：
```bat
cd wildnote-backend
mvnw spring-boot:run
```

前端开发模式：
```bash
cd wildnote-frontend
npm run dev
```

### 4. 生产环境运行

使用以下批处理启动项目，可自动选择最新打包的 jar 文件：
```bat
@echo off
title wildnote
cd /d "D:\your-path\wildnote\wildnote-backend"

if not exist "target\*.jar" (
    echo jar file not found, please build first.
    exit /b 1
)

for /f "delims=" %%i in ('dir target\*.jar /b /o:-n') do (
    set PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
    java -jar "target\%%i" --spring.config.additional-location=your-path\application-prod.yaml
    goto :EOF
)
```
> 由于使用的是外部 Chrome，可以设置环境变量 PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 跳过 Playwight 浏览器下载。

### 5. 访问

用浏览器打开 `http://localhost:8080`，默认账号密码为 `admin / admin`。

> ⚠️ 生产环境请务必修改 `app.auth-username` 和 `app.auth-password`。

## 后端主要配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 服务端口 |
| `app.auth-by` | `app` | 指定基于何处认证信息进行认证，app \| setting，app=基于应用配置文件认证，setting=基于用户配置文件认证|
| `app.auth-username` | `admin` | 登录用户名 |
| `app.auth-password` | `admin` | 登录密码 |
| `app.auth-jwt-secret` | `secret` | JWT 密钥 |
| `app.auth-jwt-expires` | `30d` | JWT 过期时间，值为时长（正整数）加时长单位（d/h/m）|
| `app.note-root-path` | `.\src` | 笔记根路径（绝对/相对路径均可） |
| `app.note-extensions` | `.md\|.txt` | 支持的笔记文件扩展名，`\|` 分隔 |
| `app.note-path-prefix-excludes` | | 需要忽略的路径前缀，`\|` 分隔 |
| `app.cron-filename-filters` | `todo\|待办` | 笔记内容 cron 任务识别过滤器，笔记路径中需包含这些名称，`\|` 分隔 |
| `app.cron-expression-prefix` | `> cron` | 笔记内容 cron 任务识别前缀 |
| `app.cron-expression-separator` | `\|` | 笔记内容 cron 任务分隔符 |
| `app.setting-file-path` | `wildnote.json` | 用户配置文件路径（相对于笔记根路径），认证、收藏、Webhook配置 |
| `app.extra-log-root-path` | `log\` | 日志保存路径，笔记监听、通知、短信日志|
| `app.ripgrep-exe-path` | `rg.exe` | ripgrep 可执行文件路径 |
| `app.everything-http-port` | `9221` | Everything HTTP Server 端口 |
| `app.chrome-cdp-port` | `9222` | Chrome CDP 远程调试端口 |
| `app.remind-dingtalk-key` | | 钉钉机器人 Access Token |
| `app.remind-dingtalk-secret` | | 钉钉机器人 Secret |
| `app.remind-dingtalk-chatid` | | 钉钉机器人 Chat ID |
| `app.sms-aliyun-key` | | 阿里云短信 AccessKey ID |
| `app.sms-aliyun-secret` | | 阿里云短信 AccessKey Secret |
| `app.sms-aliyun-sign` | | 阿里云短信签名 |
| `app.sms-aliyun-template` | | 阿里云短信模板 |
| `app.openai-base-url` | | OpenAI 兼容接口地址 |
| `app.openai-api-key` | | OpenAI API Key |
| `app.openai-model` | | OpenAI 模型名称 |

# E-LOG 企业级日志实时分析系统

![Version](https://img.shields.io/badge/version-v2.0-blue)
![Vue](https://img.shields.io/badge/Vue-3.x-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green)
![Flink](https://img.shields.io/badge/Flink-1.17-orange)

基于 Flume+Kafka+Flink/Spark+HDFS+Vue3 的分布式日志分析平台，数据来源为 GitHub 真实事件。

---

## 📁 项目结构

```
E-LOG-完整项目/
├── src/                          # 前端 Vue3 源码
│   ├── main.js
│   ├── App.vue                   # 主组件（MiMo风格UI）
│   ├── style.css                 # 全局样式
│   ├── api/
│   │   └── index.js              # API 请求封装
│   └── components/
│       └── ThreeBackground.vue    # Three.js 粒子背景
├── docker/                       # Docker 配置
│   ├── flume/conf/               # Flume 配置
│   ├── kafka/
│   ├── nginx/
│   └── spark/jobs/               # Spark 作业
├── pom.xml                       # Maven 配置
├── package.json                  # NPM 配置
├── vite.config.js                # Vite 构建配置
├── index.html                    # HTML 入口
├── README.md                     # 项目说明
└── 部署配置文档.md                # 部署文档
```

---

## 🏗️ 系统架构

```
┌─────────────┐     ┌──────────────────┐     ┌─────────┐
│  GitHub API  │ ──> │ GitHubEventFetcher│ ──> │  Kafka  │
│  (真实数据)   │     │   (每5秒采集)     │     │         │
└─────────────┘     └──────────────────┘     └────┬────┘
                                                   │
                          ┌────────────────────────┴────────────────────────┐
                          ↓                                                 ↓
                   ┌─────────────┐                                  ┌─────────────┐
                   │   Flink     │                                  │   Spark     │
                   │  实时处理   │                                  │  批处理    │
                   └──────┬──────┘                                  └──────┬──────┘
                          ↓                                                 ↓
                   ┌─────────────────────────────────────────────────────────────┐
                   │                        HDFS                                   │
                   └─────────────────────────────────────────────────────────────┘
                          ↓                                                 ↓
                   ┌─────────────┐                                  ┌─────────────┐
                   │ Spring Boot │                                  │   Vue3      │
                   │   API服务   │                                  │   大屏      │
                   └─────────────┘                                  └─────────────┘
```

---

## 🚀 快速开始

### 前置环境

- JDK 8+
- Node.js 18+
- MySQL 8.0+
- Kafka 2.x
- HDFS 3.x
- Flink 1.17 (可选)
- Docker (可选)

### 1. 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS log_analysis DEFAULT CHARACTER SET utf8mb4;
```

### 2. 启动后端

```bash
cd src/main/resources
# 修改 application.yml 中的数据库/Kafka/HDFS 地址
cd ../../..
mvn clean package -DskipTests
java -jar target/e-log-ca-system-1.0-SNAPSHOT.jar
```

### 3. 启动前端

```bash
npm install
npm run dev
```

### 4. 访问

- 前端: http://localhost:5173/
- 后端 API: http://localhost:8089/

---

## 🎨 前端技术栈

| 技术 | 作用 |
|------|------|
| Vue3 (Composition API) | 前端框架 |
| Vite 5 | 构建工具（热更新、代码分割） |
| ECharts 5 | 数据可视化（5个图表） |
| Three.js | 3D 粒子背景（WebGL Shader） |
| Axios | HTTP 请求 |

### 核心功能

1. **MiMo 风格 UI** - 极简叙事式布局，Indigo-Purple-Cyan 配色
2. **Three.js Shader 粒子背景** - 3000 粒子 + 鼠标排斥力场
3. **滚动驱动动画** - 视差滚动 + 交错入场 + 数字滚动
4. **实时告警面板** - critical/warning/info 三级告警
5. **同比环比分析** - 日对比/周对比柱状图
6. **数据导出** - JSON 格式报告下载

---

## ⚙️ 后端技术栈

| 技术 | 作用 |
|------|------|
| Spring Boot 2.7 | Web 框架 |
| MySQL 8.0 | 关系型数据库 |
| Kafka | 消息队列 |
| Flink | 实时流处理 |
| Spark | 批处理 |
| HDFS | 分布式文件系统 |
| Lombok | 简化代码 |

### API 端点

| 端点 | 说明 |
|------|------|
| `GET /api/overview` | 总览数据 (PV/UV/错误/响应时间) |
| `GET /api/pvuv` | PV/UV 24 小时统计 |
| `GET /api/topN` | Top API 排行 |
| `GET /api/errors` | 错误日志列表 |
| `GET /api/health` | 系统健康状态 |
| `GET /api/log-levels` | 日志级别统计 |
| `GET /api/servers` | 服务器监控 |

---

## 📊 数据来源

**GitHub Public Events API**
- 端点: https://api.github.com/events
- 采集频率: 每 5 秒
- 事件类型: PushEvent, PullRequestEvent, IssuesEvent, WatchEvent, ForkEvent 等

---

## 🐛 常见问题

### 1. HDFS 连接失败
```
原因: Docker 网络问题，DataNode 返回容器内部地址
解决: 在 /etc/hosts 中添加: 127.0.0.1 datanode
```

### 2. Kafka 消费失败
```
原因: advertised.listeners 配置问题
解决: 使用 --network host 模式启动容器
```

### 3. Flink 无法启动
```
原因: 缺少 Flink 环境
解决: docker run -d --name flink-jobmanager --network host flink:1.17.1-java8 jobmanager
```

---

## 📄 许可证

MIT License

---

## 👥 团队成员

- 成员1: GitHub API 数据采集
- 成员2: Flink 实时处理
- 成员3: Spark 批处理
- 成员4: Spring Boot 后端
- 成员5: Vue3 前端可视化

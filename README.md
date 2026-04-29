# E-LOG 企业数据管理平台

> 轻量级企业数据管理平台，支持多数据源接入、HDFS文件管理、数据同步任务调度

![Java](https://img.shields.io/badge/Java-8+-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green) ![License](https://img.shields.io/badge/License-MIT-blue)

## 🎯 功能特性

| 模块 | 功能 | 说明 |
|------|------|------|
| **用户认证** | 注册/登录/JWT | 角色权限支持 |
| **数据源管理** | CRUD + 连接测试 | MySQL / PostgreSQL |
| **HDFS文件管理** | 上传/下载/删除/目录 | 浏览器式管理界面 |
| **任务调度** | CRUD + 手动执行 + 启用停用 | Cron表达式配置 |
| **移动端监控** | 概览/数据源/任务/HDFS/个人 | 手机端适配，随时查看 |
| **仪表盘** | 系统统计 + 健康检查 | 数据源数量/任务数/HDFS状态 |

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8 + Spring Boot 2.7 |
| 持久化 | MyBatis Plus + MySQL |
| 文件存储 | Apache Hadoop HDFS |
| 安全 | Spring Security + JWT |
| 前端 | HTML + CSS + JavaScript（原生，无框架） |
| 移动端 | 独立移动端页面（mobile.html） |

## 📱 移动端

支持手机浏览器访问，地址：`http://192.168.146.128:8080/webapp/mobile.html`

功能：概览仪表盘、数据源列表、任务状态、HDFS文件浏览、个人设置

## 🚀 快速启动

### 前置要求
- JDK 8+
- Maven 3.6+
- MySQL 5.7+
- Hadoop 3.x (HDFS)

### 1. 初始化数据库
```bash
mysql -h 192.168.146.128 -u root -p < sql/init.sql
```

### 2. 修改配置
编辑 `src/main/resources/application.yml`

### 3. 编译运行
```bash
mvn clean package -DskipTests
java -jar target/e-log-platform-1.0.0.jar
```

### 4. 访问
- 平台地址: http://192.168.146.128:8080
- 默认管理员: `admin` / `admin123`
- 移动端: http://192.168.146.128:8080/webapp/mobile.html

## 📁 项目结构

```
E-LOG/
├── src/main/java/com/elog/
│   ├── ELogApplication.java      # 启动类
│   ├── config/                   # 配置类
│   │   ├── SecurityConfig.java   # 安全配置
│   │   └── WebConfig.java        # Web配置
│   ├── controller/               # 控制器
│   │   ├── AuthController.java   # 认证
│   │   ├── DataSourceController.java  # 数据源
│   │   ├── HdfsController.java   # HDFS文件
│   │   ├── SyncTaskController.java    # 任务调度
│   │   └── SystemController.java # 系统/仪表盘
│   ├── entity/                   # 实体类
│   ├── mapper/                   # MyBatis Mapper
│   ├── service/                  # 服务层
│   │   └── impl/                 # 服务实现
│   └── util/                     # 工具类
│       ├── JwtUtil.java          # JWT工具
│       └── Result.java           # 统一响应
├── src/main/resources/
│   ├── mapper/                   # Mapper XML
│   └── application.yml           # 应用配置
├── webapp/                       # 前端资源
│   ├── index.html                # 桌面端主页
│   ├── mobile.html               # 移动端页面
│   ├── css/
│   │   ├── style.css             # 桌面端样式
│   │   └── mobile.css            # 移动端样式
│   └── js/
│       ├── api.js                # API调用封装
│       ├── app.js                # 主应用逻辑
│       └── mobile-app.js         # 移动端逻辑
└── sql/
    └── init.sql                  # 数据库初始化
```

## 🔌 API接口

### 认证
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |

### 数据源
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/datasource/list` | GET | 获取数据源列表 |
| `/api/datasource/add` | POST | 添加数据源 |
| `/api/datasource/{id}` | DELETE | 删除数据源 |
| `/api/datasource/test` | POST | 测试连接 |

### HDFS
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/hdfs/list?path=` | GET | 获取文件列表 |
| `/api/hdfs/upload` | POST | 上传文件 |
| `/api/hdfs/download?path=` | GET | 下载文件 |
| `/api/hdfs/delete?path=` | DELETE | 删除文件/目录 |
| `/api/hdfs/mkdir?path=` | POST | 创建目录 |

### 任务调度
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/task/list` | GET | 获取任务列表 |
| `/api/task/add` | POST | 创建任务 |
| `/api/task/{id}/toggle` | PUT | 启用/停用 |
| `/api/task/{id}/execute` | POST | 手动执行 |
| `/api/task/{id}` | DELETE | 删除任务 |

### 系统
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/system/health` | GET | 健康检查 |
| `/api/system/dashboard` | GET | 仪表盘统计 |

## 📝 开发团队

- **甲方**: SZJ
- **产品经理**: AI助手
- **项目经理**: AI助手
- **开发**: AI助手
- **测试**: AI助手
- **运维**: AI助手

## 📄 License

MIT License
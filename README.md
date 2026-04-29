# E-LOG 企业数据管理平台

> 轻量级企业数据管理平台，支持多数据源接入、HDFS文件管理、数据同步任务调度。

## 🎯 功能特性

- **用户认证** - 用户注册、登录、JWT Token认证
- **数据源管理** - 支持MySQL、PostgreSQL、HDFS、API等多种数据源
- **HDFS文件管理** - 浏览器式文件管理、上传/下载/删除/创建目录
- **任务调度** - Cron表达式配置的数据同步任务（规划中）

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8 + Spring Boot 2.7 |
| 持久化 | MyBatis Plus + MySQL |
| 文件存储 | Apache Hadoop HDFS |
| 安全 | Spring Security + JWT |
| 前端 | HTML + CSS + JavaScript (原生) |

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

编辑 `src/main/resources/application.yml`，确认以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.146.128:3306/elog
    username: root
    password: root

hadoop:
  namenode: hdfs://192.168.146.128:9000
```

### 3. 编译运行

```bash
mvn clean package -DskipTests
java -jar target/e-log-platform-1.0.0.jar
```

### 4. 访问

- 平台地址: http://192.168.146.128:8080
- 默认管理员: `admin` / `admin123`

## 📁 项目结构

```
E-LOG/
├── src/main/java/com/elog/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── entity/          # 实体类
│   ├── mapper/          # MyBatis Mapper
│   ├── service/         # 服务层
│   └── util/            # 工具类
├── src/main/resources/
│   ├── mapper/          # Mapper XML
│   └── application.yml  # 应用配置
├── webapp/              # 前端资源
│   ├── css/
│   ├── js/
│   └── index.html
└── sql/
    └── init.sql         # 数据库初始化
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
| `/api/hdfs/list` | GET | 获取文件列表 |
| `/api/hdfs/upload` | POST | 上传文件 |
| `/api/hdfs/download` | GET | 下载文件 |
| `/api/hdfs/delete` | DELETE | 删除文件/目录 |
| `/api/hdfs/mkdir` | POST | 创建目录 |

## 📝 开发团队

- **甲方**: SZJ
- **产品经理**: AI助手
- **项目经理**: AI助手
- **开发**: AI助手
- **测试**: AI助手
- **运维**: AI助手

## 📄 License

MIT License
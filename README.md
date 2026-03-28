# Orchestration - 流程编排平台

基于 **Flowable** + **bpmn.js** 的前后端分离流程编排系统，支持可扩展图元（触发器、逻辑控制器、执行器）和双实例部署。

## 架构概览

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Vue 3)                   │
│         bpmn.js Designer + Custom Palette            │
│       Element Plus UI + Custom Properties Panel      │
└──────────────────────┬──────────────────────────────┘
                       │ REST API
         ┌─────────────┴─────────────┐
         ▼                           ▼
┌─────────────────┐       ┌─────────────────┐
│   Instance-1    │       │   Instance-2    │
│  (Spring Boot)  │       │  (Spring Boot)  │
│  Flowable Engine│       │  Flowable Engine│
└────────┬────────┘       └────────┬────────┘
         │      Shared Resources    │
    ┌────┴──────────────────────────┴────┐
    │  MySQL │ Redis (Lettuce) │ Kafka   │
    └────────────────────────────────────┘
```

## 核心概念

### 三种可扩展图元

| 图元 | BPMN映射 | 说明 |
|------|----------|------|
| **触发器 (Trigger)** | Message/Signal Start Event | 基于消息/信号触发流程，支持 Kafka 订阅 |
| **逻辑控制器 (Logic Controller)** | ServiceTask + delegateExpression | 条件判断、数据转换等逻辑处理 |
| **执行器 (Executor)** | ServiceTask + delegateExpression | HTTP调用、脚本执行等外部操作 |

### 扩展机制

- **ExecutorHandler** 接口：实现 `getType()` + `execute()` 方法，注册为 Spring Bean 即自动发现
- **LogicHandler** 接口：同上，用于逻辑控制
- **TriggerHandler** 接口：处理触发器事件，解析 payload 为流程变量

### 双实例运行

- 两个后端实例共享同一 MySQL + Redis + Kafka
- Flowable 异步执行器通过数据库行锁实现任务抢占
- Kafka 触发器通过 Redis 分布式锁确保消息只被一个实例处理
- Nginx 做前端负载均衡

## 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3 + bpmn.js + Element Plus + Vite |
| 后端 | Spring Boot 3.2 + Flowable 7.0 |
| 消息 | Apache Kafka |
| 缓存/锁 | Redis + Lettuce |
| 数据库 | MySQL 8.0 |
| 容器化 | Docker Compose |

## 快速开始

### Docker Compose 一键启动

```bash
docker-compose up -d
```

服务端口：
- 前端：http://localhost:3000
- 后端实例1：http://localhost:8081
- 后端实例2：http://localhost:8082

### 本地开发

**后端：**
```bash
cd orchestration-backend
# 需要本地 MySQL/Redis/Kafka
mvn spring-boot:run -Dspring-boot.run.arguments="--INSTANCE_ID=instance-1"
```

**前端：**
```bash
cd orchestration-frontend
npm install
npm run dev
```

## 项目结构

```
orchestration/
├── docker-compose.yml                    # 双实例 + 基础设施
├── orchestration-backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/orchestration/
│       ├── config/                       # Redis(Lettuce), Flowable, Kafka, CORS
│       ├── controller/                   # REST API
│       ├── service/                      # 业务逻辑 + 分布式锁
│       ├── engine/
│       │   ├── executor/                 # 执行器: Handler接口 + Registry + 内置实现
│       │   ├── logic/                    # 逻辑控制器: Handler接口 + Registry + 内置实现
│       │   ├── trigger/                  # 触发器: Handler接口 + Registry + 内置实现
│       │   └── listener/                 # Kafka消息监听器
│       ├── model/                        # Entity + DTO
│       └── repository/                   # JPA Repository
├── orchestration-frontend/
│   ├── package.json
│   ├── Dockerfile / nginx.conf
│   └── src/
│       ├── components/
│       │   ├── designer/
│       │   │   ├── BpmnDesigner.vue      # bpmn.js 流程设计器
│       │   │   ├── PropertiesPanel.vue   # 自定义属性面板
│       │   │   └── custom/              # 自定义调色板 + 上下文菜单
│       │   ├── ProcessList.vue           # 流程定义管理
│       │   ├── TriggerList.vue           # 触发器管理
│       │   └── InstanceList.vue          # 运行实例监控
│       └── api/                          # API 客户端
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/process-definitions` | 流程定义列表 |
| POST | `/api/process-definitions` | 保存流程定义 |
| POST | `/api/process-definitions/{id}/deploy` | 部署到Flowable |
| POST | `/api/process-instances/start` | 启动流程实例 |
| POST | `/api/process-instances/signal` | 发送信号触发 |
| POST | `/api/process-instances/message` | 发送消息触发 |
| GET | `/api/triggers` | 触发器列表 |
| POST | `/api/triggers` | 创建触发器 |
| GET | `/api/registry/executor-types` | 已注册执行器类型 |
| GET | `/api/registry/logic-types` | 已注册逻辑控制器类型 |

## 扩展开发

### 添加新的执行器

```java
@Component
public class MyCustomExecutor implements ExecutorHandler {
    @Override
    public String getType() { return "MY_CUSTOM"; }

    @Override
    public Object execute(DelegateExecution execution, String config) {
        // 自定义执行逻辑
        return "result";
    }
}
```

### 添加新的 Kafka 触发器

1. 在触发器管理页面创建触发器，类型选 `KAFKA_MESSAGE`，触发源填 Kafka Topic 名称
2. 该 Topic 的消息将自动触发关联的流程

### BPMN XML 中使用自定义图元

```xml
<serviceTask id="exec1" name="我的执行器"
  flowable:delegateExpression="${executorDelegate}">
  <extensionElements>
    <flowable:field name="executorType" stringValue="MY_CUSTOM" />
    <flowable:field name="executorConfig" stringValue='{"param":"value"}' />
  </extensionElements>
</serviceTask>
```

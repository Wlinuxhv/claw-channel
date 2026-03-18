# 功能清单 - Claw Channel

## MVP (最小可行产品) 功能

### 第一阶段 - 核心功能
- [ ] 用户注册/登录
- [ ] 一对一聊天（文字）
- [ ] WebSocket 实时消息推送
- [ ] 基础 Webhook 接收接口
- [ ] 简单设备控制指令发送

### 第二阶段 - 增强功能
- [ ] 群聊支持
- [ ] 图片消息
- [ ] Webhook 配置管理界面
- [ ] 设备状态监控面板
- [ ] 消息历史记录

### 第三阶段 - 高级功能
- [ ] 语音消息
- [ ] 定时任务（自动投喂等）
- [ ] 数据可视化（水质图表等）
- [ ] 告警通知推送
- [ ] 多养殖场管理

---

## Webhook 控制场景示例

### 场景 1: 水质异常告警
```json
// 外部传感器系统发送告警
POST https://your-server.com/api/webhook/receive/water-quality-alert
{
  "pond_id": "pond-001",
  "alert_type": "ammonia_high",
  "value": 0.8,
  "threshold": 0.5,
  "timestamp": "2025-03-18T10:30:00Z"
}

// 系统自动回复到聊天
"⚠️ 警告：1 号池氨氮含量超标 (0.8 > 0.5)，建议立即换水！"
```

### 场景 2: 远程投喂控制
```json
// 用户在聊天中发送指令 或 客户端发送
POST https://your-server.com/api/device/feed
{
  "pond_id": "pond-001",
  "amount": 500,  // 克
  "duration": 30  // 秒
}

// 返回执行结果
{
  "status": "success",
  "message": "投喂机已启动，预计 30 秒后完成"
}
```

### 场景 3: 定时增氧
```json
// 设置定时任务
POST https://your-server.com/api/schedule/create
{
  "name": "夜间增氧",
  "cron": "0 22 * * *",  // 每晚 22 点
  "action": "aerator_on",
  "pond_id": "pond-001",
  "duration": 480  // 分钟
}
```

---

## 消息类型定义

| 消息类型 | 说明 | 示例 |
|----------|------|------|
| `text` | 文字消息 | "今天水温正常" |
| `command` | 控制指令 | "/feed pond-001 500g" |
| `alert` | 系统告警 | "⚠️ 水温过低" |
| `image` | 图片消息 | 水质检测照片 |
| `status` | 设备状态 | "增氧泵：运行中" |

---

## 数据库表结构草案

### users 表
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    created_at DATETIME,
    last_login DATETIME
);
```

### messages 表
```sql
CREATE TABLE messages (
    id INTEGER PRIMARY KEY,
    sender_id INTEGER,
    receiver_id INTEGER,
    content TEXT,
    message_type VARCHAR(20),
    created_at DATETIME,
    read_status BOOLEAN
);
```

### devices 表
```sql
CREATE TABLE devices (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(50),  // feeder, aerator, sensor
    pond_id VARCHAR(50),
    status VARCHAR(20),
    last_seen DATETIME
);
```

### webhooks 表
```sql
CREATE TABLE webhooks (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100),
    url VARCHAR(255),
    secret VARCHAR(255),
    events TEXT,  // JSON array
    created_at DATETIME
);
```

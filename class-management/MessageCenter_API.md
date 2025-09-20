## 消息中心 & 模板接口文档 (后端)

仅涵盖已实现的通知 / 模板相关接口。所有接口当前未加鉴权示例，若后续引入登录态请在 Header 增加会话/Token。

基础说明:
- 所有时间字段均为服务器所在时区的时间戳或格式化字符串（若返回）。
- userId 由前端从当前登录用户上下文获取；本阶段直接作为参数传递。
- 变量占位符语法: `{变量名}`，渲染时简单文本替换。

变量清单 (请假领域):
| 变量 | 说明 | 示例 |
|------|------|------|
| leaveId | 请假申请ID | 123 |
| studentName | 学生姓名 | 张三 |
| studentNo | 学号 | 20240001 |
| className | 班级 | 计科23-1 |
| departmentName | 系部 | 计算机系 |
| leaveTypeName | 请假类型 | 病假 |
| startDate | 开始日期(yyyy-MM-dd) | 2025-09-17 |
| endDate | 结束日期(yyyy-MM-dd) | 2025-09-19 |
| days | 时长(天) | 2 |
| currentStepName | 当前审批步骤名 | 系主任审批 |
| rejectReason | 拒绝原因 | 资料不全 |

### 一、通知收件箱相关

#### 1. 获取收件箱
GET `/api/notifications/inbox?userId={userId}&limit=50`

示例:
```
GET /api/notifications/inbox?userId=10&limit=20
```
响应(示例):
```json
[
  {
    "notificationId": 101,
    "recipientId": 5551,
    "title": "张三 的请假申请待审批",
    "content": "学生：张三...",
    "type": "LEAVE_SUBMITTED",
    "priority": "NORMAL",
    "channels": ["INBOX"],
    "read": false,
    "createdAt": "2025-09-17T08:11:22Z"
  }
]
```

#### 2. 未读计数
GET `/api/notifications/unread-count?userId={userId}`

响应:
```json
{"unread": 3}
```

#### 3. 批量标记已读
POST `/api/notifications/mark-read`
```json
{
  "userId": 10,
  "recipientIds": [5551,5552]
}
```
响应:
```json
{"updated":2}
```

#### 4. 全部标记已读
POST `/api/notifications/mark-all-read`
```json
{"userId":10}
```
响应:
```json
{"updated":5}
```

### 二、通知创建（内部/测试用）

#### 5. 手工创建通知（非模板）
POST `/api/notifications/create`
```json
{
  "type": "LEAVE_SUBMITTED",
  "title": "手工标题",
  "content": "手工内容",
  "priority": "NORMAL",
  "businessRefType": "LEAVE_REQUEST",
  "businessRefId": "123",
  "dedupeKey": "manual:test:123",
  "templateCode": null,
  "extraJson": null,
  "recipients": [10,11]
}
```
响应:
```json
{"id": 201}
```

#### 6. 基于模板创建通知（直接渲染立即发送）
POST `/api/notifications/create-template`
```json
{
  "type": "LEAVE_APPROVED",
  "templateCode": "LEAVE_APPROVED_TO_STUDENT",
  "variables": {
    "leaveId":123,
    "studentName":"张三",
    "leaveTypeName":"病假",
    "startDate":"2025-09-17",
    "endDate":"2025-09-19",
    "days":2
  },
  "priority": "NORMAL",
  "businessRefType": "LEAVE_REQUEST",
  "businessRefId": "123",
  "dedupeKey": "leave:final:123:A",
  "recipients": [15]
}
```
响应:
```json
{"id": 305}
```

说明：`variables` 若缺少占位符，会保留 `{var}` 原样；幂等通过 `dedupeKey` 防重复。

### 三、模板管理（简化版：无版本，仅覆盖）

#### 7. 获取模板（按 code）
GET `/api/notification-templates/simple/{code}`

示例:
```
GET /api/notification-templates/simple/LEAVE_SUBMITTED_TO_APPROVER
```
响应(存在):
```json
{
  "code": "LEAVE_SUBMITTED_TO_APPROVER",
  "titleTemplate": "{studentName} 的请假申请待审批",
  "contentTemplate": "学生：{studentName}\n班级：{className}",
  "remark": "初版",
  "version": 1,
  "status": "ACTIVE"
}
```
不存在:
```json
{"code":"WHATEVER_NEW","exists":false}
```

#### 8. 覆盖/创建模板
POST `/api/notification-templates/simple/{code}`
```json
{
  "titleTemplate": "{studentName} 的请假申请待审批",
  "contentTemplate": "学生：{studentName}\n类型：{leaveTypeName}\n时间：{startDate}~{endDate}",
  "remark": "调整措辞"
}
```
响应:
```json
{"code":"LEAVE_SUBMITTED_TO_APPROVER","saved":true}
```

#### 9. 变量元数据
GET `/api/notification-templates/simple/variables`

响应(示例):
```json
{
  "groups": [
    {"group":"申请人信息","items":[{"name":"studentName","label":"学生姓名","type":"string","example":"张三","required":true}]},
    {"group":"请假信息","items":[{"name":"leaveTypeName","label":"请假类型","type":"string","example":"病假","required":true}]}
  ]
}
```

#### 10. 模板预览渲染
POST `/api/notification-templates/simple/preview`

模式 A：基于已存在模板 + leaveRequestId
```json
{
  "templateCode": "LEAVE_STEP_ADVANCED_TO_APPROVER",
  "leaveRequestId": 123,
  "overrideVariables": {"currentStepName":"学院书记审批"}
}
```

模式 B：自定义模板草稿 + 手写变量
```json
{
  "titleTemplate": "请假被拒绝",
  "contentTemplate": "{studentName} 的请假（{leaveTypeName}）被拒绝，原因：{rejectReason}",
  "variables": {
    "studentName":"测试学生",
    "leaveTypeName":"事假",
    "rejectReason":"资料不全"
  }
}
```

响应示例:
```json
{
  "renderedTitle": "请假被拒绝",
  "renderedContent": "测试学生 的请假（事假）被拒绝，原因：资料不全",
  "used": ["studentName","leaveTypeName","rejectReason"],
  "missing": [],
  "unused": [],
  "rawVariables": {
    "studentName":"测试学生",
    "leaveTypeName":"事假",
    "rejectReason":"资料不全"
  }
}
```

字段说明:
- used: 模板中出现且成功替换的变量
- missing: 模板引用但未提供值的变量（原样保留 `{var}`）
- unused: 提供了值但模板未使用的变量
- rawVariables: 实际参与渲染的变量快照

### 四、请假流程触发的模板 code 列表（仅说明）
| 事件 | 对应 templateCode | NotificationType |
|------|-------------------|------------------|
| 学生提交 → 首步审批人 | LEAVE_SUBMITTED_TO_APPROVER | LEAVE_SUBMITTED |
| 流程推进 → 下一审批人 | LEAVE_STEP_ADVANCED_TO_APPROVER | LEAVE_STEP_ADVANCED |
| 最终人工批准 → 学生 | LEAVE_APPROVED_TO_STUDENT | LEAVE_APPROVED |
| 无需审批自动批准 → 学生 | LEAVE_AUTO_APPROVED_TO_STUDENT | LEAVE_APPROVED |
| 最终拒绝 → 学生 | LEAVE_REJECTED_TO_STUDENT | LEAVE_REJECTED |

> 当前仅允许覆盖这些 code 的模板内容；如需新增自定义 code，需要后续引入“事件→模板映射”能力。

### 五、幂等策略
通知在创建时可携带 `dedupeKey`：
| 场景 | 规则样例 |
|------|----------|
| 提交 | `leave:submitted:{leaveId}` |
| 推进某步骤 | `leave:step:{leaveId}:{stepOrder}` |
| 最终批准 | `leave:final:{leaveId}:A` |
| 最终拒绝 | `leave:final:{leaveId}:R` |

后端会先查是否存在同 dedupeKey 的通知，存在则直接返回其 ID，不再重复发送。

### 六、错误与返回码（当前实现简单化）
| 状态 | 场景 |
|------|------|
| 200 | 正常返回 |
| 400 | 缺少必须字段 / 非法参数（如 templateCode 为空）|
| 404 | （预留）未来可在模板或 leaveRequest 不存在时抛出 |
| 500 | 其他未捕获异常 |

### 七、后续可扩展点（未实现，仅提示）
- 模板版本管理 / 发布审批流
- 事件→模板动态映射表
- 多渠道差异模板 (INBOX / EMAIL / SMS)
- 国际化（locale 维度）
- 模板测试发送（真正发给某个测试用户）
- 富文本或安全过滤（防止脚本注入）

---
如需再增加“模板列表接口”或“事件→模板映射设计”，可以继续提出。

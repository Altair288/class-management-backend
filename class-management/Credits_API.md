## 学分 (Credits) 模块 API 文档

版本: v1 (依据当前代码梳理)  
最后更新: {{DATE}}

> 说明：本文档覆盖当前后端已实现的“德智体美劳”学分配置、学生学分增减、规则应用、统计与审计日志接口。后续若有字段/接口调整需同步更新。

### 1. 概述

学分模块核心能力：
1. 主项目 (CreditItem) 五育分类配置（德/智/体/美/劳），支持启用、初始分、最大分、描述。  
2. 子项目 (CreditSubitem) 可选配置：权重 (0~1) + 初始/最大分，权重总和 <= 1。  
3. 学生学分记录 (StudentCredit) ：每个学生-项目维度当前分值。  
4. 应用规则：对所有学生批量 reset / clamp。  
5. 评价 (Evaluation) 汇总：生成 total 与等级 (excellent/good/warning/danger)。  
6. 学分变动审计日志 (credit_change_log) ：记录操作者、前后分值、delta、原因、批次。  
7. 仪表盘聚合、联合筛选、分页日志查询。  
8. 角色 / 权限控制：ADMIN / TEACHER 全局；CLASS_MONITOR 仅限本班学生；普通 STUDENT 只读自身（当前开放度以实现代码为准）。

### 2. 数据模型简述

| 名称 | 说明 | 关键字段 | 备注 |
|------|------|----------|------|
| CreditItem | 主项目（五育之一的配置） | id, category(德智体美劳), itemName, initialScore, maxScore, enabled | 每类目前前端只用 1 条主项目记录；category 作为逻辑分组。 |
| CreditSubitem | 子项目（细分指标） | id, itemId, subitemName, weight(0~1), initialScore, maxScore, enabled | 启用子项权重和 <= 1.0；关闭子项不计入权重。 |
| StudentCredit | 学生在某主项目当前得分 | studentId, creditItemId, score | score 上限受 item.maxScore 与操作逻辑约束。 |
| StudentEvaluation | 汇总/评级（内部表） | studentId, totalScore, status | status 可能值: excellent/good/warning/danger。 |
| CreditChangeLog | 学分变动审计日志 | operatorUserId, operatorUsername, operatorRoleCodes, studentId, creditItemId, oldScore, newScore, delta, actionType, reason, batchId, createdAt | actionType: DELTA/SET/RESET/CLAMP/INIT/ROLLBACK。 |

#### 2.1 分值与约束
* 分值范围逻辑控制（未在数据库强约束）：0 ≤ score ≤ maxScore；批量 clamp 时压缩到新上限。  
* initialScore / maxScore 限定在 0~100（代码内校验）。  
* 子项目 weight ∈ [0,1]，当前仅存储；聚合逻辑尚未对学生实际得分进行子项目加权（未来可扩展）。

### 3. 角色与权限

| 行为 | 权限控制 | 说明 |
|------|----------|------|
| 查询主/子项目列表 | 任意已认证 (当前实现未严格限制) | 可视需要进一步收紧。 |
| 创建/修改主项目 | @creditPermission.canApplyItemRule() | ADMIN / TEACHER / (userType=ADMIN|TEACHER 回退) |
| 创建/修改子项目 | (当前未加方法级限制) | 建议后续与主项目保持一致。 |
| 应用规则 apply (reset / clamp) | @creditPermission.canApplyItemRule() | 产生批量日志。 |
| 学生学分增减 / 设置 | @creditPermission.canEditStudent(studentId) | ADMIN/TEACHER=全局；CLASS_MONITOR=同班；其余拒绝。 |
| 查看学分变动日志 | hasAnyRole('ADMIN','TEACHER','CLASS_MONITOR') | 班长可查看所有日志（可扩展为仅本班）。 |
| 班长管理 (独立接口) | classMonitorPermission | 已单独实现（见另文档）。 |

> 注意：由于当前 `CustomUserDetailsService` 会从 user_role / userType 构造 ROLE_ 前缀权限，确保为管理员配置 ADMIN 角色或 userType=ADMIN。

### 4. 接口一览

| 分组 | 方法 | 路径 | 描述 | 鉴权(摘要) |
|------|------|------|------|-----------|
| Item | GET | /api/credits/items?category= | 列出主项目（可按类别） | 认证 |
| Item | POST | /api/credits/items | 创建主项目 | ADMIN/TEACHER |
| Item | POST | /api/credits/items/{id} | 更新主项目 | ADMIN/TEACHER |
| Item | POST | /api/credits/items/{itemId}/apply | 应用新规则(reset/clamp) | ADMIN/TEACHER |
| Subitem | GET | /api/credits/items/{itemId}/subitems | 主项目下子项目列表 | 认证 |
| Subitem | GET | /api/credits/subitems?itemId= | 兼容旧列表 | 认证 |
| Subitem | POST | /api/credits/items/{itemId}/subitems | 新增子项目 | 建议同主项目 |
| Subitem | GET | /api/credits/subitems/{id} | 获取子项目 | 认证 |
| Subitem | POST | /api/credits/subitems/{id} | 更新子项目 | 建议同主项目 |
| Subitem | DELETE | /api/credits/subitems/{id} | 删除子项目 | 建议同主项目 |
| Student | GET | /api/credits/students/{studentId}/totals | 学生五育汇总 | 认证 |
| Student | GET | /api/credits/students/{studentId}/items | 学生按项目列表 | 认证 |
| Student | POST | /api/credits/students/{studentId}/update-score | 增减分 | ADMIN/TEACHER/班长(本班) |
| Student | POST | /api/credits/students/{studentId}/set-score | 绝对设值 | 同上 |
| Evaluation | GET | /api/credits/students/{studentId}/evaluation | 获取/重算学生评价 | 认证 |
| Evaluation | POST | /api/credits/class/{classId}/evaluation/recompute | 重算班级所有学生 | ADMIN/TEACHER |
| Evaluation | POST | /api/credits/evaluation/recompute-all | 全量重算 | ADMIN/TEACHER |
| Class | GET | /api/credits/class/{classId}/students | 班级所有学生学分 (当前含 N+1) | 认证 |
| Union | GET | /api/credits/student-union-scores | 关键字/班级/状态过滤聚合 | 认证 |
| Dashboard | GET | /api/credits/dashboard/summary | 仪表盘汇总 | 认证 |
| Logs | GET | /api/credits/logs | 学分变动日志分页搜索 | ADMIN/TEACHER/班长 |

### 5. 详细接口说明

#### 5.1 主项目

GET `/api/credits/items?category=德`
响应: `CreditItemDTO[]`

`CreditItemDTO` 字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| category | String | "德" / "智" / "体" / "美" / "劳" |
| itemName | String | 项目名 |
| initialScore | Number | 初始分 (0~100) |
| maxScore | Number | 最大分 (0~100) |
| description | String | 描述 |
| enabled | Boolean | 是否启用 |

创建 / 更新：
POST `/api/credits/items`  
POST `/api/credits/items/{id}`  
请求体同上（更新时路径 id 为准）。

应用规则：
POST `/api/credits/items/{itemId}/apply`
请求体：`{"mode":"reset" | "clamp"}`（空则默认为 reset）
返回: `{ "affected": <受影响记录数>, "mode": "reset|clamp" }`

模式说明：
| mode | 行为 |
|------|------|
| reset | 所有学生该项目分值重置为 initialScore，再按 maxScore 截断 |
| clamp | 仅把超过新 maxScore 的分值压到 maxScore，其余不变 |

日志：批量操作生成多条 `RESET` 或 `CLAMP` 类型记录，带同一 batchId。

#### 5.2 子项目

GET `/api/credits/items/{itemId}/subitems`
响应: `CreditSubitemDTO[]`

`CreditSubitemDTO` 字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 子项目ID |
| itemId | Integer | 关联主项目ID |
| subitemName | String | 名称（同一主项目下唯一） |
| initialScore | Integer | 初始分 0~100 |
| maxScore | Integer | 最大分 0~100 |
| weight | Number | 权重 0~1（启用子项累加 ≤1） |
| enabled | Boolean | 是否启用 |

创建：POST `/api/credits/items/{itemId}/subitems`  
更新：POST `/api/credits/subitems/{id}`  
删除：DELETE `/api/credits/subitems/{id}`

#### 5.3 学生维度

GET `/api/credits/students/{studentId}/totals`
响应: `StudentCreditsDTO`

| 字段 | 类型 | 说明 |
|------|------|------|
| studentId | Integer | 学生ID |
| studentName | String | 姓名 |
| studentNo | String | 学号 |
| de/zhi/ti/mei/lao | Number | 各类别合计 |

GET `/api/credits/students/{studentId}/items`  -> 各项目当前分值（含 maxScore / enabled / description）。

增减分：
POST `/api/credits/students/{studentId}/update-score`
```json
{ "creditItemId": 12, "delta": 5.0, "reason": "期中表现" }
```

绝对设值：
POST `/api/credits/students/{studentId}/set-score`
```json
{ "creditItemId": 12, "value": 88.5, "reason": "导入修正" }
```

日志 actionType：
| 操作 | actionType | 说明 |
|------|------------|------|
| 增减分 | DELTA | old/new/delta 记录 |
| 设值 | SET | old -> new |
| 批量 reset | RESET | applyItemRule reset |
| 批量 clamp | CLAMP | applyItemRule clamp |
| 初始化 | INIT | 初次插入（若实现） |
| 回滚 | ROLLBACK | 未来支持回滚 |

#### 5.4 班级与聚合

GET `/api/credits/class/{classId}/students`  
响应: `StudentCreditsViewDTO[]` （当前实现有潜在 N+1 重算开销；后续可批量聚合优化）。

GET `/api/credits/student-union-scores?keyword=&classId=&status=`  
说明：一次批量聚合 + 状态过滤（status 可为 excellent/good/warning/danger）。

仪表盘：
GET `/api/credits/dashboard/summary`  
响应示例：
```json
{
  "totalStudents": 1200,
  "totalClasses": 36,
  "countExcellent": 180,
  "countGood": 560,
  "countWarning": 300,
  "countDanger": 160,
  "avgDe": 78.5,
  "avgZhi": 80.2,
  "avgTi": 75.1,
  "avgMei": 70.6,
  "avgLao": 72.0,
  "avgTotal": 376.4
}
```

#### 5.5 评价 / 重算

GET `/api/credits/students/{studentId}/evaluation` -> `{ studentId, total, status }` （即时重算 + 返回）。

POST `/api/credits/class/{classId}/evaluation/recompute` -> `{ "recomputed": <数量> }`  
POST `/api/credits/evaluation/recompute-all` -> `{ "recomputed": <数量> }`

#### 5.6 审计日志分页

GET `/api/credits/logs?studentId=&itemId=&operator=&actionType=&batchId=&from=&to=&page=0&size=20`

参数：
| 参数 | 说明 | 格式 |
|------|------|------|
| studentId | 按学生过滤 | 数字 |
| itemId | 按主项目过滤 | 数字 |
| operator | 操作用户名模糊（当前为精确匹配） | 字符串 |
| actionType | DELTA/SET/RESET/... | 大写常量 |
| batchId | 批量操作批次 | UUID/字符串 |
| from/to | 时间范围 | ISO8601 (e.g. 2025-01-01T00:00:00Z) |
| page/size | 分页 | size ≤ 200 |

响应：`Page<CreditChangeLog>` Spring 默认分页结构：
```json
{
  "content": [ { "id":1, "studentId":11, "oldScore":80.00, "newScore":85.00, "delta":5.00, "actionType":"DELTA", "reason":"加分", "operatorUsername":"admin", "operatorRoleCodes":"ADMIN", "createdAt":"2025-10-01T08:00:00Z" } ],
  "number":0,
  "size":20,
  "totalElements":135,
  "totalPages":7
}
```

`CreditChangeLog` 关键字段：
| 字段 | 说明 |
|------|------|
| operatorUserId / operatorUsername | 操作人快照 |
| operatorRoleCodes | 操作人角色 CSV（数据库角色 → authorities → userType 回退） |
| studentId / studentNo / studentName | 学生快照 |
| creditItemId / category / itemName | 项目快照 |
| oldScore / newScore / delta | 分值变化（BigDecimal 2 位） |
| actionType | 操作类型 |
| reason | 业务原因（可为空） |
| batchId | 批量操作同批号 |
| rollbackFlag | 预留回滚标识（当前 false） |
| createdAt | 记录时间 |

### 6. 错误码与校验

当前实现多以 `IllegalArgumentException` 抛出 -> 400；其他异常 -> 500（包装为统一结构可能含 `code`="SERVER_ERROR"）。常见 message：
| 消息 | 场景 |
|------|------|
| 学生不存在 / 项目不存在 | 传入 ID 无效 |
| 子项目不存在 | 查询/更新/删除子项目 | 
| 该主项目下已存在同名子项目 | 创建/改名冲突 |
| 子项目初始分不能大于最大分 | 校验 |
| 启用子项权重之和不能超过 1.0 | 权重校验 |
| 不支持的模式: xxx | applyItemRule 非 reset/clamp |
| 状态必须为 excellent/good/warning/danger | union 查询过滤值不合法 |
| 时间格式需为 ISO-8601 | /logs 时间解析失败 |

### 7. 审计 & 安全补充
* 所有增减 / 设置 / 批量规则应用产生不可篡改审计日志。  
* operatorRoleCodes 三层回退确保后续补角色仍可表达真实权限。  
* 建议后续：
  - 限制班长仅能查看本班日志（增加 @creditPermission.canViewLog(studentId)）。
  - 引入统一 ApiError 结构：`{ code, message, details }`。
  - 日志增加 requestId / ip / userAgent。

### 8. 已知待优化
| 项目 | 问题 | 建议 |
|------|------|------|
| 班级学分列表 `/class/{classId}/students` | N+1：循环内多次查询 + 重算 | 通过一次聚合 + 状态批量查询 (已有 union-scores 为基础) 替换 |
| 子项目权重 | 仅保存 & 校验，不参与学生实时得分计算 | 设计实际加权计算或去除未使用字段 |
| 权限粒度 | 日志/列表接口较宽 | 引入细粒度 permission bean |
| 回滚功能 | rollbackFlag 未使用 | 增加补偿/撤销接口 |

### 9. 示例 cURL

创建主项目：
```bash
curl -X POST http://localhost:8080/api/credits/items \
  -H 'Content-Type: application/json' \
  -d '{"category":"德","itemName":"德育学分","initialScore":0,"maxScore":100,"description":"德育","enabled":true}'
```

增减学分：
```bash
curl -X POST http://localhost:8080/api/credits/students/101/update-score \
  -H 'Content-Type: application/json' \
  -d '{"creditItemId":12,"delta":5,"reason":"期中表现"}'
```

应用规则（重置）：
```bash
curl -X POST http://localhost:8080/api/credits/items/12/apply \
  -H 'Content-Type: application/json' \
  -d '{"mode":"reset"}'
```

查询日志：
```bash
curl 'http://localhost:8080/api/credits/logs?actionType=DELTA&page=0&size=10'
```

### 10. 未来扩展建议
1. 引入子项目得分计算（按权重汇总到主项目）。  
2. 加入版本化的项目配置（历史追溯）。  
3. Redis 缓存常用聚合 (dashboard, union-scores)。  
4. 规则应用 dry-run 模式（预估影响人数与分值范围）。  
5. 统一导入/导出（Excel 批量学分调整 + 预检）。  
6. WebSocket/通知：学分变动触发消息中心推送。  

---
如需补充“班长操作与学分权限说明”或“批量导入设计”请再提出。

基路径：/api/workflows

流程模板
GET /api/workflows → 列出所有流程
POST /api/workflows → 创建流程
PUT /api/workflows/{id} → 更新流程
DELETE /api/workflows/{id} → 删除流程
流程步骤
GET /api/workflows/{workflowId}/steps → 列出启用步骤（按序）
POST /api/workflows/{workflowId}/steps → 新增步骤
PUT /api/workflows/steps/{stepId} → 更新步骤
DELETE /api/workflows/steps/{stepId} → 删除步骤
全局类型绑定（每个类型唯一绑定一个流程）
PUT /api/workflows/bind/type/{leaveTypeId}/workflow/{workflowId} → 绑定/更新
DELETE /api/workflows/bind/type/{leaveTypeId} → 解绑
（已移除）班级覆盖绑定：统一仅按请假类型绑定流程
审批人绑定（把角色映射到具体老师；作用域优先级：班级 > 年级 > 全局）
POST /api/workflows/role-assignment → 创建
PUT /api/workflows/role-assignment/{id} → 更新
DELETE /api/workflows/role-assignment/{id} → 删除
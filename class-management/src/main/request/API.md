# 新增 RoleController (/api/roles)：

GET /api/roles 列出全部（按 category,level,sortOrder 排序）
GET /api/roles/system 仅系统角色
GET /api/roles/approval 仅审批角色
POST /api/roles 创建角色（需 code, displayName, category）
PUT /api/roles/{id} 更新显示名/描述/启用/层级/排序
PATCH /api/roles/{id}/hierarchy 调整 level / sortOrder
PATCH /api/roles/{id}/enabled 启停用
DELETE /api/roles/{id} 删除角色


已实现后端增强，满足三点需求：

前端可获取角色信息：

GET /api/teachers/management/assignable-roles 返回所有可分配审批角色（含层级字段 level、sortOrder）
GET /api/teachers/management/role-hierarchy 返回系统+审批全量层级
为教师分配/修改角色：

仍用 PUT /api/teachers/management/{id}（全量覆盖）
新增校验：仅允许 category=APPROVAL 的角色；重复作用域组合会报错；历史异常数据（非审批类别）会阻断更新
返回层级对应关系：

role-hierarchy 接口按 category, level, sortOrder 排序
assignable-roles 仅审批角色，同样有 level/sortOrder，前端可直接按层级构建 UI

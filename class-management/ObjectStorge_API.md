## 对象存储 API 文档

### 1. 概述

本模块提供统一的对象存储接入与文件上传 / 下载预签名 URL 生成逻辑，当前实现基于 MinIO（兼容 S3）。

支持的核心能力：

1. 管理对象存储连接（连接信息加密保存，支持探活测试）。
2. 根据业务用途 bucketPurpose 创建上传预签名 URL（前端直传，后端只做签名与记录）。
3. 上传完成后通过确认接口落库文件最终信息、触发业务挂接（目前仅 LEAVE_REQUEST）。
4. 列出指定业务实体的已完成文件。
5. 获取单个文件的下载预签名 URL。

### 2. 术语与数据模型

- bucketPurpose: 逻辑用途标识，对应表 `file_storage_config.bucket_purpose`，驱动选择 bucket、路径、校验规则。
- FileObject: 文件对象元数据记录，状态机：UPLOADING -> COMPLETED（未来可扩展 FAILED / DELETED）。
- BusinessRefType / BusinessRefId: 业务实体关联（当前示例：LEAVE_REQUEST）。
- 预签名 URL: 由后端使用 Minio Client 生成的临时 PUT / GET URL，有限期 `expireSeconds`（默认 600s）。

#### 2.1 FileObject 主要字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| bucketName | String | 目标存储桶 |
| objectKey | String | 对象路径（含日期分层 + 随机 UUID）|
| originalFilename | String | 原始文件名 |
| ext | String | 扩展名（小写）|
| mimeType | String | 确认时可写入的 MIME |
| sizeBytes | Long | 确认时可写入的大小 |
| status | String | UPLOADING / COMPLETED |
| uploaderUserId | Integer | 上传者（登录用户，匿名可能为 null）|
| businessRefType | String | 业务类型 |
| businessRefId | Long | 业务实体 ID |
| createdAt | Date | 记录创建时间 |
| completedAt | Date | 完成时间（确认后）|
| downloadUrl | String | （仅下载信息接口返回）|

#### 2.2 CreateUploadResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| fileObjectId | Long | 生成的文件对象 ID |
| bucketName | String | 桶名 |
| objectKey | String | 对象键 |
| presignUrl | String | 直接 PUT 上传的预签名 URL |
| expireSeconds | Integer | URL 过期秒数 |

#### 2.3 FileObjectDTO （除列表 / 下载接口通用返回）

同 2.1，另在下载接口附带 downloadUrl。

#### 2.4 ConnectionDTO

用于连接管理：id, name, provider, endpointUrl, secureFlag, pathStyleAccess, defaultPresignExpireSeconds, active, lastTestStatus, lastTestError。

### 3. 状态机

初始：createUpload -> 记录生成 (status=UPLOADING)

成功上传 OSS 后：前端调用 confirmUpload -> 校验大小/MIME -> status=COMPLETED -> 钩子：若 businessRefType=LEAVE_REQUEST 追加 leave_attachment 并自增 attachment_count。

后续可扩展：FAILED（上传失败）、DELETED（逻辑删除）。当前代码未提供失败/删除接口。

### 4. 接口一览

| 分组 | 方法 | 路径 | 描述 | 请求体 | 响应体 |
|------|------|------|------|--------|--------|
| 连接 | GET | /api/object-storage/connections | 列出连接 | - | ConnectionDTO[] |
| 连接 | POST | /api/object-storage/connections | 新增/更新连接 | ObjectStorageConnection JSON | ConnectionDTO |
| 连接 | POST | /api/object-storage/connections/{id}/test | 测试连接 | - | ConnectionDTO |
| 配置 | GET | /api/object-storage/storage-configs | 列出存储配置 | - | FileStorageConfigDTO[] |
| 配置 | GET | /api/object-storage/storage-configs/{id} | 查看配置详情 | - | FileStorageConfigDTO |
| 配置 | POST | /api/object-storage/storage-configs | 新增/更新存储配置 | SaveFileStorageConfigRequest | FileStorageConfigDTO |
| 配置 | POST | /api/object-storage/storage-configs/{id}/enable?enabled=bool | 启用/停用配置 | - | FileStorageConfigDTO |
| 配置 | DELETE | /api/object-storage/storage-configs/{id} | 删除存储配置（无引用时） | - | 204(No Content) |
| 上传 | POST | /api/object-storage/upload/create | 创建上传（取 PUT 预签名）| CreateUploadRequest | CreateUploadResponse |
| 上传 | POST | /api/object-storage/upload/confirm | 确认上传完成 | ConfirmUploadRequest | FileObjectDTO |
| 文件 | GET | /api/object-storage/business/{type}/{id}/files | 列出业务已完成文件 | 路径参数 | FileObjectDTO[] |
| 文件 | GET | /api/object-storage/files/{id}/download-info | 获取下载预签名 | 路径参数 | FileObjectDTO (含 downloadUrl) |

### 5. 详细接口说明

#### 5.1 列出连接

GET /api/object-storage/connections

响应 200: ConnectionDTO[]

#### 5.2 保存/更新连接

POST /api/object-storage/connections

请求示例:

```json
{
  "id": 1,                 // 可选，存在则更新
  "name": "minio-main",
  "provider": "MINIO",
  "endpointUrl": "http://minio:9000",
  "accessKeyEncrypted": "minioadmin",   // 纯文本传入会自动 Base64 包装成 ENC:
  "secretKeyEncrypted": "minioadmin",
  "secureFlag": true,
  "pathStyleAccess": true,
  "defaultPresignExpireSeconds": 600,
  "active": true
}
```

响应 200: ConnectionDTO（凭证不回显明文）

#### 5.3 测试连接

#### 5.3.1 存储配置：列表

GET /api/object-storage/storage-configs

响应 200 示例:

```json
[
  {
    "id": 1,
    "bucketName": "leave-bucket",
    "bucketPurpose": "LEAVE_IMAGE",
    "connectionId": 2,
    "basePath": "leave",
    "maxFileSize": 5242880,
    "allowedExtensions": ["jpg","jpeg","png"],
    "allowedMimeTypes": ["image/jpeg","image/png"],
    "retentionDays": 365,
    "autoCleanup": false,
    "enabled": true,
    "createdAt": "2025-09-29T10:00:00",
    "updatedAt": "2025-09-29T10:00:00"
  }
]
```

#### 5.3.2 存储配置：查看详情

GET /api/object-storage/storage-configs/{id}

响应: FileStorageConfigDTO

#### 5.3.3 存储配置：新增/更新

POST /api/object-storage/storage-configs

请求体 SaveFileStorageConfigRequest:

```json
{
  "id": 1,                       // 可选，更新时传
  "bucketName": "leave-bucket",
  "bucketPurpose": "LEAVE_IMAGE",
  "connectionId": 2,
  "basePath": "leave",
  "maxFileSize": 5242880,
  "allowedExtensions": ["jpg","jpeg","png"],
  "allowedMimeTypes": ["image/jpeg","image/png"],
  "retentionDays": 365,
  "autoCleanup": false,
  "enabled": true
}
```
返回: FileStorageConfigDTO

字段说明:

- allowedExtensions / allowedMimeTypes 为空或缺省 -> 后端存储为 [] 表示不限制。
- basePath 可为空，最终 objectKey = [basePath/]yyyy/MM/dd/uuid.ext （示例路径）

#### 5.3.4 存储配置：启用 / 停用
#### 5.3.5 存储配置：删除

DELETE /api/object-storage/storage-configs/{id}

行为：
- 若存在关联文件（file_object.storage_config_id），返回 400 BAD_REQUEST，message 含计数。
- 成功返回 204 (无内容)。

注意：当前未实现级联删除文件，需业务先迁移或清理文件后再删配置。

### 5.x 自动清理说明

调度任务（默认需要在 Spring 启动类上启用 `@EnableScheduling` 若尚未添加）每日 02:30 执行：
1. 遍历 autoCleanup=true 的配置。
2. 计算 retentionDays 阈值（创建时间早于 N 天）。
3. 将满足条件且 status=COMPLETED 的 `file_object` 标记为 DELETED，写入 deletedAt。
4. 目前不物理删除对象存储文件；后续可扩展真正删除 MinIO/S3 对象。

风险 & 建议：
- 如果需要物理清理，需在任务中调用 MinioClient.removeObject，并加入失败重试与日志。
- 建议增加一个开关配置以控制是否开启逻辑删除转物理删除。


POST /api/object-storage/storage-configs/{id}/enable?enabled=true|false

返回: FileStorageConfigDTO（enabled 变更后）

校验与注意:

- 更新或启用前会验证关联 connection 存在。
- bucketName 唯一由数据库 UNIQUE 约束；违反时返回 500/错误消息（后续可改成友好错误码）。


POST /api/object-storage/connections/{id}/test

响应: ConnectionDTO（更新 lastTestStatus=SUCCESS/FAIL, lastTestError 描述）

#### 5.4 创建上传 (获取 PUT 预签名)

POST /api/object-storage/upload/create

请求体 CreateUploadRequest:

```json
{
  "bucketPurpose": "LEAVE_IMAGE",
  "originalFilename": "photo.jpg",
  "businessRefType": "LEAVE_REQUEST",
  "businessRefId": 123,
  "expectedSize": 102400   // 可选
}
```
处理逻辑：
1. 根据 bucketPurpose 查 `file_storage_config` 获取配置与连接。
2. 校验扩展名是否在 allowedExtensions。
3. 生成 objectKey: [basePath/]yyyy/MM/dd/uuid[.ext]
4. 写入 file_object status=UPLOADING。
5. 生成 PUT 预签名 URL。
响应示例:

```json
{
  "fileObjectId": 456,
  "bucketName": "leave-bucket",
  "objectKey": "leave/2025/09/29/4d2e0d1e4f8d4aa6b2f4e0d9c2a1b3f0.jpg",
  "presignUrl": "http://minio:9000/leave-bucket/...",
  "expireSeconds": 600
}
```
前端后续使用该 presignUrl 执行 HTTP PUT 上传：
Headers: Content-Type: <与文件 MIME 一致，可选>
Body: 二进制文件内容

#### 5.5 确认上传完成

POST /api/object-storage/upload/confirm

请求体 ConfirmUploadRequest:

```json
{
  "fileObjectId": 456,
  "sizeBytes": 102400,
  "mimeType": "image/jpeg"
}
```
处理逻辑：
1. 必须当前状态为 UPLOADING；否则报错。
2. 校验 mimeType 是否在 allowedMimeTypes（若配置）。
3. 校验 sizeBytes <= maxFileSize（若配置）。
4. 更新 status=COMPLETED, completedAt=now。
5. 若 businessRefType=LEAVE_REQUEST：插入 leave_attachment + leave_request.attachment_count 自增。
响应: FileObjectDTO（不含 downloadUrl）。

#### 5.6 列出业务文件

GET /api/object-storage/business/{type}/{id}/files

只返回 status=COMPLETED 的文件。

响应: FileObjectDTO[]。

#### 5.7 获取下载预签名

GET /api/object-storage/files/{id}/download-info

生成 GET 预签名（默认过期 defaultPresignExpireSeconds）。

响应: FileObjectDTO （downloadUrl 字段有值）。

### 6. 错误处理

当前代码以 RuntimeException 抛出，统一由全局异常处理（若已配置）转为 4xx/5xx。常见 message：

- "未配置对应用途的存储桶:<bucketPurpose>"
- "关联连接不存在"
- "存储配置已禁用"
- "文件扩展不允许: <ext>"
- "文件记录不存在"
- "当前状态不允许确认:<status>"
- "存储配置不存在"
- "MIME 类型不允许: <mime>"
- "文件大小超过允许限制:<max>"
- "文件不存在" / "连接不存在" / "生成上传预签名失败:..." / "生成下载预签名失败:..."

建议：前端可按 message 直接提示；后续可引入统一错误码 (ApiError) 结构。

### 7. 安全与权限

- 当前 resolveCurrentUserId() 基于 Spring Security Authentication name -> UserRepository 查找；匿名允许为 null。
- 连接保存接口未显式限制，需要在控制层或安全配置增加鉴权（如仅管理员可用）。
- 预签名 URL 过期后将失效；请在过期前完成上传 / 下载。

### 8. 前端典型流程示意

1. createUpload -> 返回 fileObjectId + presignUrl
2. 浏览器 PUT 上传文件到 presignUrl
3. 上传成功后调用 confirmUpload(fileObjectId, sizeBytes, mimeType)
4. 列表刷新：GET business/{type}/{id}/files 得到最新 COMPLETED 文件
5. 需要下载：GET files/{id}/download-info -> 跳转/下载 downloadUrl

### 9. 后续可扩展建议 (非当前实现)

- DELETE /files/{id} 逻辑删除 (status=DELETED, 设置 deletedAt )
- 失败回滚接口 /upload/cancel -> status=FAILED
- 分片/断点续传：引入 multipart init/complete 接口
- 统一错误码与国际化
- 服务器端病毒扫描/内容审核钩子

### 10. 示例：cURL

创建上传:

```bash
curl -X POST http://localhost:8080/api/object-storage/upload/create \
  -H 'Content-Type: application/json' \
  -d '{"bucketPurpose":"LEAVE_IMAGE","originalFilename":"photo.jpg","businessRefType":"LEAVE_REQUEST","businessRefId":123}'
```

确认上传:

```bash
curl -X POST http://localhost:8080/api/object-storage/upload/confirm \
  -H 'Content-Type: application/json' \
  -d '{"fileObjectId":456,"sizeBytes":102400,"mimeType":"image/jpeg"}'
```

获取下载预签名:

```bash
curl http://localhost:8080/api/object-storage/files/456/download-info
```

---
更新说明：本文件由代码自动梳理补全，保持与后端实现一致。如接口有调整请同步修改。

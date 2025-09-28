创建上传 POST /api/object-storage/upload/create Body: { "bucketPurpose": "LEAVE_IMAGE", "originalFilename": "photo.jpg", "businessRefType": "LEAVE_REQUEST", "businessRefId": 123 } 返回包含 presign PUT URL。

前端使用返回的 presignUrl 直接 PUT 文件（Content-Type 可设置为 MIME）。

调用确认 POST /api/object-storage/upload/confirm Body: { "fileObjectId": 456, "sizeBytes": 102400, "mimeType": "image/jpeg" } 成功后：

file_object 状态变为 COMPLETED
写入 leave_attachment
对应 leave_request.attachment_count +1
列出业务文件 GET /api/object-storage/business/LEAVE_REQUEST/{leaveId}/files

获取下载预签名 GET /api/object-storage/files/{id}/download-info
-- Flyway V1 Baseline: All core tables (extracted from original schema.sql)
-- 仅包含 DDL，不含种子数据；后续数据在 V2__seed_data.sql 中做幂等插入

-- NOTE: 如果生产库已存在部分表，使用 baseline-on-migrate=true 将不会重复创建

-- ================== CORE TABLES ==================

CREATE TABLE IF NOT EXISTS `permission` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称(唯一代码)',
  `display_name` VARCHAR(100) NULL COMMENT '显示名称(可选)',
  `description` TEXT COMMENT '权限描述',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_name` (`permission_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` VARCHAR(50) NOT NULL COMMENT '唯一代码，例如 STUDENT / HOMEROOM',
  `display_name` VARCHAR(50) NOT NULL COMMENT '显示名称，例如 学生 / 班主任',
  `category` ENUM('SYSTEM','APPROVAL') NOT NULL COMMENT '角色类别',
  `parent_id` INT DEFAULT NULL COMMENT '父角色(用于层级/继承)',
  `level` INT NOT NULL DEFAULT 1 COMMENT '层级：根=1 逐级递增',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '同层排序',
  `description` TEXT NULL COMMENT '描述',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`),
  KEY `idx_role_category` (`category`),
  KEY `idx_role_parent` (`parent_id`),
  CONSTRAINT `fk_role_parent` FOREIGN KEY (`parent_id`) REFERENCES `role`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `teacher` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `name` varchar(50) NOT NULL COMMENT '教师姓名',
  `teacher_no` varchar(20) NOT NULL COMMENT '工号',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `teacher_no` (`teacher_no`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `department` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '系部ID',
  `name` varchar(50) NOT NULL COMMENT '系部名称',
  `code` varchar(20) NOT NULL COMMENT '系部代码',
  `description` text NULL COMMENT '描述',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_code` (`code`),
  UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `class` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name` varchar(50) NOT NULL COMMENT '班级名称',
  `teacher_id` int DEFAULT NULL COMMENT '班主任ID',
  `department_id` int DEFAULT NULL COMMENT '所属系部ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `grade` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '年级',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `teacher_id` (`teacher_id`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `class_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_class_department` FOREIGN KEY (`department_id`) REFERENCES `department`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `class_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `teacher_id` int NOT NULL COMMENT '发布教师ID',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `class_notice_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `student` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `name` varchar(50) NOT NULL COMMENT '学生姓名',
  `student_no` varchar(20) NOT NULL COMMENT '学号',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `class_id` int NOT NULL COMMENT '班级ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `student_no` (`student_no`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `student_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `teacher_subject` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `teacher_id` int NOT NULL COMMENT '教师ID',
  `subject` varchar(50) NOT NULL COMMENT '科目名称',
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `teacher_subject_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `grade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `teacher_id` int DEFAULT NULL COMMENT '录入教师ID',
  `subject` varchar(50) NOT NULL COMMENT '科目',
  `score` decimal(5,2) NOT NULL COMMENT '成绩',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `grade_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `grade_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `credit_item` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置项ID',
  `category` enum('德','智','体','美','劳') NOT NULL COMMENT '类别',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `initial_score` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '初始分值',
  `max_score` decimal(5,2) NOT NULL DEFAULT 100 COMMENT '最大分值',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用状态: 1启用 0停用',
  `description` text NULL COMMENT '描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category`, `item_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `student_credit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `credit_item_id` int NOT NULL COMMENT '配置项ID',
  `score` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '当前得分',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_item` (`student_id`, `credit_item_id`),
  KEY `idx_credit_item` (`credit_item_id`),
  CONSTRAINT `fk_sc_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sc_item` FOREIGN KEY (`credit_item_id`) REFERENCES `credit_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `credit_subitem` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `item_id` INT NOT NULL COMMENT '关联 credit_item 主项目 ID',
  `subitem_name` VARCHAR(100) NOT NULL COMMENT '子项目名称',
  `initial_score` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '子项目初始分值',
  `max_score` DECIMAL(5,2) NOT NULL DEFAULT 100 COMMENT '子项目最高得分',
  `weight` DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '在父项目中的权重比例（0–1）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_subitem` (`item_id`, `subitem_name`),
  CONSTRAINT `fk_cs_item` FOREIGN KEY (`item_id`) REFERENCES `credit_item`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `student_evaluation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `student_id` INT NOT NULL COMMENT '学生ID',
  `total_score` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT '总分',
  `status` ENUM('excellent','good','warning','danger') NOT NULL COMMENT '评判等级',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_eval_student` (`student_id`),
  CONSTRAINT `fk_eval_student` FOREIGN KEY (`student_id`) REFERENCES `student`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `parent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '家长ID',
  `name` varchar(50) NOT NULL COMMENT '家长姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `student_id` int NOT NULL COMMENT '关联的学生ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `parent_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `identity_no` varchar(20) DEFAULT NULL COMMENT '学号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_type` varchar(50) NOT NULL COMMENT '用户类型',
  `related_id` int DEFAULT NULL COMMENT '关联的身份ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `identity_no` (`identity_no`),
  KEY `related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `leave_type_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `type_code` varchar(20) NOT NULL COMMENT '类型代码',
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `max_days_per_request` int NOT NULL DEFAULT 30 COMMENT '单次请假最大天数',
  `annual_allowance` int NOT NULL DEFAULT 15 COMMENT '年度额度',
  `requires_approval` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否需要审批',
  `requires_medical_proof` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要医疗证明',
  `advance_days_required` int NOT NULL DEFAULT 1 COMMENT '需要提前申请天数',
  `color` varchar(7) NOT NULL DEFAULT '#1976d2' COMMENT '显示颜色',
  `description` text COMMENT '类型描述',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `student_leave_balance` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `leave_type_id` int NOT NULL COMMENT '请假类型ID',
  `total_allowance` int NOT NULL COMMENT '总额度',
  `used_days` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '已使用天数',
  `remaining_days` decimal(5,2) NOT NULL COMMENT '剩余天数',
  `year` int NOT NULL COMMENT '年度',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `student_type_year` (`student_id`, `leave_type_id`, `year`),
  KEY `leave_type_id` (`leave_type_id`),
  CONSTRAINT `student_leave_balance_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `student_leave_balance_ibfk_2` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type_config` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 对象存储连接需先于 file_storage_config 创建，便于内联外键
CREATE TABLE IF NOT EXISTS `object_storage_connection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '连接ID',
  `name` VARCHAR(50) NOT NULL COMMENT '名称/别名',
  `provider` VARCHAR(20) NOT NULL DEFAULT 'MINIO' COMMENT '供应商',
  `endpoint_url` VARCHAR(255) NOT NULL COMMENT '终端地址 http(s)://host:port',
  `access_key_encrypted` VARCHAR(255) NOT NULL COMMENT '加密AccessKey',
  `secret_key_encrypted` VARCHAR(255) NOT NULL COMMENT '加密SecretKey',
  `secure_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否HTTPS',
  `path_style_access` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Path-Style访问',
  `default_presign_expire_seconds` INT NOT NULL DEFAULT 600 COMMENT '预签名默认有效期秒',
  `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `last_test_status` ENUM('SUCCESS','FAIL','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN' COMMENT '最近测试状态',
  `last_test_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最近测试时间',
  `last_test_error` VARCHAR(300) DEFAULT NULL COMMENT '最近错误信息截断',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `file_storage_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `bucket_name` varchar(100) NOT NULL COMMENT '存储桶名称',
  `bucket_purpose` varchar(100) NOT NULL COMMENT '存储桶用途',
  `connection_id` BIGINT NOT NULL COMMENT '对象存储连接ID',
  `base_path` VARCHAR(100) DEFAULT NULL COMMENT '对象Key逻辑前缀(可空)',
  `max_file_size` bigint NOT NULL DEFAULT 5242880 COMMENT '最大文件大小',
  `allowed_extensions` json NOT NULL COMMENT '允许的扩展名',
  `allowed_mime_types` json NOT NULL COMMENT '允许的MIME类型',
  `retention_days` int NOT NULL DEFAULT 365 COMMENT '保留天数',
  `auto_cleanup` tinyint(1) NOT NULL DEFAULT 0 COMMENT '自动清理',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`),
  KEY `idx_connection` (`connection_id`),
  CONSTRAINT `fk_fsc_connection` FOREIGN KEY (`connection_id`) REFERENCES `object_storage_connection`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `leave_request` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `student_id` int NOT NULL COMMENT '请假学生ID',
  `leave_type_id` int NOT NULL COMMENT '请假类型ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` DECIMAL(5,2) NOT NULL COMMENT '天数',
  `reason` text NOT NULL COMMENT '原因',
  `emergency_contact` varchar(50) NOT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) NOT NULL COMMENT '紧急电话',
  `handover_notes` text COMMENT '交接说明',
  `attachment_count` int NOT NULL DEFAULT 0 COMMENT '附件数量',
  `status` enum('草稿','待审批','已批准','已拒绝','已撤销') DEFAULT '待审批' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `leave_type_id` (`leave_type_id`),
  KEY `status` (`status`),
  KEY `start_date` (`start_date`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_request_ibfk_3` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type_config` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 通用文件元数据表（精简版，使用 storage_config_id 外键方式）
CREATE TABLE IF NOT EXISTS `file_object` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `storage_config_id` INT NOT NULL COMMENT '关联 file_storage_config.id',
  `bucket_name` VARCHAR(100) NOT NULL COMMENT '冗余存储桶名称（方便查询与快速过滤）',
  `object_key` VARCHAR(500) NOT NULL COMMENT '对象完整Key',
  `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `ext` VARCHAR(20) NOT NULL COMMENT '扩展名(小写)',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  `size_bytes` BIGINT DEFAULT NULL COMMENT '大小字节',
  `status` ENUM('UPLOADING','COMPLETED','FAILED','DELETED') NOT NULL DEFAULT 'UPLOADING' COMMENT '状态',
  `uploader_user_id` INT NOT NULL COMMENT '上传者用户ID',
  `business_ref_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `business_ref_id` BIGINT NOT NULL COMMENT '业务ID',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_storage_config` (`storage_config_id`),
  UNIQUE KEY `uk_storage_object` (`storage_config_id`,`object_key`),
  KEY `idx_bucket_key` (`bucket_name`,`object_key`),
  KEY `idx_status` (`status`),
  KEY `idx_biz_ref` (`business_ref_type`,`business_ref_id`),
  KEY `idx_uploader` (`uploader_user_id`),
  CONSTRAINT `fk_file_object_storage_config` FOREIGN KEY (`storage_config_id`) REFERENCES `file_storage_config`(`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_file_object_uploader` FOREIGN KEY (`uploader_user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `leave_attachment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `leave_request_id` int NOT NULL COMMENT '请假申请ID',
  `file_object_id` BIGINT NOT NULL COMMENT '关联通用文件 file_object.id',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `created_by` int NOT NULL COMMENT '上传学生ID',
  PRIMARY KEY (`id`),
  KEY `leave_request_id` (`leave_request_id`),
  KEY `file_object_id` (`file_object_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `fk_leave_attachment_leave` FOREIGN KEY (`leave_request_id`) REFERENCES `leave_request` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_leave_attachment_file_object` FOREIGN KEY (`file_object_id`) REFERENCES `file_object`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_leave_attachment_student` FOREIGN KEY (`created_by`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `approval_workflow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '流程ID',
  `workflow_name` varchar(100) NOT NULL COMMENT '流程名称',
  `workflow_code` varchar(50) NOT NULL COMMENT '流程代码',
  `description` text COMMENT '描述',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `workflow_code` (`workflow_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `approval_step` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
  `workflow_id` INT NOT NULL COMMENT '流程ID',
  `step_order` INT NOT NULL COMMENT '顺序',
  `step_name` VARCHAR(50) NOT NULL COMMENT '步骤名称',
  `approver_role_id` INT NOT NULL COMMENT '角色ID',
  `auto_approve` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动通过',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用',
  PRIMARY KEY (`id`),
  KEY `workflow_id` (`workflow_id`),
  KEY `approver_role_id` (`approver_role_id`),
  UNIQUE KEY `uk_workflow_step` (`workflow_id`, `step_order`),
  CONSTRAINT `fk_as_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `approval_workflow` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_as_role` FOREIGN KEY (`approver_role_id`) REFERENCES `role`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `leave_approval` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
  `leave_id` INT NOT NULL COMMENT '请假申请ID',
  `teacher_id` INT NOT NULL COMMENT '审批教师ID',
  `workflow_id` INT DEFAULT NULL COMMENT '流程ID',
  `step_order` INT NOT NULL DEFAULT 1 COMMENT '步骤序号',
  `step_name` VARCHAR(50) DEFAULT NULL COMMENT '步骤名称',
  `approver_role_id` INT DEFAULT NULL COMMENT '审批角色ID',
  `status` ENUM('待审批','已批准','已拒绝') NOT NULL DEFAULT '待审批' COMMENT '审批结果',
  `reviewed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '审批时间',
  `comment` TEXT COMMENT '意见',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `leave_id` (`leave_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `idx_workflow` (`workflow_id`),
  KEY `idx_leave_step` (`leave_id`, `step_order`),
  KEY `approver_role_id` (`approver_role_id`),
  UNIQUE KEY `uk_leave_step_teacher` (`leave_id`, `step_order`, `teacher_id`),
  CONSTRAINT `fk_la_leave` FOREIGN KEY (`leave_id`) REFERENCES `leave_request` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_la_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_la_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `approval_workflow` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_la_role` FOREIGN KEY (`approver_role_id`) REFERENCES `role`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `leave_type_workflow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `leave_type_id` int NOT NULL COMMENT '请假类型ID',
  `workflow_id` int NOT NULL COMMENT '审批流程ID',
  `condition_expression` text COMMENT '条件表达式',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `leave_type_id` (`leave_type_id`),
  KEY `workflow_id` (`workflow_id`),
  UNIQUE KEY `uk_leave_type_unique` (`leave_type_id`),
  CONSTRAINT `leave_type_workflow_ibfk_1` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_type_workflow_ibfk_2` FOREIGN KEY (`workflow_id`) REFERENCES `approval_workflow` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role_assignment` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `approval_role_id` INT NOT NULL COMMENT '审批角色ID',
  `teacher_id` INT NOT NULL COMMENT '教师ID',
  `class_id` INT DEFAULT NULL COMMENT '班级作用域',
  `department_id` INT DEFAULT NULL COMMENT '系部作用域',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级作用域',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `class_id` (`class_id`),
  KEY `department_id` (`department_id`),
  KEY `grade` (`grade`),
  KEY `idx_scope_role` (`approval_role_id`, `class_id`, `grade`),
  CONSTRAINT `fk_ra_role` FOREIGN KEY (`approval_role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_class` FOREIGN KEY (`class_id`) REFERENCES `class`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_department` FOREIGN KEY (`department_id`) REFERENCES `department`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notice_read_status` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notice_id` int NOT NULL COMMENT '通知ID',
  `user_id` int NOT NULL COMMENT '阅读用户ID',
  `read_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `notice_id` (`notice_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notice_read_status_ibfk_1` FOREIGN KEY (`notice_id`) REFERENCES `class_notice` (`id`) ON DELETE CASCADE,
  CONSTRAINT `notice_read_status_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int NOT NULL COMMENT '操作用户ID',
  `operation` text NOT NULL COMMENT '操作内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `operation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `role_id` INT NOT NULL COMMENT '角色ID',
  `permission_id` INT NOT NULL COMMENT '权限ID',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `granted_by` INT DEFAULT NULL COMMENT '授权人ID',
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`),
  KEY `permission_id` (`permission_id`),
  KEY `granted_by` (`granted_by`),
  CONSTRAINT `rp_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `rp_perm_fk` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `rp_user_fk` FOREIGN KEY (`granted_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '正文',
  `priority` ENUM('LOW','NORMAL','HIGH','CRITICAL') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
  `channels_bitmask` INT NOT NULL DEFAULT 1 COMMENT '渠道位图',
  `dedupe_key` VARCHAR(150) DEFAULT NULL COMMENT '幂等键',
  `business_ref_type` VARCHAR(50) DEFAULT NULL COMMENT '业务引用类型',
  `business_ref_id` VARCHAR(64) DEFAULT NULL COMMENT '业务引用ID',
  `template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板代码',
  `template_version` INT DEFAULT NULL COMMENT '模板版本',
  `rendered_variables_json` JSON DEFAULT NULL COMMENT '变量快照',
  `extra_json` JSON DEFAULT NULL COMMENT '扩展',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_created` (`type`, `created_at`),
  KEY `idx_business_ref` (`business_ref_type`, `business_ref_id`),
  UNIQUE KEY `uk_dedupe_key` (`dedupe_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification_recipient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notification_id` BIGINT NOT NULL COMMENT '通知ID',
  `user_id` INT NOT NULL COMMENT '接收用户ID',
  `inbox_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '站内消息',
  `email_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮件',
  `email_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮件已发送',
  `read_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '已读状态',
  `read_at` TIMESTAMP NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_notification` (`notification_id`),
  KEY `idx_user_unread` (`user_id`, `read_status`),
  UNIQUE KEY `uk_notification_user` (`notification_id`, `user_id`),
  CONSTRAINT `fk_nr_notification` FOREIGN KEY (`notification_id`) REFERENCES `notification`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_nr_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification_preference` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `notification_type` VARCHAR(50) NOT NULL COMMENT '类型代码',
  `channel` VARCHAR(20) NOT NULL COMMENT '渠道',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_channel` (`user_id`,`notification_type`,`channel`),
  KEY `idx_user_channel` (`user_id`,`channel`),
  CONSTRAINT `fk_np_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `credit_warning_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用',
  `threshold_value` DECIMAL(7,2) NOT NULL COMMENT '阈值',
  `suspend_threshold_value` DECIMAL(7,2) DEFAULT NULL COMMENT '暂停阈值',
  `evaluation_cycle` ENUM('GLOBAL','MONTH','TERM','WEEK') NOT NULL DEFAULT 'GLOBAL' COMMENT '周期',
  `resend_interval_days` INT NOT NULL DEFAULT 14 COMMENT '重发间隔',
  `min_remind_gap_days` INT NOT NULL DEFAULT 3 COMMENT '最小提醒间隔',
  `force_resend_interval_days` INT DEFAULT NULL COMMENT '强制重发间隔',
  `require_previous_read_before_resend` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '需已读后重发',
  `max_resend_count` INT DEFAULT NULL COMMENT '最大重发次数',
  `channels_default` INT NOT NULL DEFAULT 1 COMMENT '渠道位图',
  `email_default_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮件默认',
  `escalation_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '升级策略',
  `template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板代码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_active_threshold` (`active`,`threshold_value`),
  KEY `idx_cycle` (`evaluation_cycle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `credit_warning_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `student_id` INT NOT NULL COMMENT '学生ID',
  `config_id` BIGINT NOT NULL COMMENT '配置ID',
  `cycle_key` VARCHAR(32) NOT NULL COMMENT '周期键',
  `unresolved` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '未恢复',
  `last_trigger_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最近触发',
  `last_notified_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最近通知',
  `last_notification_id` BIGINT DEFAULT NULL COMMENT '最近通知ID',
  `last_notification_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '最近已读',
  `resend_count` INT NOT NULL DEFAULT 0 COMMENT '重发次数',
  `skipped_count` INT NOT NULL DEFAULT 0 COMMENT '跳过次数',
  `resolved_time` TIMESTAMP NULL DEFAULT NULL COMMENT '恢复时间',
  `force_resend_marker_time` TIMESTAMP NULL DEFAULT NULL COMMENT '强制提醒时间',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_config_cycle` (`student_id`,`config_id`,`cycle_key`),
  KEY `idx_student_unresolved` (`student_id`,`unresolved`),
  KEY `idx_config_cycle` (`config_id`,`cycle_key`),
  CONSTRAINT `fk_cws_student` FOREIGN KEY (`student_id`) REFERENCES `student`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cws_config` FOREIGN KEY (`config_id`) REFERENCES `credit_warning_config`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cws_notification` FOREIGN KEY (`last_notification_id`) REFERENCES `notification`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `code` VARCHAR(100) NOT NULL COMMENT '模板编码',
  `channel` VARCHAR(20) DEFAULT NULL COMMENT '渠道',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本',
  `status` ENUM('DRAFT','ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `title_template` TEXT NOT NULL COMMENT '标题模板',
  `content_template` TEXT NOT NULL COMMENT '内容模板',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `sample_variables` JSON DEFAULT NULL COMMENT '示例变量',
  `effective_at` TIMESTAMP NULL DEFAULT NULL COMMENT '生效时间',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_channel_active` (`code`,`channel`,`version`),
  KEY `idx_code_status` (`code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

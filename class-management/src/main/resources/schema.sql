-- =============================================================
-- 基础权限与统一角色体系（支持系统角色+审批角色层级）
-- =============================================================

CREATE TABLE `permission` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称(唯一代码)',
  `display_name` VARCHAR(100) NULL COMMENT '显示名称(可选)',
  `description` TEXT COMMENT '权限描述',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_name` (`permission_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 统一角色表：包含系统登录角色 与 审批流程角色
-- category: SYSTEM (登录/访问控制) | APPROVAL (审批流程用)
CREATE TABLE `role` (
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


-- class_management.teacher definition

CREATE TABLE `teacher` (
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


-- 系部表（需在 class 之前创建，供外键引用）
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


-- class_management.class definition

CREATE TABLE `class` (
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
  CONSTRAINT `class_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
  ,CONSTRAINT `fk_class_department` FOREIGN KEY (`department_id`) REFERENCES `department`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 新增：系部表与班级-系部关联（需在引用前创建）
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



-- class_management.class_notice definition

CREATE TABLE `class_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `teacher_id` int NOT NULL COMMENT '发布教师ID',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `class_notice_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.student definition

CREATE TABLE `student` (
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


-- class_management.teacher_subject definition

CREATE TABLE `teacher_subject` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `teacher_id` int NOT NULL COMMENT '教师ID',
  `subject` varchar(50) NOT NULL COMMENT '科目名称',
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `teacher_subject_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.grade definition

CREATE TABLE `grade` (
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


-- class_management.credit definition


-- 学分配置项表：德/智/体/美/劳各类下可配置多个项目
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

-- 学生学分表：每个学生在每个项目下的一条记录
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

-- 学分子项目表：隶属于主项目（德/智/体/美/劳之一）
CREATE TABLE IF NOT EXISTS `credit_subitem` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `item_id` INT NOT NULL COMMENT '关联 credit_item 主项目 ID',
  `subitem_name` VARCHAR(100) NOT NULL COMMENT '子项目名称',
  `initial_score` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '子项目初始分值',
  `max_score` DECIMAL(5,2) NOT NULL DEFAULT 100 COMMENT '子项目最高得分',
  `weight` DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '在父项目中的权重比例（0–1），暂不参与计算，仅配置',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_subitem` (`item_id`, `subitem_name`),
  CONSTRAINT `fk_cs_item` FOREIGN KEY (`item_id`) REFERENCES `credit_item`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生评价汇总表：存储每个学生的总分与评判等级
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

-- class_management.parent definition

CREATE TABLE `parent` (
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


-- class_management.`user` definition

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `identity_no` varchar(20) DEFAULT NULL COMMENT '学号',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_type` varchar(50) NOT NULL COMMENT '用户类型',
  `related_id` int DEFAULT NULL COMMENT '关联的身份ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `identity_no` (`identity_no`),
  KEY `related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.user_role definition

CREATE TABLE `user_role` (
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


-- class_management.leave_request definition

-- 请假类型配置表
CREATE TABLE `leave_type_config` (
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

-- 学生请假余额表
CREATE TABLE `student_leave_balance` (
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

-- MinIO存储配置表
CREATE TABLE `file_storage_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `bucket_name` varchar(100) NOT NULL COMMENT '存储桶名称',
  `bucket_purpose` varchar(100) NOT NULL COMMENT '存储桶用途',
  `max_file_size` bigint NOT NULL DEFAULT 5242880 COMMENT '最大文件大小（字节，默认5MB）',
  `allowed_extensions` json NOT NULL COMMENT '允许的文件扩展名',
  `allowed_mime_types` json NOT NULL COMMENT '允许的MIME类型',
  `retention_days` int NOT NULL DEFAULT 365 COMMENT '文件保留天数',
  `auto_cleanup` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动清理过期文件',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bucket_name` (`bucket_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leave_request` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `student_id` int NOT NULL COMMENT '请假学生ID',
  `leave_type_id` int NOT NULL COMMENT '请假类型ID',
  `start_date` date NOT NULL COMMENT '请假开始日期',
  `end_date` date NOT NULL COMMENT '请假结束日期',
  `days` DECIMAL(5,2) NOT NULL COMMENT '请假天数（支持小数，半天=0.5，单位为天，保留两位小数）',
  `reason` text NOT NULL COMMENT '请假原因',
  `emergency_contact` varchar(50) NOT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) NOT NULL COMMENT '紧急联系电话',
  `handover_notes` text COMMENT '交接说明',
  `attachment_count` int NOT NULL DEFAULT 0 COMMENT '附件数量',
  `status` enum('草稿','待审批','已批准','已拒绝','已撤销') DEFAULT '待审批' COMMENT '审批状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `leave_type_id` (`leave_type_id`),
  KEY `status` (`status`),
  KEY `start_date` (`start_date`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_request_ibfk_3` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type_config` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 请假附件表（支持MinIO存储）
CREATE TABLE `leave_attachment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `leave_request_id` int NOT NULL COMMENT '请假申请ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名（UUID）',
  `file_path` varchar(500) NOT NULL COMMENT 'MinIO存储路径',
  `bucket_name` varchar(100) NOT NULL DEFAULT 'leave-attachments' COMMENT 'MinIO存储桶名称',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) NOT NULL COMMENT '文件MIME类型',
  `file_extension` varchar(10) NOT NULL COMMENT '文件扩展名',
  `upload_status` enum('uploading','completed','failed') DEFAULT 'uploading' COMMENT '上传状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `created_by` int NOT NULL COMMENT '上传者ID',
  PRIMARY KEY (`id`),
  KEY `leave_request_id` (`leave_request_id`),
  KEY `created_by` (`created_by`),
  KEY `upload_status` (`upload_status`),
  CONSTRAINT `leave_attachment_ibfk_1` FOREIGN KEY (`leave_request_id`) REFERENCES `leave_request` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_attachment_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 审批流程模板表
CREATE TABLE `approval_workflow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '流程ID',
  `workflow_name` varchar(100) NOT NULL COMMENT '流程名称',
  `workflow_code` varchar(50) NOT NULL COMMENT '流程代码（用于程序识别）',
  `description` text COMMENT '流程描述',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `workflow_code` (`workflow_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `approval_step` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
  `workflow_id` INT NOT NULL COMMENT '所属流程ID',
  `step_order` INT NOT NULL COMMENT '步骤顺序（1,2,3...）',
  `step_name` VARCHAR(50) NOT NULL COMMENT '步骤名称',
  `approver_role_id` INT NOT NULL COMMENT '审批角色ID(引用 role)',
  `auto_approve` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动通过',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`),
  KEY `workflow_id` (`workflow_id`),
  KEY `approver_role_id` (`approver_role_id`),
  UNIQUE KEY `uk_workflow_step` (`workflow_id`, `step_order`),
  CONSTRAINT `fk_as_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `approval_workflow` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_as_role` FOREIGN KEY (`approver_role_id`) REFERENCES `role`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.leave_approval definition（放在 workflow 与 step 之后，保证外键可用）

CREATE TABLE `leave_approval` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
  `leave_id` INT NOT NULL COMMENT '请假申请ID',
  `teacher_id` INT NOT NULL COMMENT '审批教师ID',
  `workflow_id` INT DEFAULT NULL COMMENT '所属审批流程ID',
  `step_order` INT NOT NULL DEFAULT 1 COMMENT '审批步骤序号',
  `step_name` VARCHAR(50) DEFAULT NULL COMMENT '审批步骤名称',
  `approver_role_id` INT DEFAULT NULL COMMENT '审批角色ID(冗余快照，可为空)',
  `status` ENUM('待审批','已批准','已拒绝') NOT NULL DEFAULT '待审批' COMMENT '审批结果',
  `reviewed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '审批完成时间',
  `comment` TEXT COMMENT '审批意见',
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

CREATE TABLE `leave_type_workflow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `leave_type_id` int NOT NULL COMMENT '请假类型ID',
  `workflow_id` int NOT NULL COMMENT '审批流程ID',
  `condition_expression` text COMMENT '可空；条件表达式（JSON），通常留空，表示直接绑定流程',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `leave_type_id` (`leave_type_id`),
  KEY `workflow_id` (`workflow_id`),
  UNIQUE KEY `uk_leave_type_unique` (`leave_type_id`),
  CONSTRAINT `leave_type_workflow_ibfk_1` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_type_workflow_ibfk_2` FOREIGN KEY (`workflow_id`) REFERENCES `approval_workflow` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 按班级覆盖的请假类型-流程关联：当存在匹配记录时，优先于全局 leave_type_workflow

-- 审批人绑定：将“审批角色”在不同作用域（班级/年级/校级）绑定到具体教师，无需改代码即可更换审批人
CREATE TABLE `role_assignment` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `approval_role_id` INT NOT NULL COMMENT '审批角色ID(role.category=APPROVAL)',
  `teacher_id` INT NOT NULL COMMENT '被绑定的教师ID',
  `class_id` INT DEFAULT NULL COMMENT '班级作用域（优先级最高）',
  `department_id` INT DEFAULT NULL COMMENT '系部作用域（次于班级，先于年级）',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级作用域（如：高一/初二）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `class_id` (`class_id`),
  KEY `department_id` (`department_id`),
  KEY `grade` (`grade`),
  KEY `idx_scope_role` (`approval_role_id`, `class_id`, `grade`),
  CONSTRAINT `fk_ra_role` FOREIGN KEY (`approval_role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ra_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.notice_read_status definition

CREATE TABLE `notice_read_status` (
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


-- class_management.operation_log definition

CREATE TABLE `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int NOT NULL COMMENT '操作用户ID',
  `operation` text NOT NULL COMMENT '操作内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `operation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.role_permission definition

CREATE TABLE `role_permission` (
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

-- =============================================
-- 默认角色数据 (系统角色 + 审批角色层级)
-- =============================================
INSERT INTO `role` (`code`,`display_name`,`category`,`level`,`sort_order`,`description`) VALUES
 ('STUDENT','学生','SYSTEM',1,10,'系统登录学生'),
 ('TEACHER','教师','SYSTEM',1,20,'系统登录教师'),
 ('PARENT','家长','SYSTEM',1,30,'系统登录家长'),
 ('ADMIN','管理员','SYSTEM',1,40,'系统管理员'),
 ('HOMEROOM','班主任','APPROVAL',1,100,'班级第一层审批'),
 ('DEPT_HEAD','系部主任','APPROVAL',2,110,'系部层审批'),
 ('GRADE_HEAD','年级主任','APPROVAL',3,120,'年级层审批'),
 ('ACADEMIC_DIRECTOR','教务主任','APPROVAL',4,130,'教务层审批'),
 ('PRINCIPAL','校长','APPROVAL',5,140,'最高审批');

-- 方案 C：不使用 parent_id 表达审批或组织顺序，全部保持 NULL。
-- 审批顺序仅由 approval_workflow / approval_step 控制；parent_id 保留列以备未来扩展。

-- 插入默认请假类型配置数据
INSERT INTO `leave_type_config` (`type_code`, `type_name`, `max_days_per_request`, `annual_allowance`, `requires_approval`, `requires_medical_proof`, `advance_days_required`, `color`, `description`) VALUES
('annual', '年假', 30, 15, 1, 0, 3, '#1976d2', '每年享有的带薪年假，需提前申请'),
('sick', '病假', 90, 10, 1, 1, 1, '#388e3c', '因病需要休息，需提供医疗证明'),
('personal', '事假', 10, 5, 1, 0, 1, '#f57c00', '因个人事务需要请假'),
('maternity', '产假', 128, 128, 1, 1, 30, '#e91e63', '女性员工生育期间的带薪假期'),
('emergency', '紧急事假', 3, 3, 1, 0, 0, '#f44336', '突发紧急情况的临时请假');

-- 插入MinIO存储配置数据
INSERT INTO `file_storage_config` (`bucket_name`, `bucket_purpose`, `max_file_size`, `allowed_extensions`, `allowed_mime_types`, `retention_days`, `auto_cleanup`) VALUES
('leave-attachments', '请假申请附件', 5242880, '["pdf", "jpg", "jpeg", "png", "doc", "docx"]', '["application/pdf", "image/jpeg", "image/png", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]', 1095, 0),
('student-documents', '学生证明文件', 10485760, '["pdf", "jpg", "jpeg", "png"]', '["application/pdf", "image/jpeg", "image/png"]', 2190, 0),
('system-backups', '系统备份文件', 1073741824, '["zip", "sql", "tar", "gz"]', '["application/zip", "application/sql", "application/x-tar", "application/gzip"]', 90, 1);

-- 插入审批流程模板
INSERT INTO `approval_workflow` (`workflow_name`, `workflow_code`, `description`) VALUES
('单级审批', 'single_level', '班主任直接审批'),
('两级审批', 'two_level', '班主任 -> 系部主任'),
('三级审批', 'three_level', '班主任 -> 系部主任 -> 年级主任'),
('校长审批', 'principal_approval', '班主任 -> 系部主任 -> 年级主任 -> 校长');

-- 插入审批步骤
-- 单级审批
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`) VALUES
(1, 1, '班主任审批', (SELECT id FROM role WHERE code='HOMEROOM'));

-- 两级审批
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`) VALUES
(2, 1, '班主任审批', (SELECT id FROM role WHERE code='HOMEROOM')),
(2, 2, '系部主任审批', (SELECT id FROM role WHERE code='DEPT_HEAD'));

-- 三级审批
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`) VALUES
(3, 1, '班主任审批', (SELECT id FROM role WHERE code='HOMEROOM')),
(3, 2, '系部主任审批', (SELECT id FROM role WHERE code='DEPT_HEAD')),
(3, 3, '年级主任审批', (SELECT id FROM role WHERE code='GRADE_HEAD'));

-- 校长审批
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`) VALUES
(4, 1, '班主任审批', (SELECT id FROM role WHERE code='HOMEROOM')),
(4, 2, '系部主任审批', (SELECT id FROM role WHERE code='DEPT_HEAD')),
(4, 3, '年级主任审批', (SELECT id FROM role WHERE code='GRADE_HEAD')),
(4, 4, '校长审批', (SELECT id FROM role WHERE code='PRINCIPAL'));

-- 配置请假类型与审批流程的关联
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`) VALUES
((SELECT id FROM leave_type_config WHERE type_code = 'annual'), 2, NULL),
((SELECT id FROM leave_type_config WHERE type_code = 'sick'), 1, NULL),
((SELECT id FROM leave_type_config WHERE type_code = 'personal'), 1, NULL),
((SELECT id FROM leave_type_config WHERE type_code = 'maternity'), 4, NULL),
((SELECT id FROM leave_type_config WHERE type_code = 'emergency'), 1, NULL);

-- 旧的基于 ENUM 的审批角色已被统一角色表替代，上述注释保留说明，不再使用。
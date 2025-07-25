-- class_management.permission definition

CREATE TABLE `permission` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
  `description` text COMMENT '权限描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `permission_name` (`permission_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.`role` definition

CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` enum('学生','教师','家长','管理员') NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


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


-- class_management.class definition

CREATE TABLE `class` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name` varchar(50) NOT NULL COMMENT '班级名称',
  `teacher_id` int DEFAULT NULL COMMENT '班主任ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `class_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


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


-- class_management.leave_request definition

CREATE TABLE `leave_request` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `student_id` int NOT NULL COMMENT '请假学生ID',
  `teacher_id` int DEFAULT NULL COMMENT '审批教师ID',
  `leave_type` enum('病假','事假','其他') NOT NULL COMMENT '请假类型',
  `reason` text NOT NULL COMMENT '请假原因',
  `start_date` date NOT NULL COMMENT '请假开始日期',
  `end_date` date NOT NULL COMMENT '请假结束日期',
  `status` enum('待审批','已批准','已拒绝') DEFAULT '待审批' COMMENT '审批状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_request_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- class_management.moral_score definition

CREATE TABLE `moral_score` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `teacher_id` int DEFAULT NULL COMMENT '评分教师ID',
  `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
  `score` decimal(5,2) NOT NULL COMMENT '获得学分',
  `status` enum('待审核','已通过','已拒绝') DEFAULT '待审核' COMMENT '审批状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `student_id` (`student_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `moral_score_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `moral_score_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


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


-- class_management.leave_approval definition

CREATE TABLE `leave_approval` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
  `leave_id` int NOT NULL COMMENT '请假申请ID',
  `teacher_id` int NOT NULL COMMENT '审批教师ID',
  `status` enum('待审批','已批准','已拒绝') NOT NULL COMMENT '审批结果',
  `reviewed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `comment` text COMMENT '审批意见',
  PRIMARY KEY (`id`),
  KEY `leave_id` (`leave_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `leave_approval_ibfk_1` FOREIGN KEY (`leave_id`) REFERENCES `leave_request` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leave_approval_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
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
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  `permission_id` int NOT NULL COMMENT '权限ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `granted_by` int DEFAULT NULL COMMENT '授权人ID',
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`),
  KEY `permission_id` (`permission_id`),
  KEY `granted_by` (`granted_by`),
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_permission_ibfk_3` FOREIGN KEY (`granted_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
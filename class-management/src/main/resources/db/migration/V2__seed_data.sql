-- Flyway V2 Seed Data (Idempotent)
-- 使用 INSERT IGNORE 或 ON DUPLICATE KEY UPDATE 方式确保重复执行不报错

-- 使用传统 VALUES(col) 方式（MySQL 8.0.20+ 虽标记弃用但仍可用；后续可改为单行 INSERT + 行别名）
INSERT INTO `role` (`code`,`display_name`,`category`,`level`,`sort_order`,`description`)
VALUES 
 ('STUDENT','学生','SYSTEM',1,10,'系统登录学生'),
 ('TEACHER','教师','SYSTEM',1,20,'系统登录教师'),
 ('PARENT','家长','SYSTEM',1,30,'系统登录家长'),
 ('ADMIN','管理员','SYSTEM',1,40,'系统管理员'),
 ('HOMEROOM','班主任','APPROVAL',1,100,'班级第一层审批'),
 ('DEPT_HEAD','系部主任','APPROVAL',2,110,'系部层审批'),
 ('GRADE_HEAD','年级主任','APPROVAL',3,120,'年级层审批'),
 ('ACADEMIC_DIRECTOR','教务主任','APPROVAL',4,130,'教务层审批'),
 ('PRINCIPAL','校长','APPROVAL',5,140,'最高审批')
ON DUPLICATE KEY UPDATE
  `display_name`=VALUES(`display_name`),
  `category`=VALUES(`category`),
  `level`=VALUES(`level`),
  `sort_order`=VALUES(`sort_order`),
  `description`=VALUES(`description`);

-- 请假类型
INSERT INTO `leave_type_config` (`type_code`, `type_name`, `max_days_per_request`, `annual_allowance`, `requires_approval`, `requires_medical_proof`, `advance_days_required`, `color`, `description`)
VALUES
('annual', '年假', 30, 15, 1, 0, 3, '#1976d2', '每年享有的带薪年假，需提前申请'),
('sick', '病假', 90, 10, 1, 1, 1, '#388e3c', '因病需要休息，需提供医疗证明'),
('personal', '事假', 10, 5, 1, 0, 1, '#f57c00', '因个人事务需要请假'),
('maternity', '产假', 128, 128, 1, 1, 30, '#e91e63', '女性员工生育期间的带薪假期'),
('emergency', '紧急事假', 3, 3, 1, 0, 0, '#f44336', '突发紧急情况的临时请假')
ON DUPLICATE KEY UPDATE
  `type_name`=VALUES(`type_name`),
  `max_days_per_request`=VALUES(`max_days_per_request`),
  `annual_allowance`=VALUES(`annual_allowance`),
  `requires_approval`=VALUES(`requires_approval`),
  `requires_medical_proof`=VALUES(`requires_medical_proof`),
  `advance_days_required`=VALUES(`advance_days_required`),
  `color`=VALUES(`color`),
  `description`=VALUES(`description`);

-- MinIO bucket 配置
-- INSERT INTO `file_storage_config` (`bucket_name`, `bucket_purpose`, `max_file_size`, `allowed_extensions`, `allowed_mime_types`, `retention_days`, `auto_cleanup`)
-- VALUES
-- ('leave-attachments', '请假申请附件', 5242880, '[["pdf", "jpg", "jpeg", "png", "doc", "docx"]]', '[["application/pdf", "image/jpeg", "image/png", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]]', 1095, 0),
-- ('student-documents', '学生证明文件', 10485760, '[["pdf", "jpg", "jpeg", "png"]]', '[["application/pdf", "image/jpeg", "image/png"]]', 2190, 0),
-- ('system-backups', '系统备份文件', 1073741824, '[["zip", "sql", "tar", "gz"]]', '[["application/zip", "application/sql", "application/x-tar", "application/gzip"]]', 90, 1)
-- ON DUPLICATE KEY UPDATE
--   `bucket_purpose`=VALUES(`bucket_purpose`),
--   `max_file_size`=VALUES(`max_file_size`),
--   `allowed_extensions`=VALUES(`allowed_extensions`),
--   `allowed_mime_types`=VALUES(`allowed_mime_types`),
--   `retention_days`=VALUES(`retention_days`),
--   `auto_cleanup`=VALUES(`auto_cleanup`);

-- 审批流程模板
INSERT INTO `approval_workflow` (`workflow_name`, `workflow_code`, `description`)
VALUES
('单级审批', 'single_level', '班主任直接审批'),
('两级审批', 'two_level', '班主任 -> 系部主任'),
('三级审批', 'three_level', '班主任 -> 系部主任 -> 年级主任'),
('校长审批', 'principal_approval', '班主任 -> 系部主任 -> 年级主任 -> 校长')
ON DUPLICATE KEY UPDATE `description`=VALUES(`description`);

-- 审批步骤（需要角色已存在）
-- 为避免重复插入，先判断是否存在记录
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 1,1,'班主任审批', r.id FROM role r WHERE r.code='HOMEROOM' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=1 AND s.step_order=1
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 2,1,'班主任审批', r1.id FROM role r1 WHERE r1.code='HOMEROOM' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=2 AND s.step_order=1
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 2,2,'系部主任审批', r2.id FROM role r2 WHERE r2.code='DEPT_HEAD' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=2 AND s.step_order=2
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 3,1,'班主任审批', r1.id FROM role r1 WHERE r1.code='HOMEROOM' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=3 AND s.step_order=1
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 3,2,'系部主任审批', r2.id FROM role r2 WHERE r2.code='DEPT_HEAD' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=3 AND s.step_order=2
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 3,3,'年级主任审批', r3.id FROM role r3 WHERE r3.code='GRADE_HEAD' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=3 AND s.step_order=3
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 4,1,'班主任审批', r1.id FROM role r1 WHERE r1.code='HOMEROOM' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=4 AND s.step_order=1
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 4,2,'系部主任审批', r2.id FROM role r2 WHERE r2.code='DEPT_HEAD' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=4 AND s.step_order=2
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 4,3,'年级主任审批', r3.id FROM role r3 WHERE r3.code='GRADE_HEAD' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=4 AND s.step_order=3
);
INSERT INTO `approval_step` (`workflow_id`, `step_order`, `step_name`, `approver_role_id`)
SELECT 4,4,'校长审批', r4.id FROM role r4 WHERE r4.code='PRINCIPAL' AND NOT EXISTS(
  SELECT 1 FROM approval_step s WHERE s.workflow_id=4 AND s.step_order=4
);

-- 请假类型绑定审批流程（若不存在）
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`)
SELECT (SELECT id FROM leave_type_config WHERE type_code='annual'),2,NULL FROM dual WHERE NOT EXISTS(
  SELECT 1 FROM leave_type_workflow l WHERE l.leave_type_id=(SELECT id FROM leave_type_config WHERE type_code='annual')
);
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`)
SELECT (SELECT id FROM leave_type_config WHERE type_code='sick'),1,NULL FROM dual WHERE NOT EXISTS(
  SELECT 1 FROM leave_type_workflow l WHERE l.leave_type_id=(SELECT id FROM leave_type_config WHERE type_code='sick')
);
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`)
SELECT (SELECT id FROM leave_type_config WHERE type_code='personal'),1,NULL FROM dual WHERE NOT EXISTS(
  SELECT 1 FROM leave_type_workflow l WHERE l.leave_type_id=(SELECT id FROM leave_type_config WHERE type_code='personal')
);
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`)
SELECT (SELECT id FROM leave_type_config WHERE type_code='maternity'),4,NULL FROM dual WHERE NOT EXISTS(
  SELECT 1 FROM leave_type_workflow l WHERE l.leave_type_id=(SELECT id FROM leave_type_config WHERE type_code='maternity')
);
INSERT INTO `leave_type_workflow` (`leave_type_id`, `workflow_id`, `condition_expression`)
SELECT (SELECT id FROM leave_type_config WHERE type_code='emergency'),1,NULL FROM dual WHERE NOT EXISTS(
  SELECT 1 FROM leave_type_workflow l WHERE l.leave_type_id=(SELECT id FROM leave_type_config WHERE type_code='emergency')
);

-- 学分预警默认配置
INSERT INTO `credit_warning_config` (`active`,`threshold_value`,`evaluation_cycle`,`resend_interval_days`,`min_remind_gap_days`,`require_previous_read_before_resend`,`channels_default`,`email_default_enabled`,`template_code`,`description`)
VALUES (1,60,'GLOBAL',14,3,1,1,0,'CREDIT_WARNING_DEFAULT','默认学分预警配置：低于或等于60触发')
ON DUPLICATE KEY UPDATE
  `threshold_value`=VALUES(`threshold_value`),
  `evaluation_cycle`=VALUES(`evaluation_cycle`),
  `resend_interval_days`=VALUES(`resend_interval_days`),
  `min_remind_gap_days`=VALUES(`min_remind_gap_days`),
  `require_previous_read_before_resend`=VALUES(`require_previous_read_before_resend`),
  `channels_default`=VALUES(`channels_default`),
  `email_default_enabled`=VALUES(`email_default_enabled`),
  `description`=VALUES(`description`);

-- 通知模板
INSERT INTO `notification_template` (`code`,`channel`,`version`,`status`,`title_template`,`content_template`,`remark`,`sample_variables`)
VALUES
('LEAVE_SUBMITTED_TO_APPROVER',NULL,1,'ACTIVE','{studentName} 的请假申请待审批','学生：{studentName}（学号 {studentNo}）\n班级：{className}（{departmentName}）\n类型：{leaveTypeName}\n时间：{startDate} ~ {endDate}（共 {days} 天）\n申请编号：#{leaveId}\n请尽快处理。','初始版本',NULL),
('LEAVE_STEP_ADVANCED_TO_APPROVER',NULL,1,'ACTIVE','请假流程进入 {currentStepName}','申请 #{leaveId} 已进入你的审批：{currentStepName}\n学生：{studentName}（{className}/{departmentName}）\n类型：{leaveTypeName}\n时间：{startDate}~{endDate}','初始版本',NULL),
('LEAVE_APPROVED_TO_STUDENT',NULL,1,'ACTIVE','请假申请已批准','你的请假申请 #{leaveId} 已批准。\n类型：{leaveTypeName}\n时间：{startDate}~{endDate}','初始版本',NULL),
('LEAVE_REJECTED_TO_STUDENT',NULL,1,'ACTIVE','请假申请已被拒绝','你的请假申请 #{leaveId} 被拒绝。\n类型：{leaveTypeName}\n时间：{startDate}~{endDate}\n原因：{rejectReason}','初始版本',NULL),
('LEAVE_AUTO_APPROVED_TO_STUDENT',NULL,1,'ACTIVE','请假自动批准','你的请假申请 #{leaveId} 已自动批准（无需人工审批）。\n类型：{leaveTypeName}\n时间：{startDate}~{endDate}','初始版本',NULL)
ON DUPLICATE KEY UPDATE
  `status`=VALUES(`status`),
  `title_template`=VALUES(`title_template`),
  `content_template`=VALUES(`content_template`),
  `remark`=VALUES(`remark`);

-- 更新 task_assignments 表结构以支持新的任务分工功能

-- 首先备份现有数据（如果有的话）
CREATE TABLE IF NOT EXISTS `task_assignments_backup` AS SELECT * FROM `task_assignments`;

-- 删除现有表
DROP TABLE IF EXISTS `task_assignments`;

-- 重新创建 task_assignments 表
CREATE TABLE `task_assignments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(100) NULL COMMENT '角色',
    `description` TEXT NOT NULL COMMENT '任务描述',
    `assigned_by` BIGINT NOT NULL COMMENT '分配人ID',
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT(草稿), SUBMITTED(已提交), FINALIZED(已确定)',
    PRIMARY KEY (`id`),
    INDEX `idx_ta_task` (`task_id`),
    INDEX `idx_ta_user` (`user_id`),
    INDEX `idx_ta_assigned_by` (`assigned_by`),
    CONSTRAINT `fk_ta_task` FOREIGN KEY (`task_id`) REFERENCES `tasks`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ta_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ta_assigned_by` FOREIGN KEY (`assigned_by`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 
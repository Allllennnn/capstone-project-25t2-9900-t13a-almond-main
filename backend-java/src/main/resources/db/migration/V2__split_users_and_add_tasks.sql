-- 子表：学生、教师、管理员 —— 与 users 表一对一
CREATE TABLE IF NOT EXISTS `students` (
                                          `user_id` BIGINT NOT NULL,
                                          `student_no` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_students_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `teachers` (
                                          `user_id` BIGINT NOT NULL,
                                          PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_teachers_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `admins` (
                                        `user_id` BIGINT NOT NULL,
                                        PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_admins_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 从 users 表中移除 student_no 字段（先判断存在）
SET @column_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'student_no' AND TABLE_SCHEMA = DATABASE()
);
SET @sql := IF(@column_exists > 0, 'ALTER TABLE `users` DROP COLUMN `student_no`;', 'SELECT "Column student_no does not exist."');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 小组任务表
CREATE TABLE IF NOT EXISTS `tasks` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `group_id` BIGINT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING/IN_PROGRESS/COMPLETED
    `due_date` DATE NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_tasks_group` (`group_id`),
    CONSTRAINT `fk_tasks_group` FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务分配表
CREATE TABLE IF NOT EXISTS `task_assignments` (
                                                  `task_id` BIGINT NOT NULL,
                                                  `student_id` BIGINT NOT NULL,
                                                  `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (`task_id`, `student_id`),
    INDEX `idx_ta_task` (`task_id`),
    INDEX `idx_ta_student` (`student_id`),
    CONSTRAINT `fk_ta_task` FOREIGN KEY (`task_id`) REFERENCES `tasks`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ta_student` FOREIGN KEY (`student_id`) REFERENCES `students`(`user_id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

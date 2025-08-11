-- 通用用户表
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NULL,
    `role` VARCHAR(20) NOT NULL,  -- ADMIN / TEACHER / STUDENT
    `student_no` VARCHAR(50) NULL,  -- 仅学生填写
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / ACTIVE
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小组表
CREATE TABLE IF NOT EXISTS `groups` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                                        `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `teacher_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_groups_teacher` (`teacher_id`),
    CONSTRAINT `fk_groups_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户-小组关联表
CREATE TABLE IF NOT EXISTS `user_group` (
                                            `user_id` BIGINT NOT NULL,
                                            `group_id` BIGINT NOT NULL,
                                            `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`user_id`, `group_id`),
    INDEX `idx_ug_user` (`user_id`),
    INDEX `idx_ug_group` (`group_id`),
    CONSTRAINT `fk_ug_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ug_group` FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

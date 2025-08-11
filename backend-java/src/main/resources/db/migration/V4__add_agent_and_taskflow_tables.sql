-- V4__add_agent_and_taskflow_tables.sql

-- 1. 将旧有 tasks.status 中的 PENDING 更新为 INITIALIZING（避免 ENUM 修改失败）
UPDATE `tasks`
SET status = 'INITIALIZING'
WHERE status = 'PENDING';

-- 2. 修改 tasks.status 枚举为项目三态
ALTER TABLE `tasks`
    MODIFY COLUMN `status`
    ENUM('INITIALIZING','IN_PROGRESS','COMPLETED')
    NOT NULL
    DEFAULT 'INITIALIZING';

-- 3. 创建 meetings 表：记录每次 Meeting 文档与状态
CREATE TABLE IF NOT EXISTS `meetings` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                                          `group_id` BIGINT NOT NULL COMMENT '所属小组',
                                          `task_id`  BIGINT NOT NULL COMMENT '所属 Task',
                                          `meeting_no` TINYINT NOT NULL COMMENT '第几次 Meeting',
                                          `meeting_date` DATE NOT NULL COMMENT '会议日期',
                                          `status` ENUM('UNFINISHED','COMPLETED') NOT NULL DEFAULT 'UNFINISHED',
    `document_url` VARCHAR(512) NULL COMMENT '摘要文档存储路径',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL
    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_meetings_group` (`group_id`),
    INDEX `idx_meetings_task`  (`task_id`),
    CONSTRAINT `fk_meetings_group` FOREIGN KEY (`group_id`)
    REFERENCES `groups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_meetings_task` FOREIGN KEY (`task_id`)
    REFERENCES `tasks`(`id`)  ON DELETE CASCADE
    ) ENGINE=InnoDB CHARSET=utf8mb4;

-- 4. 创建 task_weeks 表：INITIALIZING 阶段每周目标
CREATE TABLE IF NOT EXISTS `task_weeks` (
                                            `id`      BIGINT NOT NULL AUTO_INCREMENT,
                                            `task_id` BIGINT NOT NULL COMMENT '所属 Task',
                                            `week_no` TINYINT NOT NULL COMMENT '周序号',
                                            `goal`    TEXT    NOT NULL COMMENT '该周目标描述',
                                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `updated_at` DATETIME NOT NULL
                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_task_week` (`task_id`,`week_no`),
    CONSTRAINT `fk_taskweeks_task` FOREIGN KEY (`task_id`)
    REFERENCES `tasks`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB CHARSET=utf8mb4;

-- 5. 创建 contributions 表：COMPLETED 阶段成员贡献度
CREATE TABLE IF NOT EXISTS `contributions` (
                                               `id`         BIGINT NOT NULL AUTO_INCREMENT,
                                               `task_id`    BIGINT NOT NULL COMMENT '所属 Task',
                                               `student_id` BIGINT NOT NULL COMMENT '学生（user_id）',
                                               `score`      DECIMAL(5,2) NOT NULL COMMENT '贡献度分数',
    `feedback`   TEXT    NULL COMMENT 'Agent/Teacher 评价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_contrib_task`    (`task_id`),
    INDEX `idx_contrib_student` (`student_id`),
    CONSTRAINT `fk_contrib_task` FOREIGN KEY (`task_id`)
    REFERENCES `tasks`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_contrib_student` FOREIGN KEY (`student_id`)
    REFERENCES `students`(`user_id`) ON DELETE CASCADE
    ) ENGINE=InnoDB CHARSET=utf8mb4;

-- 6. 创建 agent_conversations 表：持久化 Agent 对话上下文
CREATE TABLE IF NOT EXISTS `agent_conversations` (
                                                     `id`          BIGINT NOT NULL AUTO_INCREMENT,
                                                     `task_id`     BIGINT NOT NULL COMMENT '关联 Task',
                                                     `group_id`    BIGINT NOT NULL COMMENT '关联小组',
                                                     `sender`      ENUM('STUDENT','TEACHER','ADMIN','AGENT')
    NOT NULL COMMENT '消息发送者角色',
    `message`     TEXT    NOT NULL COMMENT '消息内容',
    `context`     JSON    NULL COMMENT '可选：LLM 上下文元数据',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_conv_task`  (`task_id`),
    INDEX `idx_conv_group` (`group_id`),
    CONSTRAINT `fk_conv_task`  FOREIGN KEY (`task_id`)
    REFERENCES `tasks`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_conv_group` FOREIGN KEY (`group_id`)
    REFERENCES `groups`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB CHARSET=utf8mb4;

-- 创建对话表 (修复版本)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    sender_id BIGINT,
    sender_type ENUM('USER', 'AGENT') NOT NULL DEFAULT 'USER',
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_task_group (task_id, group_id),
    INDEX idx_created_at (created_at)
); 
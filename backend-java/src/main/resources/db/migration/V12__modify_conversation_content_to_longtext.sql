-- 修改conversations表的content字段为LONGTEXT以支持更长的AI回复
ALTER TABLE conversations MODIFY COLUMN content LONGTEXT NOT NULL; 
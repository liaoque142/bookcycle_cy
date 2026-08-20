-- ============================================================
-- BookCycle 数据库升级脚本 2026-08-20b
-- 适用：已有旧库（schema.sql 建表早于本次改动），一次性执行
-- 幂等说明：MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报 Duplicate column，勿重复运行
-- 变更：
--   1. user_profile.genre_ids        （书籍喜好，多选分类逗号分隔）
--   2. message.content 改可空        （纯图私信 content 为 NULL，原 NOT NULL 会拒绝插入）
--   3. message.idx_receiver_id_id    （消息中心按 (receiver_id, id) 分页排序索引，可选）
-- 全新安装无需本脚本，schema.sql 已含这些列/约束。
-- ============================================================

USE `bookcycle`;

ALTER TABLE `user_profile`
  ADD COLUMN `genre_ids` VARCHAR(100) DEFAULT NULL COMMENT '书籍喜好分类ID，逗号分隔' AFTER `interest_tags`;

ALTER TABLE `message`
  MODIFY COLUMN `content` VARCHAR(500) DEFAULT NULL COMMENT '消息内容（纯图消息可为空）';

ALTER TABLE `message`
  ADD INDEX `idx_receiver_id_id` (`receiver_id`, `id`);

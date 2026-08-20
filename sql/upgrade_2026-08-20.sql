-- ============================================================
-- BookCycle 数据库升级脚本 2026-08-20
-- 适用：已有旧库（schema.sql 建表早于本次改动），一次性执行
-- 幂等说明：MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报 Duplicate column，勿重复运行
-- 变更：
--   1. blacklist.was_friend          （解除拉黑恢复好友）
--   2. message.image_url             （私信发图）
--   3. forum_comment.image_url       （论坛评论发图）
--   4. book_discussion.image_url     （书籍讨论发图）
-- 全新安装无需本脚本，schema.sql 已含这些列。
-- ============================================================

USE `bookcycle`;

ALTER TABLE `blacklist`
  ADD COLUMN `was_friend` TINYINT NOT NULL DEFAULT 0 COMMENT '拉黑时是否为好友：1 是（解除时恢复好友）/ 0 否' AFTER `black_user_id`;

ALTER TABLE `message`
  ADD COLUMN `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）' AFTER `content`;

ALTER TABLE `forum_comment`
  ADD COLUMN `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）' AFTER `content`;

ALTER TABLE `book_discussion`
  ADD COLUMN `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）' AFTER `content`;

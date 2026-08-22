-- ============================================================
-- BookCycle 数据库升级脚本 2026-08-22
-- 适用：已有旧库（schema.sql 建表早于本次改动），一次性执行
-- 幂等说明：MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报 Duplicate column，勿重复运行
-- 变更：
--   1. user_profile.pay_qr          （收款二维码 URL，买家下单后展示扫码转账）
-- 全新安装无需本脚本，schema.sql 已含该列。
-- ============================================================

USE `bookcycle`;

ALTER TABLE `user_profile`
  ADD COLUMN `pay_qr` VARCHAR(255) DEFAULT NULL COMMENT '收款二维码 URL' AFTER `genre_ids`;

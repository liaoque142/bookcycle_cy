-- ============================================================
-- BookCycle（书循环）初始数据脚本
-- 依赖：需先执行 schema.sql
-- 幂等：INSERT IGNORE + 显式 id，可重复执行
-- ============================================================

USE `bookcycle`;

-- 角色：admin（管理员）/ user（书友）
INSERT IGNORE INTO `role` (`id`, `role_name`, `role_code`, `description`) VALUES
  (1, '管理员', 'admin', '系统管理员，拥有管理后台全部权限'),
  (2, '书友', 'user', '注册用户，可发布/交易/发帖');

-- 分类：8 个根分类，对齐前端 data/books.ts categories 顺序
INSERT IGNORE INTO `category` (`id`, `parent_id`, `name`, `sort`, `status`) VALUES
  (1, 0, '文学小说', 1, 1),
  (2, 0, '教材教辅', 2, 1),
  (3, 0, '人文社科', 3, 1),
  (4, 0, '科学技术', 4, 1),
  (5, 0, '少儿绘本', 5, 1),
  (6, 0, '生活艺术', 6, 1),
  (7, 0, '杂志期刊', 7, 1),
  (8, 0, '其他', 8, 1);

-- 初始管理员：admin（默认密码 admin123456，首次登录后请修改；生产从环境变量注入）
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `email`, `credit`, `status`, `role_id`) VALUES
  (1, 'admin', '$2a$10$.56sbnLvTv5bywLohfLDCOgbuJ7hOcsiyBtQR4ZMNXsGUSBNhSUVi', 'admin@bookcycle.dev', 100, 1, 1);
INSERT IGNORE INTO `user_profile` (`id`, `user_id`, `nickname`) VALUES
  (1, 1, '管理员');

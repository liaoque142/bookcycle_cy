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

-- 论坛板块（阶段二）
INSERT IGNORE INTO `forum_board` (`id`, `name`, `description`, `sort`, `status`) VALUES
  (1, '读书心得', '读后感想、好书推荐、读书方法交流', 1, 1),
  (2, '书评', '对一本书的详细点评与深度解读', 2, 1),
  (3, '闲置交流', '二手书定价、流通、交易经验分享', 3, 1),
  (4, '求助问答', '找书、求推荐、问题求助', 4, 1);

-- 示例帖子（挂管理员，便于浏览器即见效果；点赞数 0 与 forum_like 保持一致）
INSERT IGNORE INTO `forum_post` (`id`, `board_id`, `user_id`, `title`, `content`, `like_count`, `view_count`) VALUES
  (1, 1, 1, '欢迎来到书循环论坛', '在这里分享读书心得、推荐好书，也可以交流二手书流通经验。让每一本书，都继续它的旅程。', 0, 0),
  (2, 3, 1, '二手教材怎么定价才不亏？', '卖教材怕挂高没人问、挂低心疼。聊聊常见定价思路：参考新书价 3-5 折、看内页笔记多少、学期初需求旺。', 0, 0);

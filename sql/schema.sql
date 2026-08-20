-- ============================================================
-- BookCycle（书循环）数据库初始化脚本
-- 适用：MySQL 8（本机 8.4.5）；字符集 utf8mb4；引擎 InnoDB
-- 幂等：CREATE TABLE IF NOT EXISTS，可重复执行
-- 约定：
--   * 无物理外键，用逻辑索引（配合逻辑删除、MyBatis 灵活组装）
--   * 枚举存 TINYINT 码 + 注释，展示中文由后端 VO 层映射
--   * 通用字段：id / create_time / update_time / deleted（逻辑删除）
--   * 纯追加表只保留 id + create_time
-- ============================================================

CREATE DATABASE IF NOT EXISTS `bookcycle`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `bookcycle`;

-- ============================================================
-- 一、用户体系
-- ============================================================

-- 1. 角色表（RBAC：用户与管理员统一 sys_user + 角色，无独立 admin 表）
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名，如 管理员 / 书友',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码：admin / user（唯一）',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- 2. 用户表（注册登录：手机号/邮箱 + 密码，JWT 认证）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 哈希）',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（唯一，登录凭证）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱（唯一，登录凭证）',
  `credit` INT NOT NULL DEFAULT 100 COMMENT '信誉分：初始 100，0-100',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1 正常 / 0 禁用',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID（关联 role.id）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表（RBAC 统一账号）';

-- 3. 用户详情表
CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（关联 sys_user.id）',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称（唯一）',
  `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  `interest_tags` VARCHAR(255) DEFAULT NULL COMMENT '兴趣标签，逗号分隔',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户详情表';

-- 4. 好友关系表
CREATE TABLE IF NOT EXISTS `friend` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `friend_id` BIGINT UNSIGNED NOT NULL COMMENT '好友用户ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '好友状态：1 正常 / 0 已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友关系表';

-- 5. 好友申请表
CREATE TABLE IF NOT EXISTS `friend_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from_user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID',
  `to_user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人用户ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '申请状态：0 待处理 / 1 同意 / 2 拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友申请表';

-- 6. 用户拉黑表（被拉黑后双方无法互动）
CREATE TABLE IF NOT EXISTS `blacklist` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '拉黑操作方用户ID',
  `black_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被拉黑用户ID',
  `was_friend` TINYINT NOT NULL DEFAULT 0 COMMENT '拉黑时是否为好友：1 是（解除时恢复好友）/ 0 否',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_black` (`user_id`, `black_user_id`),
  KEY `idx_black_user_id` (`black_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户拉黑表';

-- 7. 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：1 是 / 0 否',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- ============================================================
-- 二、书籍与分类
-- ============================================================

-- 8. 书籍分类表（支持多级，parent_id=0 为根）
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类ID，0 为根分类',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '分类状态：1 启用 / 0 禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍分类表';

-- 9. 书籍表（卖家发布，Book VO 由后端关联返回 categoryName / sellerName）
CREATE TABLE IF NOT EXISTS `book` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(100) NOT NULL COMMENT '书名',
  `author` VARCHAR(50) NOT NULL COMMENT '作者',
  `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN',
  `publisher` VARCHAR(50) DEFAULT NULL COMMENT '出版社',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID（关联 category.id）',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID（关联 sys_user.id）',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '售价（交换/赠送为 NULL）',
  `trade_type` TINYINT NOT NULL DEFAULT 1 COMMENT '交易方式：1 出售 / 2 赠送 / 3 交换',
  `condition` TINYINT NOT NULL DEFAULT 1 COMMENT '新旧程度：1 全新 / 2 九成新 / 3 八成新 / 4 七成新及以下',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '书籍描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '书籍状态：1 在售 / 2 已预订 / 3 已售出 / 4 已下架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_trade_type` (`trade_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍表';

-- 10. 书籍图片表
CREATE TABLE IF NOT EXISTS `book_image` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '书籍ID（关联 book.id）',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片 URL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍图片表';

-- 11. 收藏表（取消收藏即删除行，故不做逻辑删除）
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '书籍ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book` (`user_id`, `book_id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

-- 12. 书籍讨论表（书籍详情页留言 / 回复，parent_id 指向父评论）
CREATE TABLE IF NOT EXISTS `book_discussion` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '书籍ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）',
  `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父级评论ID，NULL 为顶级',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书籍讨论表';

-- ============================================================
-- 三、论坛
-- ============================================================

-- 13. 论坛板块表
CREATE TABLE IF NOT EXISTS `forum_board` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '板块名',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '板块描述',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '板块状态：1 启用 / 0 禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛板块表';

-- 14. 论坛帖子表
CREATE TABLE IF NOT EXISTS `forum_post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `board_id` BIGINT UNSIGNED NOT NULL COMMENT '板块ID（关联 forum_board.id）',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发帖用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '帖子内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余，以 forum_like 为准）',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_board_id` (`board_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛帖子表';

-- 15. 帖子评论表
CREATE TABLE IF NOT EXISTS `forum_comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID（关联 forum_post.id）',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子评论表';

-- 16. 帖子点赞表（取消点赞即删除行，故不做逻辑删除）
CREATE TABLE IF NOT EXISTS `forum_like` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞表';

-- ============================================================
-- 四、交易
-- ============================================================

-- 17. 交易订单表（出售/赠送 一期；交换二期）
CREATE TABLE IF NOT EXISTS `trade_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一）',
  `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家用户ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '书籍ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额（赠送为 0）',
  `trade_type` TINYINT NOT NULL DEFAULT 1 COMMENT '交易方式：1 出售 / 2 赠送 / 3 交换',
  `ship_type` TINYINT NOT NULL DEFAULT 1 COMMENT '物流方式：1 邮寄 / 2 自提',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1 待支付 / 2 待发货 / 3 待收货 / 4 已完成 / 5 已取消 / 6 售后中',
  `address_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '收货地址ID（关联 address.id，自提为 NULL）',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `after_sale` TINYINT NOT NULL DEFAULT 0 COMMENT '售后状态：0 无 / 1 申请中 / 2 处理中 / 3 已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易订单表';

-- 18. 支付表（一期模拟支付，预留支付宝/微信）
CREATE TABLE IF NOT EXISTS `payment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID（关联 trade_order.id）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `pay_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式：1 模拟余额 / 2 支付宝 / 3 微信',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0 未支付 / 1 已支付 / 2 已退款',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付表';

-- 19. 退款/售后表
CREATE TABLE IF NOT EXISTS `refund` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID（关联 trade_order.id）',
  `applicant_id` BIGINT UNSIGNED NOT NULL COMMENT '申请方用户ID（买家或卖家）',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '售后类型：1 仅退款 / 2 退货退款',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '申请原因',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '处理状态：1 待处理 / 2 同意 / 3 拒绝 / 4 已完成',
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人（管理员）用户ID',
  `handle_result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款/售后表';

-- 20. 交易评价表（交易完成后互评，评分 1-5 影响信誉分）
CREATE TABLE IF NOT EXISTS `trade_evaluation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID（关联 trade_order.id）',
  `evaluator_id` BIGINT UNSIGNED NOT NULL COMMENT '评价方用户ID',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '被评价方用户ID',
  `score` TINYINT UNSIGNED NOT NULL COMMENT '评分 1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_evaluator` (`order_id`, `evaluator_id`),
  KEY `idx_target_id` (`target_id`),
  CONSTRAINT `chk_score` CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易评价表';

-- 20.1 交换单表（以书换书：买家以书 B 换卖家书 A，需对方同意 + 双向发货 + 双向确认收货；SWAP 非保留字，统一反引号）
CREATE TABLE IF NOT EXISTS `swap` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `swap_no` VARCHAR(32) NOT NULL COMMENT '交换单号（唯一）',
  `initiator_id` BIGINT UNSIGNED NOT NULL COMMENT '发起人（买家）用户ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收方（卖家）用户ID',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '被交换书籍ID（卖家书 A）',
  `offer_book_id` BIGINT UNSIGNED NOT NULL COMMENT '发起人提供的书籍ID（买家书 B）',
  `message` VARCHAR(255) DEFAULT NULL COMMENT '发起人附言',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 待对方同意 / 2 已同意待发货 / 3 交换中 / 4 已完成 / 5 已取消 / 6 已拒绝',
  `initiator_ship_type` TINYINT DEFAULT NULL COMMENT '发起人物流方式：1 邮寄 / 2 自提',
  `initiator_address_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发起人收货地址ID（收书 A）',
  `receiver_ship_type` TINYINT DEFAULT NULL COMMENT '接收方物流方式：1 邮寄 / 2 自提',
  `receiver_address_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '接收方收货地址ID（收书 B）',
  `initiator_shipped_at` DATETIME DEFAULT NULL COMMENT '发起人发货时间',
  `receiver_shipped_at` DATETIME DEFAULT NULL COMMENT '接收方发货时间',
  `initiator_confirmed_at` DATETIME DEFAULT NULL COMMENT '发起人确认收货时间',
  `receiver_confirmed_at` DATETIME DEFAULT NULL COMMENT '接收方确认收货时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_swap_no` (`swap_no`),
  KEY `idx_initiator_id` (`initiator_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_offer_book_id` (`offer_book_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交换单表';

-- 20.2 好友私信表（好友间一对一私信，仅双方可见；纯追加，未读标记）
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送人用户ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片 URL（可选，与文字可并存）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：1 已读 / 0 未读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`, `id`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友私信表';

-- ============================================================
-- 五、信誉与通知
-- ============================================================

-- 21. 信誉变动日志表（可正负，管理员调整需记录操作人）
CREATE TABLE IF NOT EXISTS `credit_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `change_value` INT NOT NULL COMMENT '信誉变动值（正加负扣）',
  `reason` VARCHAR(255) NOT NULL COMMENT '变动原因',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人用户ID（管理员/系统触发为 NULL）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信誉变动日志表';

-- 22. 信誉规则表（数据驱动，可后台配置）
CREATE TABLE IF NOT EXISTS `credit_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_code` VARCHAR(50) NOT NULL COMMENT '规则标识（唯一），如 TRADE_SUCCESS',
  `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `trigger_condition` VARCHAR(255) NOT NULL COMMENT '触发条件描述',
  `change_value` INT NOT NULL COMMENT '分值（正加负扣），如 +2 / -5',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，越小越先匹配',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '生效状态：1 启用 / 0 禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信誉规则表';

-- 23. 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型：1 系统 / 2 交易 / 3 好友',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：1 已读 / 0 未读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

-- ============================================================
-- 六、管理与日志
-- ============================================================

-- 24. 举报表（举报人 → 被举报对象，管理员处理）
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id` BIGINT UNSIGNED NOT NULL COMMENT '举报人用户ID',
  `target_type` TINYINT NOT NULL COMMENT '被举报对象类型：1 用户 / 2 书籍 / 3 帖子 / 4 评论',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '被举报对象ID',
  `reason` VARCHAR(255) NOT NULL COMMENT '举报原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '处理状态：1 待处理 / 2 已处理 / 3 已驳回',
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人（管理员）用户ID',
  `handle_result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='举报表';

-- 25. 操作日志表（记录管理员关键操作，纯追加）
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人用户ID（管理员）',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型，如 UPDATE_USER / DOWN_BOOK',
  `detail` VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

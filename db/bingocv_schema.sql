-- BingoCV database schema
-- MySQL 8.x, charset utf8mb4

CREATE DATABASE IF NOT EXISTS `bingoCV`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `bingoCV`;

CREATE TABLE IF NOT EXISTS `bingo_user` (
  `userid` BIGINT NOT NULL COMMENT '用户唯一ID',
  `username` VARCHAR(100) NOT NULL COMMENT '登录用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '用户头像地址',
  `role` VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN, USER',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态: 0正常, 1禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
  PRIMARY KEY (`userid`),
  UNIQUE KEY `uk_bingo_user_username` (`username`),
  KEY `idx_bingo_user_status` (`status`),
  KEY `idx_bingo_user_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录账户表';

CREATE TABLE IF NOT EXISTS `bingo_profiles` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '关联用户ID',
  `name` VARCHAR(80) DEFAULT NULL COMMENT '姓名',
  `photo` MEDIUMTEXT DEFAULT NULL COMMENT '免冠照/头像地址',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `city` VARCHAR(120) DEFAULT NULL COMMENT '城市',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `email` VARCHAR(120) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(60) DEFAULT NULL COMMENT '电话或手机号',
  `weixin` VARCHAR(80) DEFAULT NULL COMMENT '微信号',
  `qq` VARCHAR(80) DEFAULT NULL COMMENT 'QQ号',
  `weibo` VARCHAR(255) DEFAULT NULL COMMENT '微博地址',
  `sex` VARCHAR(20) DEFAULT NULL COMMENT '性别',
  `description` TEXT DEFAULT NULL COMMENT '个人简介',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_profiles_userid` (`userid`),
  KEY `idx_bingo_profiles_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人简介及基本信息表';

CREATE TABLE IF NOT EXISTS `bingo_edu` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `start` VARCHAR(20) DEFAULT NULL COMMENT '开始时间',
  `end` VARCHAR(20) DEFAULT NULL COMMENT '结束时间',
  `school` VARCHAR(160) DEFAULT NULL COMMENT '学校',
  `study` VARCHAR(160) DEFAULT NULL COMMENT '专业名称',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_edu_user_priority` (`userid`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育经历表';

CREATE TABLE IF NOT EXISTS `bingo_work` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `start` VARCHAR(20) DEFAULT NULL COMMENT '开始时间',
  `end` VARCHAR(20) DEFAULT NULL COMMENT '结束时间',
  `company` VARCHAR(160) DEFAULT NULL COMMENT '公司',
  `job` VARCHAR(160) DEFAULT NULL COMMENT '岗位名称',
  `description` TEXT DEFAULT NULL COMMENT '工作描述',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_work_user_priority` (`userid`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作经历表';

CREATE TABLE IF NOT EXISTS `bingo_skill` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `keywords` TEXT DEFAULT NULL COMMENT '技能关键词，空格分隔',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_skill_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能表';

CREATE TABLE IF NOT EXISTS `bingo_specialty` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(120) DEFAULT NULL COMMENT '特长名称',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_specialty_user_priority` (`userid`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户特长表';

CREATE TABLE IF NOT EXISTS `bingo_template` (
  `id` BIGINT NOT NULL COMMENT '模板ID',
  `name` VARCHAR(120) NOT NULL COMMENT '模板名称',
  `industry` VARCHAR(60) NOT NULL COMMENT '行业分类',
  `cover_url` VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  `template_key` VARCHAR(80) NOT NULL COMMENT '模板前端渲染标识',
  `template_type` VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT 'FREE免费, POINTS积分, PAID付费',
  `points_cost` INT NOT NULL DEFAULT 0 COMMENT '所需积分',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '付费价格',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0下架, 1上架',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_template_key` (`template_key`),
  KEY `idx_bingo_template_market` (`industry`, `template_type`, `status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历模板表';

CREATE TABLE IF NOT EXISTS `bingo_user_template` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `template_id` BIGINT NOT NULL COMMENT '模板ID',
  `source` VARCHAR(30) NOT NULL DEFAULT 'FREE' COMMENT '来源: FREE, POINTS, PAID, ADMIN',
  `active` TINYINT NOT NULL DEFAULT 0 COMMENT '是否当前使用: 0否, 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_user_template` (`userid`, `template_id`),
  KEY `idx_bingo_user_template_active` (`userid`, `active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户拥有的模板表';

CREATE TABLE IF NOT EXISTS `bingo_points` (
  `id` BIGINT NOT NULL COMMENT '积分账户ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `balance` INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  `total_earned` INT NOT NULL DEFAULT 0 COMMENT '累计获得',
  `total_spent` INT NOT NULL DEFAULT 0 COMMENT '累计消费',
  `expire_time` DATETIME DEFAULT NULL COMMENT '积分有效期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_points_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户表';

CREATE TABLE IF NOT EXISTS `bingo_points_log` (
  `id` BIGINT NOT NULL COMMENT '流水ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `biz_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `amount` INT NOT NULL COMMENT '变动积分，正数获得，负数消费',
  `balance` INT NOT NULL COMMENT '变动后余额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_points_log_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_points_log_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

CREATE TABLE IF NOT EXISTS `bingo_task` (
  `id` BIGINT NOT NULL COMMENT '任务ID',
  `task_key` VARCHAR(80) NOT NULL COMMENT '任务唯一标识',
  `name` VARCHAR(120) NOT NULL COMMENT '任务名称',
  `task_type` VARCHAR(30) NOT NULL COMMENT 'NEWBIE, DAILY, ACHIEVEMENT',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '任务说明',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_task_key` (`task_key`),
  KEY `idx_bingo_task_type` (`task_type`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务配置表';

CREATE TABLE IF NOT EXISTS `bingo_user_task` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '进度',
  `completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成',
  `rewarded` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发放奖励',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_user_task` (`userid`, `task_id`),
  KEY `idx_bingo_user_task_completed` (`userid`, `completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户任务进度表';

CREATE TABLE IF NOT EXISTS `bingo_sign_in` (
  `id` BIGINT NOT NULL COMMENT '签到ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `sign_date` DATE NOT NULL COMMENT '签到日期',
  `streak_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_sign_in_user_date` (`userid`, `sign_date`),
  KEY `idx_bingo_sign_in_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

CREATE TABLE IF NOT EXISTS `bingo_share` (
  `id` BIGINT NOT NULL COMMENT '分享ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `share_type` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT 'PUBLIC公开, PRIVATE私密',
  `title` VARCHAR(160) DEFAULT NULL COMMENT '分享标题',
  `password` VARCHAR(32) DEFAULT NULL COMMENT '访问验证码/密码',
  `access_limit` INT DEFAULT NULL COMMENT '访问人数上限',
  `access_count` INT NOT NULL DEFAULT 0 COMMENT '已访问次数',
  `expire_time` DATETIME DEFAULT NULL COMMENT '失效时间',
  `short_code` VARCHAR(32) NOT NULL COMMENT '短码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0关闭, 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_share_short_code` (`short_code`),
  KEY `idx_bingo_share_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_share_status` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

CREATE TABLE IF NOT EXISTS `bingo_share_access` (
  `id` BIGINT NOT NULL COMMENT '访问记录ID',
  `share_id` BIGINT NOT NULL COMMENT '分享ID',
  `visitor_key` VARCHAR(128) DEFAULT NULL COMMENT '访客标识',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP',
  `region` VARCHAR(120) DEFAULT NULL COMMENT '地域',
  `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_share_access_share_time` (`share_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享访问记录表';

CREATE TABLE IF NOT EXISTS `bingo_short_url` (
  `id` BIGINT NOT NULL COMMENT '短链ID',
  `short_code` VARCHAR(32) NOT NULL COMMENT '短码',
  `target_url` VARCHAR(1024) NOT NULL COMMENT '目标URL',
  `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
  `access_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
  `expire_time` DATETIME DEFAULT NULL COMMENT '失效时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0关闭, 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_short_url_code` (`short_code`),
  KEY `idx_bingo_short_url_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接映射表';

CREATE TABLE IF NOT EXISTS `bingo_system_config` (
  `id` BIGINT NOT NULL COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '说明',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_system_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `bingo_operation_log` (
  `id` BIGINT NOT NULL COMMENT '日志ID',
  `userid` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(80) DEFAULT NULL COMMENT '用户名',
  `module` VARCHAR(120) DEFAULT NULL COMMENT '模块',
  `action` VARCHAR(120) DEFAULT NULL COMMENT '动作',
  `request_method` VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
  `request_uri` VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1成功，0失败',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `cost_ms` BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_operation_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_operation_uri_time` (`request_uri`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS `bingo_pay_order` (
  `id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `order_type` VARCHAR(30) NOT NULL COMMENT 'POINTS积分充值, TEMPLATE模板购买',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分数量',
  `pay_channel` VARCHAR(30) DEFAULT NULL COMMENT '支付渠道',
  `pay_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PAID, CLOSED, REFUNDED',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_pay_order_no` (`order_no`),
  KEY `idx_bingo_pay_order_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_pay_order_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

INSERT INTO `bingo_template`
  (`id`, `name`, `industry`, `cover_url`, `template_key`, `template_type`, `points_cost`, `price`, `description`, `status`, `sort_order`)
VALUES
  (900001, '工程师清晰版', 'IT互联网', NULL, 'engineer-clean', 'FREE', 0, 0.00, '突出项目、技术栈和交付结果', 1, 100),
  (900002, '产品增长版', 'IT互联网', NULL, 'product-growth', 'POINTS', 120, 0.00, '适合产品、运营、增长岗位', 1, 90),
  (900003, '财务稳健版', '金融财会', NULL, 'finance-stable', 'POINTS', 160, 0.00, '强调证书、经验和风控意识', 1, 80),
  (900004, '讲师履历版', '教育培训', NULL, 'teacher-classic', 'FREE', 0, 0.00, '课程、成果和教学风格更醒目', 1, 70),
  (900005, '医护专业版', '医疗健康', NULL, 'medical-professional', 'POINTS', 180, 0.00, '突出资质、科室和实践经历', 1, 60),
  (900006, '创意作品版', '设计创意', NULL, 'designer-portfolio', 'PAID', 0, 9.90, '适合展示作品集和视觉能力', 1, 50),
  (900007, '通用简约版', '通用简约', NULL, 'general-minimal', 'FREE', 0, 0.00, '干净稳妥，适配多数岗位', 1, 40)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `industry` = VALUES(`industry`),
  `template_type` = VALUES(`template_type`),
  `points_cost` = VALUES(`points_cost`),
  `price` = VALUES(`price`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`);

INSERT INTO `bingo_task`
  (`id`, `task_key`, `name`, `task_type`, `reward_points`, `description`, `enabled`)
VALUES
  (910001, 'complete_profile', '完善个人简历', 'NEWBIE', 20, '填写姓名、联系方式和个人简介', 1),
  (910002, 'add_first_edu', '添加教育经历', 'NEWBIE', 15, '至少添加一条教育经历', 1),
  (910003, 'add_first_work', '添加工作经历', 'NEWBIE', 15, '至少添加一条工作或项目经历', 1),
  (910004, 'select_template', '选择简历模板', 'NEWBIE', 10, '从模板市场启用一套模板', 1),
  (910005, 'daily_login', '每日登录', 'DAILY', 5, '每日登录奖励', 1),
  (910006, 'resume_full_score', '简历完整度100%', 'ACHIEVEMENT', 50, '简历资料完整度达到100%', 1),
  (910007, 'share_resume', '创建简历分享', 'NEWBIE', 10, '创建一个公开或私密简历分享链接', 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `task_type` = VALUES(`task_type`),
  `reward_points` = VALUES(`reward_points`),
  `description` = VALUES(`description`),
  `enabled` = VALUES(`enabled`);

INSERT INTO `bingo_system_config`
  (`id`, `config_key`, `config_value`, `description`, `enabled`)
VALUES
  (920001, 'share.access.limit.options', '3,5,8', '分享访问人数可选档位', 1),
  (920002, 'share.verify.code.length', '4', '私密分享验证码长度', 1),
  (920003, 'points.sign.rewards', '5,8,12,16,20,25,30', '连续签到7天奖励配置', 1),
  (920004, 'points.template.free.first', 'true', '新用户默认拥有免费模板', 1),
  (920005, 'rate.limit.enabled', 'true', '是否启用接口限流', 1),
  (920006, 'rate.limit.max.requests', '120', '单个IP在窗口期内允许的最大请求数', 1),
  (920007, 'rate.limit.window.seconds', '60', '接口限流窗口期秒数', 1)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `description` = VALUES(`description`),
  `enabled` = VALUES(`enabled`);

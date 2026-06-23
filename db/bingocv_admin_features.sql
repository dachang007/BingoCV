-- BingoCV 后台管理能力补充脚本
-- 包含：系统配置、操作日志、接口限流默认配置、分享任务奖励、AI 配置。
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

CREATE TABLE IF NOT EXISTS `bingo_ai_usage_log` (
  `id` BIGINT NOT NULL COMMENT '记录ID',
  `userid` BIGINT DEFAULT NULL COMMENT '用户ID',
  `action_type` VARCHAR(40) NOT NULL COMMENT '动作类型：ANALYZE、POLISH',
  `provider` VARCHAR(80) DEFAULT NULL COMMENT 'AI提供方',
  `model` VARCHAR(120) DEFAULT NULL COMMENT '模型名称',
  `success` TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用外部模型成功',
  `fallback_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用本地兜底',
  `cost_ms` BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_ai_usage_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_ai_usage_action_time` (`action_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用记录表';

INSERT INTO `bingo_task`
  (`id`, `task_key`, `name`, `task_type`, `reward_points`, `description`, `enabled`)
VALUES
  (910007, 'share_resume', '创建简历分享', 'NEWBIE', 10, '创建一个公开或私密简历分享链接', 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `task_type` = VALUES(`task_type`),
  `reward_points` = VALUES(`reward_points`),
  `description` = VALUES(`description`),
  `enabled` = VALUES(`enabled`);

-- 支付通道默认配置：模拟支付默认开启，真实支付通道需要配置密钥后再启用。
INSERT INTO `bingo_system_config`
  (`id`, `config_key`, `config_value`, `description`, `enabled`)
VALUES
  (920101, 'payment.mock.enabled', 'true', '是否启用模拟支付通道', 1),
  (920102, 'payment.alipay.enabled', 'false', '是否启用支付宝支付通道', 1),
  (920103, 'payment.wechat.enabled', 'false', '是否启用微信支付通道', 1),
  (920104, 'payment.callback.secret', '', '支付回调签名密钥，占位配置', 1)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
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
  (920007, 'rate.limit.window.seconds', '60', '接口限流窗口期秒数', 1),
  (920008, 'ai.provider', 'local', 'AI 提供方：local 或 openai-compatible', 1),
  (920009, 'ai.enabled', 'false', '是否启用外部 AI 模型；关闭时使用本地规则分析', 1),
  (920010, 'ai.base.url', 'https://api.openai.com/v1', 'OpenAI 兼容接口地址', 1),
  (920011, 'ai.api.key', '', '外部 AI API Key，留空时自动回退本地规则分析', 1),
  (920012, 'ai.model', 'gpt-4.1-mini', '外部 AI 模型名称', 1),
  (920013, 'ai.timeout.seconds', '20', '外部 AI 请求超时时间', 1)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `description` = VALUES(`description`),
  `enabled` = VALUES(`enabled`);

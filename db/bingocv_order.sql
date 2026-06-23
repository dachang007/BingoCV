-- BingoCV 支付订单功能补充脚本
-- 新环境可直接执行；旧环境如果已有表，请按字段列表手动补齐缺失列。
CREATE TABLE IF NOT EXISTS `bingo_pay_order` (
  `id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `order_type` VARCHAR(30) NOT NULL COMMENT '订单类型：POINTS积分充值，TEMPLATE模板购买',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原始金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分数量',
  `coupon_id` BIGINT DEFAULT NULL COMMENT '优惠券ID',
  `pay_channel` VARCHAR(30) DEFAULT NULL COMMENT '支付渠道：MOCK、ALIPAY、WECHAT',
  `pay_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING、PAID、CLOSED、REFUNDED',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `admin_remark` VARCHAR(1000) DEFAULT NULL COMMENT '后台备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_pay_order_no` (`order_no`),
  KEY `idx_bingo_pay_order_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_pay_order_status` (`pay_status`),
  KEY `idx_bingo_pay_order_channel` (`pay_channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

CREATE TABLE IF NOT EXISTS `bingo_pay_refund` (
  `id` BIGINT NOT NULL COMMENT '退款ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS' COMMENT '退款状态：PENDING、SUCCESS、FAILED',
  `refund_no` VARCHAR(80) DEFAULT NULL COMMENT '退款流水号',
  `reason` VARCHAR(300) DEFAULT NULL COMMENT '退款原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作管理员ID',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_pay_refund_order` (`order_id`),
  KEY `idx_bingo_pay_refund_user_time` (`userid`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付退款记录表';

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

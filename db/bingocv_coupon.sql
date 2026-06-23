-- BingoCV 优惠券功能补充脚本
CREATE TABLE IF NOT EXISTS `bingo_coupon` (
  `id` BIGINT NOT NULL COMMENT '优惠券ID',
  `coupon_code` VARCHAR(40) NOT NULL COMMENT '优惠券码',
  `name` VARCHAR(120) NOT NULL COMMENT '优惠券名称',
  `coupon_type` VARCHAR(20) NOT NULL COMMENT '优惠券类型：AMOUNT固定金额，RATE折扣比例',
  `discount_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额',
  `discount_rate` DECIMAL(5,4) DEFAULT NULL COMMENT '折扣比例，如0.8000表示八折',
  `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低使用金额',
  `target_userid` BIGINT DEFAULT NULL COMMENT '指定用户ID，空表示可被任意用户领取',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED待使用，USED已使用，DISABLED已禁用',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `used_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bingo_coupon_code` (`coupon_code`),
  KEY `idx_bingo_coupon_user_status` (`target_userid`, `status`),
  KEY `idx_bingo_coupon_status_time` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

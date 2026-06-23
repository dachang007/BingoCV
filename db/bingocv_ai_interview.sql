-- AI 面试题练习记录表
CREATE TABLE IF NOT EXISTS `bingo_ai_interview_question` (
  `id` BIGINT NOT NULL COMMENT '题目ID',
  `userid` BIGINT NOT NULL COMMENT '用户ID',
  `job_title` VARCHAR(160) DEFAULT NULL COMMENT '目标岗位',
  `category` VARCHAR(60) DEFAULT NULL COMMENT '题目分类',
  `difficulty` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '难度: EASY, MEDIUM, HARD',
  `question` TEXT NOT NULL COMMENT '面试题',
  `answer_hint` TEXT DEFAULT NULL COMMENT '回答要点',
  `source` VARCHAR(30) NOT NULL DEFAULT 'LOCAL' COMMENT '来源: LOCAL, AI',
  `mastery_status` VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT '掌握状态: TODO, PRACTICING, MASTERED',
  `practice_note` TEXT DEFAULT NULL COMMENT '练习备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bingo_ai_interview_user_time` (`userid`, `create_time`),
  KEY `idx_bingo_ai_interview_status` (`userid`, `mastery_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 面试题练习记录表';

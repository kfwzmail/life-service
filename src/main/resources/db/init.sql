-- ==================== 个人记账系统 - 建表脚本 ====================
-- 在 aimarket 库中执行

CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone`         VARCHAR(20)     NOT NULL COMMENT '手机号',
    `password`      VARCHAR(200)    NOT NULL COMMENT 'BCrypt密码',
    `nickname`      VARCHAR(50)     DEFAULT NULL COMMENT '昵称',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `category` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`          VARCHAR(50)     NOT NULL COMMENT '分类名称',
    `type`          VARCHAR(10)     NOT NULL COMMENT '类型: EXPENSE/INCOME',
    `icon`          VARCHAR(50)     DEFAULT '📦' COMMENT '图标emoji',
    `sort_order`    INT             DEFAULT 0 COMMENT '排序',
    `is_default`    TINYINT         NOT NULL DEFAULT 0 COMMENT '是否系统预设: 1-是 0-否',
    `user_id`       BIGINT          DEFAULT NULL COMMENT '所属用户ID(预设为NULL)',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE IF NOT EXISTS `bill` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '账单ID',
    `user_id`       BIGINT          NOT NULL COMMENT '用户ID',
    `category_id`   BIGINT          NOT NULL COMMENT '分类ID',
    `type`          VARCHAR(10)     NOT NULL COMMENT '类型: EXPENSE/INCOME',
    `amount`        DECIMAL(12,2)   NOT NULL COMMENT '金额',
    `remark`        VARCHAR(200)    DEFAULT '' COMMENT '备注',
    `bill_time`     DATETIME        NOT NULL COMMENT '记账时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_bill_time` (`user_id`, `bill_time`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

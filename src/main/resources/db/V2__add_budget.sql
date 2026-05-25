CREATE TABLE IF NOT EXISTS budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT DEFAULT NULL COMMENT '分类ID，NULL表示总预算',
    budget_month VARCHAR(7) NOT NULL COMMENT '预算月份，格式YYYY-MM',
    amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_month_category (user_id, budget_month, category_id),
    INDEX idx_user_month (user_id, budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

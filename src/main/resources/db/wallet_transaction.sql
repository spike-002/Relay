-- 钱包流水表
-- 每一笔余额变动都落一行，用于对账、纠纷赔付、退款追溯。
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transaction_no  VARCHAR(64)  NOT NULL COMMENT '流水单号',
    user_id         BIGINT       NOT NULL COMMENT '所属用户ID',
    type            TINYINT      NOT NULL COMMENT '类型 1-接单入账 2-提现 3-退款',
    status          TINYINT      NOT NULL COMMENT '状态 1-成功 2-待打款 3-已打款 4-已退回',
    amount          DECIMAL(10,2) NOT NULL COMMENT '变动金额(正增负减)',
    balance_after   DECIMAL(10,2) NOT NULL COMMENT '变动后余额快照',
    related_order_id BIGINT      DEFAULT NULL COMMENT '关联订单ID',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_related_order (related_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

package com.spike.relay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水表实体。
 * 每一笔余额变动都落一行，用于对账、纠纷赔付、退款追溯。绝不允许只改 user.balance 而不记流水。
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流水单号 */
    private String transactionNo;

    /** 所属用户ID */
    private Long userId;

    /** 流水类型 1-接单入账 2-提现 3-退款 */
    private Integer type;

    /** 流水状态 1-成功 2-待打款 3-已打款 4-已退回 */
    private Integer status;

    /** 变动金额(正数表示增加，负数表示减少) */
    private BigDecimal amount;

    /** 变动后余额(快照，便于对账) */
    private BigDecimal balanceAfter;

    /** 关联订单ID(入账时记录来源订单) */
    private Long relatedOrderId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}

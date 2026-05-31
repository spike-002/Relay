package com.spike.relay.enums;

import lombok.Getter;

/**
 * 钱包流水状态
 * 注意：v1.0 mock 支付下，平台无真实资金，提现仅生成"待打款"申请，由运营线下手动转账后改状态。
 */
@Getter
public enum TransactionStatusEnum {

    /** 入账即终态 */
    SUCCESS(1, "成功"),
    /** 提现申请已提交、余额已冻结，等待运营线下打款 */
    WITHDRAWING(2, "待打款"),
    /** 线下打款完成 */
    DONE(3, "已打款"),
    /** 打款失败，余额已退回 */
    FAILED(4, "已退回");

    private final int code;
    private final String description;

    TransactionStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}

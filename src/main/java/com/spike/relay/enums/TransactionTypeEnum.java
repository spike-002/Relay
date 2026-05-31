package com.spike.relay.enums;

import lombok.Getter;

/**
 * 钱包流水类型
 */
@Getter
public enum TransactionTypeEnum {

    INCOME(1, "接单入账"),
    WITHDRAW(2, "提现"),
    REFUND(3, "退款");

    private final int code;
    private final String description;

    TransactionTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}

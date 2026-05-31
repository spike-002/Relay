package com.spike.relay.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    CANCELLED(-1, "已取消"),
    PENDING(0, "待接单"),
    ACCEPTED(1, "待取货"),
    DELIVERING(2, "配送中"),
    DELIVERED(3, "待确认"),
    COMPLETED(4, "已完成"),
    REFUNDING(5, "退款处理中"),
    APPEALING(6, "申诉中");

    private final int code;
    private final String description;

    OrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatusEnum getByCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

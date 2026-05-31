package com.spike.relay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布订单请求参数
 */
@Data
public class OrderPublishDTO {

    /** 发单人用户ID */
    private Long publisherId;

    /** 取件外卖柜(如 "菜鸟驿站-A区") */
    private String pickupCabinet;

    /** 外卖柜格口号(如 "A-12") */
    private String cabinetSlot;

    /** 目标宿舍楼栋号 */
    private String targetBuilding;

    /** 详细寝室地址 */
    private String targetAddress;

    /** 外卖大小类型 0-小件 1-大件 */
    private Integer packageSize;

    /** 跑腿费(元) */
    private BigDecimal fee;

    /** 小费(元) 固定选项: 0/1/2/5 */
    private BigDecimal tip;

    /** 期望送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime expectedTime;

    /** 包裹预计到柜时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime expectedArrivalTime;

    /** 发单人联系手机号 */
    private String phone;

    /** 订单备注 */
    private String remark;
}

package com.spike.relay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 跑腿订单表实体
 */
@Data
@TableName("delivery_order")
public class DeliveryOrder {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号(业务唯一号) */
    private String orderNo;

    /** 发单人用户ID */
    private Long publisherId;

    /** 接单人用户ID */
    private Long takerId;

    /** 取件外卖柜名称 */
    private String cabinetName;

    /** 外卖柜格口号 */
    private String cabinetSlot;

    /** 目标宿舍楼栋号 */
    private String targetBuilding;

    /** 详细寝室门牌号 */
    private String targetAddress;

    /** 外卖大小类型 0-小件 1-大件 */
    private Integer itemType;

    /** 期望送达时间 */
    private LocalDateTime expectedTime;

    /** 跑腿费(元) */
    private BigDecimal fee;

    /** 小费(元) 固定选项: 0/1/2/5 */
    private BigDecimal tip;

    /** 包裹预计到柜时间(发单人填写，供跑腿员规划) */
    private LocalDateTime expectedArrivalTime;

    /** 订单备注 */
    private String remark;

    /** 订单状态 -1-已取消 0-待接单 1-已接单 2-已取件 3-已送达 4-已确认收货 5-退款中 6-申诉中 */
    private Integer status;

    /** 取件留痕照片路径 */
    private String pickupPhotoUrl;

    /** 送达留痕照片路径 */
    private String deliveryPhotoUrl;

    /** 接单时间 */
    private LocalDateTime takenTime;

    /** 取件打卡时间 */
    private LocalDateTime pickedTime;

    /** 送达打卡时间 */
    private LocalDateTime deliveredTime;

    /** 确认收货时间 */
    private LocalDateTime confirmedTime;

    /** 发单人联系手机号 */
    private String phone;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}

package com.spike.relay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@TableName("user")
public class User {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信OpenID */
    private String openId;

    /** 微信昵称 */
    private String nickname;

    /** 微信头像URL */
    private String avatarUrl;

    /** 手机号 */
    private String phone;

    /** 默认宿舍楼栋号 */
    private String defaultBuilding;

    /** 详细宿舍门牌号 */
    private String defaultRoom;

    /** 学生认证状态 0-未认证 1-审核中 2-已认证 3-认证失败 */
    private Integer authStatus;

    /** 学生认证照片路径 */
    private String authPhotoUrl;

    /** 钱包余额(元) */
    private BigDecimal balance;

    /** 累计发单数 */
    private Integer totalPublishCount;

    /** 累计接单数 */
    private Integer totalTakeCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}

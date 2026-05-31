package com.spike.relay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spike.relay.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 原子增加余额(入账)。返回受影响行数 */
    @Update("UPDATE user SET balance = balance + #{amount}, update_time = NOW() WHERE id = #{userId}")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 原子扣减余额(提现冻结/退款)。
     * 条件 balance >= amount 防止余额变负与并发超提；返回 0 表示余额不足。
     */
    @Update("UPDATE user SET balance = balance - #{amount}, update_time = NOW() " +
            "WHERE id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}

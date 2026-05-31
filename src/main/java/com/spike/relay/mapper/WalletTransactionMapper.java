package com.spike.relay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spike.relay.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}

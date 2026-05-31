package com.spike.relay.service;

import com.spike.relay.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {

    /**
     * 接单入账：给用户加余额并写入账流水。供 confirmOrder 在事务内调用。
     */
    void income(Long userId, BigDecimal amount, Long relatedOrderId, String remark);

    /**
     * 提现申请：校验余额→原子冻结(扣减)余额→写"待打款"流水。
     * 注意：v1.0 不执行任何真实转账，仅生成线下打款待办。
     */
    WalletTransaction applyWithdraw(Long userId, BigDecimal amount);

    /**
     * 钱包概览：余额 + 最近流水。
     */
    Map<String, Object> getWallet(Long userId);

    List<WalletTransaction> listTransactions(Long userId);

    /** 运营：列出所有待打款的提现申请 */
    List<WalletTransaction> listPendingWithdraws();

    /** 运营：标记提现已打款(线下转账成功后) */
    void markWithdrawDone(Long transactionId);

    /** 运营：标记提现失败并退回冻结的余额 */
    void markWithdrawFailed(Long transactionId);
}

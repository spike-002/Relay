package com.spike.relay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spike.relay.entity.User;
import com.spike.relay.entity.WalletTransaction;
import com.spike.relay.enums.TransactionStatusEnum;
import com.spike.relay.enums.TransactionTypeEnum;
import com.spike.relay.mapper.UserMapper;
import com.spike.relay.mapper.WalletTransactionMapper;
import com.spike.relay.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletTransactionMapper transactionMapper;

    /** 单笔提现下限，避免无意义的零钱申请 */
    private static final BigDecimal MIN_WITHDRAW = new BigDecimal("1.00");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void income(Long userId, BigDecimal amount, Long relatedOrderId, String remark) {
        if (amount == null || amount.signum() <= 0) {
            return; // 0 元订单无需入账
        }
        int rows = userMapper.addBalance(userId, amount);
        if (rows != 1) {
            throw new IllegalStateException("入账失败，用户不存在");
        }
        User user = userMapper.selectById(userId);
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionNo(generateNo("IN"));
        tx.setUserId(userId);
        tx.setType(TransactionTypeEnum.INCOME.getCode());
        tx.setStatus(TransactionStatusEnum.SUCCESS.getCode());
        tx.setAmount(amount);
        tx.setBalanceAfter(user.getBalance());
        tx.setRelatedOrderId(relatedOrderId);
        tx.setRemark(remark);
        transactionMapper.insert(tx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction applyWithdraw(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(MIN_WITHDRAW) < 0) {
            throw new IllegalArgumentException("提现金额不能低于 " + MIN_WITHDRAW + " 元");
        }
        // 原子扣减：余额不足时返回 0，绝不允许余额变负
        int rows = userMapper.deductBalance(userId, amount);
        if (rows != 1) {
            throw new IllegalArgumentException("余额不足，无法提现");
        }
        User user = userMapper.selectById(userId);
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionNo(generateNo("WD"));
        tx.setUserId(userId);
        tx.setType(TransactionTypeEnum.WITHDRAW.getCode());
        // 仅生成"待打款"申请，平台无真实资金，由运营线下转账后改为已打款；失败则需退回余额
        tx.setStatus(TransactionStatusEnum.WITHDRAWING.getCode());
        tx.setAmount(amount.negate());
        tx.setBalanceAfter(user.getBalance());
        tx.setRemark("提现申请，等待运营打款");
        transactionMapper.insert(tx);
        return tx;
    }

    @Override
    public Map<String, Object> getWallet(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", user != null ? user.getBalance() : BigDecimal.ZERO);
        result.put("transactions", listTransactions(userId));
        return result;
    }

    @Override
    public List<WalletTransaction> listTransactions(Long userId) {
        return transactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, userId)
                        .orderByDesc(WalletTransaction::getCreateTime));
    }

    private String generateNo(String prefix) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + ts + random;
    }

    @Override
    public List<WalletTransaction> listPendingWithdraws() {
        return transactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, TransactionTypeEnum.WITHDRAW.getCode())
                        .eq(WalletTransaction::getStatus, TransactionStatusEnum.WITHDRAWING.getCode())
                        .orderByAsc(WalletTransaction::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markWithdrawDone(Long transactionId) {
        // 原子流转 待打款->已打款：返回 0 表示已被处理过，防止重复操作
        int rows = transactionMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getId, transactionId)
                        .eq(WalletTransaction::getType, TransactionTypeEnum.WITHDRAW.getCode())
                        .eq(WalletTransaction::getStatus, TransactionStatusEnum.WITHDRAWING.getCode())
                        .set(WalletTransaction::getStatus, TransactionStatusEnum.DONE.getCode())
                        .set(WalletTransaction::getRemark, "已线下打款"));
        if (rows != 1) {
            throw new IllegalArgumentException("该提现申请不存在或已被处理");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markWithdrawFailed(Long transactionId) {
        WalletTransaction wd = transactionMapper.selectById(transactionId);
        // 原子流转 待打款->已退回：守住"已打款的钱不能再退"这条线
        int rows = transactionMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getId, transactionId)
                        .eq(WalletTransaction::getType, TransactionTypeEnum.WITHDRAW.getCode())
                        .eq(WalletTransaction::getStatus, TransactionStatusEnum.WITHDRAWING.getCode())
                        .set(WalletTransaction::getStatus, TransactionStatusEnum.FAILED.getCode())
                        .set(WalletTransaction::getRemark, "打款失败，余额已退回"));
        if (rows != 1) {
            throw new IllegalArgumentException("该提现申请不存在或已被处理");
        }
        // 退回冻结的余额（提现时金额记为负数，退款金额取其相反数）
        BigDecimal refund = wd.getAmount().negate();
        userMapper.addBalance(wd.getUserId(), refund);
        User user = userMapper.selectById(wd.getUserId());
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionNo(generateNo("RF"));
        tx.setUserId(wd.getUserId());
        tx.setType(TransactionTypeEnum.REFUND.getCode());
        tx.setStatus(TransactionStatusEnum.SUCCESS.getCode());
        tx.setAmount(refund);
        tx.setBalanceAfter(user.getBalance());
        tx.setRemark("提现 " + wd.getTransactionNo() + " 打款失败退回");
        transactionMapper.insert(tx);
    }

}

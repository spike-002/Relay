package com.spike.relay.controller;

import com.spike.relay.common.Result;
import com.spike.relay.entity.WalletTransaction;
import com.spike.relay.service.WalletService;
import com.spike.relay.util.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /** 钱包概览：余额 + 流水 */
    @GetMapping
    public Result<Map<String, Object>> getWallet() {
        return Result.success(walletService.getWallet(UserContextHolder.getUserId()));
    }

    /**
     * 提现申请。注意：v1.0 仅生成"待打款"申请并冻结余额，不执行任何真实转账。
     */
    @PostMapping("/withdraw")
    public Result<WalletTransaction> withdraw(@RequestBody Map<String, Object> params) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(params.get("amount").toString());
        } catch (NullPointerException | NumberFormatException e) {
            return Result.fail(400, "提现金额格式不正确");
        }
        try {
            WalletTransaction tx = walletService.applyWithdraw(UserContextHolder.getUserId(), amount);
            return Result.success("提现申请已提交，等待运营打款", tx);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

package com.spike.relay.controller;

import com.spike.relay.common.Result;
import com.spike.relay.entity.WalletTransaction;
import com.spike.relay.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 运营后台：提现线下打款处理。
 * 注意：这些接口会触发真实资金决策（退余额），用独立的运营口令保护，
 * 不能仅靠普通用户 JWT —— 普通登录只证明"是某个用户"，不证明"是运营"。
 */
@RestController
@RequestMapping("/admin/withdraw")
public class AdminController {

    @Autowired
    private WalletService walletService;

    @Value("${admin.token}")
    private String adminToken;

    private void checkAdmin(String token) {
        if (adminToken == null || adminToken.isBlank() || !adminToken.equals(token)) {
            throw new SecurityException("无运营权限");
        }
    }

    /** 列出所有待打款的提现申请 */
    @GetMapping("/pending")
    public Result<List<WalletTransaction>> pending(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        try {
            checkAdmin(token);
            return Result.success(walletService.listPendingWithdraws());
        } catch (SecurityException e) {
            return Result.fail(403, e.getMessage());
        }
    }

    /** 标记已打款（线下转账成功后调用） */
    @PostMapping("/done")
    public Result<Void> done(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                             @RequestBody Map<String, Object> params) {
        try {
            checkAdmin(token);
            Long txId = Long.valueOf(params.get("transactionId").toString());
            walletService.markWithdrawDone(txId);
            return Result.success("已标记打款完成", null);
        } catch (SecurityException e) {
            return Result.fail(403, e.getMessage());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.fail(400, e.getMessage() != null ? e.getMessage() : "参数错误");
        }
    }

    /** 标记打款失败并退回余额 */
    @PostMapping("/fail")
    public Result<Void> fail(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                             @RequestBody Map<String, Object> params) {
        try {
            checkAdmin(token);
            Long txId = Long.valueOf(params.get("transactionId").toString());
            walletService.markWithdrawFailed(txId);
            return Result.success("已标记失败，余额已退回", null);
        } catch (SecurityException e) {
            return Result.fail(403, e.getMessage());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.fail(400, e.getMessage() != null ? e.getMessage() : "参数错误");
        }
    }
}

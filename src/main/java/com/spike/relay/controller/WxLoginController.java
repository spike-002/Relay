package com.spike.relay.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spike.relay.entity.User;
import com.spike.relay.mapper.UserMapper;
import com.spike.relay.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class WxLoginController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");

        // Mock: in production, call WeChat jscode2session to get openid
        // String openid = callWxApi(code);
        String openid = "mock_openid_" + code;

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openid)
        );

        if (user == null) {
            user = new User();
            user.setOpenId(openid);
            user.setNickname("wx_" + code);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        String token = JwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("token", token);
        result.put("userId", user.getId());
        return result;
    }
}

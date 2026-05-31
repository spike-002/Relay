package com.spike.relay.controller;

import com.spike.relay.common.Result;
import com.spike.relay.entity.User;
import com.spike.relay.mapper.UserMapper;
import com.spike.relay.util.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/info")
    public Result<User> getMyInfo() {
        User user = userMapper.selectById(UserContextHolder.getUserId());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(user);
    }
}

package com.example.tomatomall.controller;

import com.example.tomatomall.po.LoginRequest;
import com.example.tomatomall.po.User;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/accounts")


public class AccountController {


    /**
     * 获取用户详情
     */
    @Autowired
    private AccountService accountService;
    @Autowired
    private TokenUtil tokenUtil;


    @GetMapping("/{username}")
    public Response<User> getUser(@PathVariable String username) {
        User user = accountService.findByUsername(username);
        if (user != null) {
            return Response.buildSuccess(user);//返回user结构体
        }
        return Response.buildFailure("User not found", "401");
    }

    /**
     * 创建新的用户
     */

    @PostMapping()
    public Response createUser(@RequestBody User user) {
        if (accountService.findByUsername(user.getUsername()) != null) {//保证用户名唯一
            return Response.buildFailure("Username already exists", "409");
        }
        User createdUser = accountService.createUser(user);
        return Response.buildSuccess("注册成功");
    }
    /**
     * 更新用户信息
     */
    @PutMapping()
    public Response updateUser(@RequestBody User user) {
        if (user == null || user.getUsername() == null) {
            return Response.buildFailure("Invalid user data", "400");
        }

        User updatedUser = accountService.updateUser(user);
        if (updatedUser != null) {
            return Response.buildSuccess(updatedUser);
        } else {
            return Response.buildFailure("User not found or update failed", "404");
        }
    }
    /**
     * 登录
     */
    @PostMapping("/login")
    public Response login(@RequestParam String username, @RequestParam String password) {
        if (accountService.authenticate(username, password)) {
            User user = accountService.findByUsername(username);
            return Response.buildSuccess(tokenUtil.getToken(user));
        } else {
            return Response.buildFailure("Invalid username or password", "401");
        }
    }

}

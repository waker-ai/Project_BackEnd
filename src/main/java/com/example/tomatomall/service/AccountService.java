package com.example.tomatomall.service;

import com.example.tomatomall.po.User;

public interface AccountService {
    User findByUsername(String username);
    User createUser(User user);
    User updateUser(User user);
    User getInformation();
    boolean authenticate(String username, String password);//判断是否有该用户

}

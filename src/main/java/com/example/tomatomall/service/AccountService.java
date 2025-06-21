package com.example.tomatomall.service;

import com.example.tomatomall.po.User;
/**
 * 用户账户服务接口，定义用户相关的操作
 */
public interface AccountService {
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 查找到的用户实体，如果不存在返回 null
     */
    User findByUsername(String username);

    /**
     * 创建新用户
     * @param user 用户实体，包含用户信息
     * @return 创建成功后的用户实体
     */
    User createUser(User user);

    /**
     * 更新用户信息
     * @param user 用户实体，包含需要更新的信息
     * @return 更新后的用户实体
     */
    User updateUser(User user);

    /**
     * 验证用户名和密码是否匹配
     * @param username 用户名
     * @param password 明文密码
     * @return 如果用户名存在且密码正确返回 true，否则返回 false
     */
    boolean authenticate(String username, String password);//判断是否有该用户

}

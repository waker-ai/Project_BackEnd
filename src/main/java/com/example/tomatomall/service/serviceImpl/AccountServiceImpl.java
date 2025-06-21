
package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.OssService;
import com.example.tomatomall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * 账号相关业务实现类
 * 负责用户的注册、信息查询、更新和认证等功能
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;//加密密码
    @Autowired
    private OssService ossService;
    @Autowired
    private SecurityUtil securityUtil;


    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return User 用户实体
     */
    @Override
    public User findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    /**
     * 获取当前登录用户信息
     * @return 当前用户实体
     */
    @Override
    public User getInformation() {
        User user=securityUtil.getCurrentUser();
        return user;
    }

    /**
     * 新建用户，密码会进行加密处理
     * 如果传入了头像（base64格式），则上传头像并设置URL
     * @param user 用户实体，包含用户名、密码、头像等信息
     * @return 保存后的用户实体
     */
    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));//加密密码
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String avatarUrl = uploadAvatar(user.getAvatar());
                user.setAvatar(avatarUrl);
        }
        return accountRepository.save(user);//储存到数据库表中
    }
    // FILEPATH: E:/lab3后端/src/main/java/com/example/tomatomall/service/serviceImpl/AccountServiceImpl.java

    /**
     * 将base64编码的头像图片上传至OSS服务器
     * @param base64Image base64格式的图片字符串
     * @return 上传成功后返回的图片URL
     */
    private String uploadAvatar(String base64Image){
        // 解码Base64字符串
        byte[] imageBytes = Base64.getDecoder().decode(base64Image.split(",")[1]);
        // 使用OssService上传图片
        return ossService.uploadFile(imageBytes, "avatar_" + System.currentTimeMillis() + ".png");
    }

    /**
     * 更新用户信息，包括姓名、头像、电话、邮箱、地址和密码等
     * @param user 包含新信息的用户实体
     * @return 更新后的用户实体，如果用户不存在则返回null
     */
    @Override
    public User updateUser(User user) {
        User existingUser = accountRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            // 更新用户信息，这个里面写了邮箱之类的更改，前端先确定能够实现用户和密码吧
            existingUser.setName(user.getName());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    String avatarUrl =  uploadAvatar(user.getAvatar());
                    existingUser.setAvatar(avatarUrl);
            }
            existingUser.setTelephone(user.getTelephone());
            existingUser.setEmail(user.getEmail());
            existingUser.setLocation(user.getLocation());
            // 如果提供了新密码，则更新密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return accountRepository.save(existingUser);
        }
        return null;
    }

    /**
     * 用户认证，验证用户名和密码是否匹配
     * @param username 用户名
     * @param password 明文密码
     * @return true 如果用户名存在且密码匹配；false 否则
     */
    @Override
    public boolean authenticate(String username, String password) {

        User user = accountRepository.findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }
}
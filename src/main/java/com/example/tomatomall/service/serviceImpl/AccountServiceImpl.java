
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


    @Override
    public User findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    @Override
    public User getInformation() {
        User user=securityUtil.getCurrentUser();
        return user;
    }
    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));//加密密码
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                String avatarUrl = uploadAvatar(user.getAvatar());
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                // 处理上传失败的情况，可以设置一个默认头像或者记录日志
                user.setAvatar(null);
            }
        }
        return accountRepository.save(user);//储存到数据库表中
    }
    private String uploadAvatar(String base64Image) throws IOException {
        // 解码Base64字符串
        byte[] imageBytes = Base64.getDecoder().decode(base64Image.split(",")[1]);
        // 使用OssService上传图片
        return ossService.uploadFile(imageBytes, "avatar_" + System.currentTimeMillis() + ".png");
    }

    @Override
    public User updateUser(User user) {
        User existingUser = accountRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            // 更新用户信息，这个里面写了邮箱之类的更改，前端先确定能够实现用户和密码吧
            existingUser.setName(user.getName());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    String avatarUrl = uploadAvatar(user.getAvatar());
                    existingUser.setAvatar(avatarUrl);
                } catch (IOException e) {
                    // 处理上传失败的情况，可以保留原头像或者记录日志
                }
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

    @Override
    public boolean authenticate(String username, String password) {

        User user = accountRepository.findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }
}
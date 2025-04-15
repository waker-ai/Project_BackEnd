package com.example.tomatomall.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.AccountRepository;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 这是一个token的工具类，
 * 设置了过期时间为1天。
 * getToken方法用来获取token，
 * token中包含了用户的Id、密码信息以及到期时间。
 * verifyToken方法用来检验token是否正确。
 * getUser方法用来从token中获得用户信息。
 */

@Component
public class TokenUtil {
    private static final long EXPIRE_TIME = 365 * 24 * 60 * 60 * 1000;

    @Autowired
    AccountRepository accountRepository;

    public String getToken(User user)
    {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        return JWT.create()
                .withAudience(String.valueOf(user.getId()))
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(user.getUsername()));
    }

    public boolean verifyToken(String token) {
        try {
            Long userId = Long.parseLong(JWT.decode(token).getAudience().get(0));
            User user = accountRepository.findById(userId).get();
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getUsername())).build();
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User getUser(String token) {
        Long userId = Long.parseLong(JWT.decode(token).getAudience().get(0));
        return accountRepository.findById(userId).get();
    }
}

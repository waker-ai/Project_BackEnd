package com.example.tomatomall.config;

import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.User;
import com.example.tomatomall.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 这个类定制了一个登录的拦截器，
 * SpringBoot的拦截器标准为HandlerInterceptor接口，
 * 这个类实现了这个接口，表示是SpringBoot标准下的，
 * 在preHandle方法中，通过获取请求头Header中的token，
 * 判断了token是否合法，如果不合法则抛异常，
 * 合法则将用户信息存储到request的session中。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.info("被拦截的路径: {}", request.getRequestURI());
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 放行预检请求
        }

        String token = request.getHeader("token");

        if (token != null && tokenUtil.verifyToken(token)) {
            User user = tokenUtil.getUser(token);

            if (user != null) {
                request.getSession().setAttribute("currentUser", user);
//                logger.info("用户信息存入session: " + user);
                return true;
            } else {
                logger.warn("用户信息为空");
            }
        }
        logger.warn("Invalid or missing token");
        throw TomatoException.notLogin();
    }
}

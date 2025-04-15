package com.example.tomatomall.util;

import com.example.tomatomall.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 可以通过这个类来获得当前用户的信息
 */
@Component
public class SecurityUtil {

    @Autowired
    HttpServletRequest httpServletRequest;

    public User getCurrentUser() { return (User) httpServletRequest.getSession().getAttribute("currentUser"); }

}

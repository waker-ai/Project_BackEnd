package com.example.tomatomall.service;

import javax.servlet.http.HttpServletResponse;

public interface OrderService extends AliPayable{
    void pay(Long orderId, HttpServletResponse httpServletResponse);
}

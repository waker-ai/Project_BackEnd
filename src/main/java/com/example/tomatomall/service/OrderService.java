package com.example.tomatomall.service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface OrderService extends AliPayable{
    Map<String, Object> pay(Long orderId, HttpServletResponse httpServletResponse);
}

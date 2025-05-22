package com.example.tomatomall.service;

import com.example.tomatomall.vo.OrderDetailVO;
import com.example.tomatomall.vo.OrderVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface OrderService extends AliPayable{
    Map<String, Object> pay(Long orderId, HttpServletResponse httpServletResponse);
    List<OrderVO> getHistoryOrders();

    OrderDetailVO getOrderDetail(Long orderId);
}

package com.example.tomatomall.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.tomatomall.config.AliPayConfig;
import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AliPayConfig aliPayConfig;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);


    @Override
    public Map<String, Object> pay(Long orderId, javax.servlet.http.HttpServletResponse httpServletResponse) {
        Order order = orderRepository.findByOrderId(orderId).orElse(null);
        if (order == null) {
            throw TomatoException.orderNotFound();
        }

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", String.valueOf(orderId));
        bizContent.put("total_amount", String.valueOf(order.getTotalAmount()));
        bizContent.put("subject", order.getPaymentMethod());
        bizContent.put("body", "normal_order");
        String form = "";
        try {
            Map<String, String> returnParams = new HashMap<>();
            Map<String, String> notifyParams = new HashMap<>();
            returnParams.put("url", URLEncoder.encode("http://localhost:3000/#/cart", "UTF-8"));
            notifyParams.put("service", "orderService");

            form = aliPayConfig.pay(bizContent, notifyParams, returnParams);
        } catch (Exception e) {
            logger.error("调用支付宝支付异常", e); // 打印具体异常
            throw TomatoException.payError();
        }

//        httpServletResponse.setContentType("text/html;charset=UTF-8");
//        try {
//            httpServletResponse.getWriter().write(form);
//            httpServletResponse.getWriter().flush();
//            httpServletResponse.getWriter().close();
//        } catch (IOException e) {
//            throw TomatoException.payError();
//        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentForm", form);
        result.put("orderId", orderId);
        result.put("totalAmount", order.getTotalAmount());
        result.put("paymentMethod", order.getPaymentMethod());
        logger.info("pay method result:" + result.toString());
        return result;
    }

    @Override
    public boolean payNotify(Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("out_trade_no"));
//        try {
//            BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
//        } catch (NumberFormatException e) {
//            throw TomatoException.NumberFormatError();
//        }
        Order order = orderRepository.findByOrderId(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        try {
            assertOrderStatus(order, OrderStatusEnum.PENDING);
        } catch (TomatoException e) {
            return false;
        }
        order.setStatus(OrderStatusEnum.SUCCESS);
        orderRepository.save(order);
        logger.info(String.format("Order %d payed", order.getOrderId()));
        return true;
    }


    private void assertOrderStatus(Order order, OrderStatusEnum orderStatus) {
        if (order.getStatus() != orderStatus)
            throw TomatoException.illegalOrderState();
    }

}

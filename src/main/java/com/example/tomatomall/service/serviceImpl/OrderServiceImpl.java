package com.example.tomatomall.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.tomatomall.config.AliPayConfig;
import com.example.tomatomall.config.AliPayOrder;
import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.OrderItem;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.OrderItemRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tomatomall.vo.OrderDetailVO;
import com.example.tomatomall.vo.OrderVO;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import java.text.SimpleDateFormat;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AliPayOrder aliPayConfig;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private StockpileRepository stockpileRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<OrderVO> getHistoryOrders() {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            logger.warn("当前未登录用户尝试获取历史订单");
            return new ArrayList<>();
        }
        Long userId = user.getId();
        List<Order> orders = orderRepository.findAllByUserId(userId);
        if (orders == null) {
            return new ArrayList<>();
        }
        return orders.stream()
                .map(Order::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(TomatoException::orderNotFound);
        User user = accountRepository.findById(order.getUserId()).orElseThrow(TomatoException::userNotFount);

        OrderDetailVO orderDetail = new OrderDetailVO();
        orderDetail.setOrderId(orderId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        orderDetail.setCreateTime(sdf.format(order.getCreateTime()));
        orderDetail.setPaymentMethod(order.getPaymentMethod());
        orderDetail.setStatus(order.getStatus().toString());
        orderDetail.setReceiverName(user.getName());
        orderDetail.setReceiverPhone(user.getTelephone());

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderDetailVO.OrderItemDetail> orderItemDetails = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId()).orElseThrow(TomatoException::productNotFound);

            OrderDetailVO.OrderItemDetail itemDetail = new OrderDetailVO.OrderItemDetail();
            itemDetail.setProductId(product.getId());
            itemDetail.setProductName(product.getTitle());
            itemDetail.setQuantity(orderItem.getQuantity());
            itemDetail.setPrice(product.getPrice().doubleValue());
            orderItemDetails.add(itemDetail);
        }
        orderDetail.setItems(orderItemDetails);
        logger.info("getOrderDetail:" + orderDetail);
        return orderDetail;
    }

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
            returnParams.put("url", URLEncoder.encode("http://localhost:3000/#/cart", "UTF-8"));

            form = aliPayConfig.pay(bizContent, returnParams);
        } catch (Exception e) {
            logger.error("调用支付宝支付异常", e); // 打印具体异常
            throw TomatoException.payError();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentForm", form);
        result.put("orderId", orderId);
        result.put("totalAmount", order.getTotalAmount());
        result.put("paymentMethod", order.getPaymentMethod());
//        logger.info("pay method result:" + result);
        return result;
    }

    @Override
    @Transactional
    public boolean payNotify(Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("out_trade_no"));
//        logger.info("order" + orderId + " enter into payNotify");
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

        // 释放冻结库存
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<Long> cartItemIds = orderItems.stream()
                .map(OrderItem::getCartItemId)
                .collect(Collectors.toList());

        for (Long cartItemId : cartItemIds) {
            Long productId = cartRepository.findById(cartItemId).get().getProductId();
            Integer quantity = cartRepository.findById(cartItemId).get().getQuantity();
            Stockpile stockpile = stockpileRepository.findByProductId(productId).orElseThrow(TomatoException::productNotFound);
            stockpile.setFrozen(stockpile.getFrozen() - quantity);
            logger.info("释放冻结库存：" + quantity);
            stockpileRepository.save(stockpile);
        }

//        logger.info("order" + orderId + " status changed to success");
        // 删除购物车中相关商品
        logger.info(cartItemIds.toString());
        if(!cartItemIds.isEmpty()) {
            cartRepository.deleteAllById(cartItemIds);
        }

        // 删除orderId和cartItemId的映射关系
//        orderItemRepository.deleteAllByOrderId(orderId);

        orderRepository.save(order);

        logger.info(String.format("Order %d payed", order.getOrderId()));
        return true;
    }


    private void assertOrderStatus(Order order, OrderStatusEnum orderStatus) {
        if (order.getStatus() != orderStatus)
            throw TomatoException.illegalOrderState();
    }

}

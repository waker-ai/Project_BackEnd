package com.example.tomatomall.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderServiceImpl 实现类
 * 负责订单相关业务逻辑的实现，如订单支付、查询、详情获取、评论状态更新等。
 *
 * 支持功能包括：
 * - 提交支付请求并调用支付宝支付
 * - 处理支付成功回调并释放冻结库存
 * - 获取用户历史订单
 * - 获取订单详情
 * - 更新订单项的评论状态
 */
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
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;


    /**
     * 更新订单项中某个商品的评论状态。
     *
     * @param orderId    订单ID
     * @param productId  商品ID
     * @param isReviewed 是否已评论
     */
    @Override
    @Transactional
    public void updateReviewStatus(Long orderId, Long productId, boolean isReviewed) {
        OrderItem orderItem = orderItemRepository.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(TomatoException::orderItemNotFound);
        orderItem.setReviewed(isReviewed);
        logger.info("OrderId: {}, productId: {} is Reviewed", orderId, productId);
        orderItemRepository.save(orderItem);
    }

    /**
     * 获取当前登录用户的所有历史订单（不包含订单项详情）。
     *
     * @return 当前用户的订单列表（简要信息）
     */
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

    /**
     * 获取指定订单的详细信息，包括收货人、订单状态、商品列表等。
     *
     * @param orderId 订单ID
     * @return 订单详情视图对象
     */
    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(TomatoException::orderNotFound);
        OrderDetailVO orderDetail = new OrderDetailVO();
        orderDetail.setOrderId(orderId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        orderDetail.setCreateTime(sdf.format(order.getCreateTime()));
        orderDetail.setPaymentMethod(order.getPaymentMethod());
        orderDetail.setStatus(order.getStatus().toString());
        Address address = addressRepository.findById(order.getShippingAddressId()).orElseThrow(TomatoException::addressNotFound);
        orderDetail.setReceiverName(address.getAddresseeName());
        orderDetail.setReceiverPhone(address.getPhone());

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderDetailVO.OrderItemDetail> orderItemDetails = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId()).orElseThrow(TomatoException::productNotFound);

            OrderDetailVO.OrderItemDetail itemDetail = new OrderDetailVO.OrderItemDetail();
            itemDetail.setProductId(product.getId());
            itemDetail.setProductName(product.getTitle());
            itemDetail.setQuantity(orderItem.getQuantity());
            itemDetail.setPrice(product.getPrice().doubleValue());
            itemDetail.setReviewed(orderItem.isReviewed());
            orderItemDetails.add(itemDetail);
        }
        orderDetail.setItems(orderItemDetails);
        logger.info("getOrderDetail:" + orderDetail);
        return orderDetail;
    }

    /**
     * 发起支付宝支付请求。
     *
     * @param orderId            订单ID
     * @param httpServletResponse 用于传输支付跳转（未使用）
     * @return 返回包含支付表单HTML、订单信息等
     */
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


    /**
     * 支付成功回调处理。
     * 1. 更新订单状态为成功；
     * 2. 释放冻结库存；
     * 3. 删除购物车中已支付商品；
     *
     * @param params 回调参数，需包含 out_trade_no（订单号）
     * @return 是否处理成功
     */
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
            Product product = productRepository.findById(productId).get();
            product.setSales(product.getSales() + quantity);
            productRepository.save(product);
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


    /**
     * 校验订单当前状态是否符合预期状态。
     *
     * @param order       订单对象
     * @param orderStatus 预期状态
     */
    private void assertOrderStatus(Order order, OrderStatusEnum orderStatus) {
        if (order.getStatus() != orderStatus)
            throw TomatoException.illegalOrderState();
    }

}

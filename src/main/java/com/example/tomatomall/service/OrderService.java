package com.example.tomatomall.service;

import com.example.tomatomall.vo.OrderDetailVO;
import com.example.tomatomall.vo.OrderVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
/**
 * 订单服务接口 OrderService
 *
 * 定义了与订单相关的核心业务功能，包括：
 * - 支付请求发起
 * - 获取历史订单列表
 * - 查询订单详情
 * - 更新订单中商品的评论状态
 *
 * 本接口扩展了 {@link AliPayable}，表示具有支付宝支付能力。
 */
public interface OrderService extends AliPayable{
    /**
     * 发起支付宝支付请求。
     *
     * @param orderId             要支付的订单 ID
     * @param httpServletResponse 响应对象，用于写回支付表单（可选，可不使用）
     * @return 包含支付信息的 Map（如：支付表单HTML、订单ID、总金额、支付方式等）
     */
    Map<String, Object> pay(Long orderId, HttpServletResponse httpServletResponse);
    /**
     * 获取当前登录用户的历史订单列表。
     *
     * @return 订单简要信息列表 {@link OrderVO}
     */
    List<OrderVO> getHistoryOrders();

    /**
     * 获取指定订单的详细信息，包括商品、收货地址、下单时间等。
     *
     * @param orderId 要查询的订单 ID
     * @return 订单详情视图对象 {@link OrderDetailVO}
     */
    OrderDetailVO getOrderDetail(Long orderId);
    /**
     * 更新指定订单中某个商品的“是否已评论”状态。
     *
     * @param orderId    订单 ID
     * @param productId  商品 ID
     * @param isReviewed 是否已评论（true 表示已评论，false 表示未评论）
     */
    void updateReviewStatus(Long orderId,  Long productId, boolean isReviewed);
}

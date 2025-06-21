package com.example.tomatomall.service;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.vo.CartItemVO;

import java.util.List;
import java.util.Map;

public interface CartService {
    // 添加商品到购物车
    Map<String, Object> addCartItem(Long productId, Integer quantity);

    // 删除购物车商品
    void deleteCartItem(Long cartItemId);

    // 修改购物车商品数量
    void updateCartItemQuantity(Long cartItemId, Integer quantity);

    // 获取购物车商品列表
    Map<String, Object> getAllCartItems();

    // 结算购物车商品
    Order checkout(CheckoutRequest request);
}

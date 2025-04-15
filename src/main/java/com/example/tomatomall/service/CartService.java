package com.example.tomatomall.service;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.vo.CartItemVO;

import java.util.List;
import java.util.Map;

public interface CartService {
    Map<String, Object> addCartItem(Long productId, Integer quantity);

    void deleteCartItem(Long cartItemId);

    void updateCartItemQuantity(Long cartItemId, Integer quantity);

    Map<String, Object> getAllCartItems();

    Order checkout(CheckoutRequest request);
}

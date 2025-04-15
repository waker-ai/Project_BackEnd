package com.example.tomatomall.controller;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    //增加购物车商品
    @PostMapping
    public Response<Map<String, Object>> addCartItem(@RequestBody Map<Long, Object> request) {
        Long productId = (Long) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        return Response.buildSuccess(cartService.addCartItem(productId, quantity));
    }

    //删除购物车商品
    @DeleteMapping("/{cartItemId}")
    public Response<String> deleteCartItem(@PathVariable("cartItemId") Long cartItemId) {
        try {
            cartService.deleteCartItem(cartItemId);
            return Response.buildSuccess("删除成功");
        } catch (Exception e) {
            return Response.buildFailure("删除失败", "400");
        }
    }

    //修改购物车商品数量
    @PatchMapping("/{cartItemId}")
    public Response<String> updateCartItemQuantity(@PathVariable("cartItemId") Long cartItemId, @RequestBody Map<Long, Object> request) {
        Integer quantity = (Integer) request.get("quantity");
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            return Response.buildSuccess("修改数量成功");
        } catch (Exception e) {
            return Response.buildFailure("修改数量失败", "400");
        }
    }

    //获取购物车商品列表
    @GetMapping
    public Response<Map<String, Object>> getCartItems() { return Response.buildSuccess(cartService.getAllCartItems()); }

    @PostMapping("/checkout")
    public Response<Order>checkout(@RequestBody CheckoutRequest request){
        return Response.buildSuccess(cartService.checkout(request));
    }

}

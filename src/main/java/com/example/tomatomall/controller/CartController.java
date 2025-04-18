package com.example.tomatomall.controller;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.exception.TomatoException;
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
    public Response addCartItem(@RequestBody Map<String, Object> requestBody) {
        Long productId = Long.parseLong(requestBody.get("productId").toString());
        Integer quantity = Integer.parseInt(requestBody.get("quantity").toString());
        try {
            Map<String, Object> result = cartService.addCartItem(productId, quantity);
            return Response.buildSuccess(result);
        } catch (TomatoException e) {
            if("库存不足".equals(e.getMessage()))
            {
                return Response.buildFailure("库存不足", "400");
            } else {
                return Response.buildFailure("添加失败", "400");
            }
        }
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
    public Response<String> updateCartItemQuantity(@PathVariable("cartItemId") Long cartItemId, @RequestBody Map<String, Object> request) {
        Object quantityObj = request.get("quantity");
        Integer quantity = null;

        if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        } else if (quantityObj instanceof Number) {
            quantity = ((Number) quantityObj).intValue();
        } else if (quantityObj instanceof String) {
            quantity = Integer.parseInt((String) quantityObj);
        } else {
            throw new IllegalArgumentException("无效的 quantity 类型");
        }
        System.out.println(quantity);
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

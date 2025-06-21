package com.example.tomatomall.controller;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 购物车控制器：提供购物车相关的 API 接口。
 *
 * 包括功能：
 * - 添加商品到购物车
 * - 删除购物车商品
 * - 修改商品数量
 * - 获取购物车列表
 * - 结算购物车生成订单
 *
 * 请求路径统一前缀为：/api/cart
 */
@RestController
@RequestMapping("api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 添加商品到购物车。
     *
     * @param requestBody 包含 productId（商品 ID）和 quantity（商品数量）
     * @return 添加结果，成功返回商品详情；库存不足或失败返回错误信息
     */
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

    /**
     * 删除购物车中的某个商品项。
     *
     * @param cartItemId 购物车项 ID
     * @return 删除成功/失败响应
     */
    @DeleteMapping("/{cartItemId}")
    public Response<String> deleteCartItem(@PathVariable("cartItemId") Long cartItemId) {
        try {
            cartService.deleteCartItem(cartItemId);
            return Response.buildSuccess("删除成功");
        } catch (Exception e) {
            return Response.buildFailure("删除失败", "400");
        }
    }

    /**
     * 修改购物车中某个商品的数量。
     *
     * @param cartItemId 购物车项 ID
     * @param request    请求体，包含新的 quantity 值
     * @return 修改成功/失败响应
     */
    @PatchMapping("/{cartItemId}")
    public Response<String> updateCartItemQuantity(@PathVariable("cartItemId") Long cartItemId, @RequestBody Map<String, Object> request) {
        Object quantityObj = request.get("quantity");
        int quantity;

        if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        } else if (quantityObj instanceof Number) {
            quantity = ((Number) quantityObj).intValue();
        } else if (quantityObj instanceof String) {
            quantity = Integer.parseInt((String) quantityObj);
        } else {
            throw new IllegalArgumentException("无效的 quantity 类型");
        }
//        System.out.println(quantity);
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            return Response.buildSuccess("修改数量成功");
        } catch (Exception e) {
            return Response.buildFailure("修改数量失败", "400");
        }
    }

    /**
     * 获取当前用户的购物车商品列表。
     *
     * @return 返回包含购物车商品列表、总金额等信息的 map
     */
    @GetMapping
    public Response<Map<String, Object>> getCartItems() { return Response.buildSuccess(cartService.getAllCartItems()); }

    /**
     * 结算购物车，创建订单。
     *
     * @param request 结算请求体，包含购物车项、支付方式、收货地址等
     * @return 返回生成的订单对象
     */
    @PostMapping("/checkout")
    public Response<Order>checkout(@RequestBody CheckoutRequest request){
        return Response.buildSuccess(cartService.checkout(request));
    }

}

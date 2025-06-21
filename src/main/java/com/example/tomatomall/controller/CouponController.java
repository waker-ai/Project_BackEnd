package com.example.tomatomall.controller;

import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.tomatomall.dto.CreateCouponRequest;

import java.util.List;

/**
 * 优惠券控制器：处理与优惠券相关的 API 请求。
 *
 * 请求路径前缀为 /api/coupons，支持以下功能：
 * - 获取指定用户的所有优惠券
 * - 获取指定用户的可用优惠券
 * - 管理员创建优惠券并分发给多个用户
 * - 用户使用优惠券
 */
@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 获取指定用户名的所有优惠券。
     *
     * @param username 用户名（可选参数）
     * @return 若用户名存在，返回该用户所有优惠券列表；否则返回错误信息
     */
    @GetMapping
    public Response<List<CouponVO>> getCoupons(@RequestParam(required = false) String username) {
        if (username != null) {
            return Response.buildSuccess(couponService.getCouponsByUsername(username));
        }
        return Response.buildFailure("user not found", "404");
    }

    /**
     * 获取指定用户名下所有尚未使用、仍有效的优惠券。
     *
     * @param username 用户名（可选参数）
     * @return 若用户名存在，返回用户可用的优惠券列表；否则返回错误信息
     */
    @GetMapping("/available")
    public Response<List<CouponVO>> getAvailableCoupons(@RequestParam(required = false) String username) {
        if (username != null) {
            return Response.buildSuccess(couponService.getAvailableCouponsByUsername(username));
        }
        return Response.buildFailure("user not found", "404");
    }

    /**
     * 批量创建优惠券，并分发给多个用户。
     *
     * @param request 包含成员用户名列表和优惠券基本信息的请求体
     * @return 创建并分发成功的优惠券列表
     */
    @PostMapping
    public Response<List<CouponVO>> createCoupon(@RequestBody CreateCouponRequest request) {
        return Response.buildSuccess(couponService.createCouponsForMembers(request.getMembers(), request.getCouponVO()));
    }

    /**
     * 用户使用优惠券（标记为已使用）。
     *
     * @param couponVO 包含优惠券 ID 和基本信息的对象
     * @return 返回已使用状态的优惠券信息；若未提供 ID，返回失败响应
     */
    @PutMapping("/apply")
    public Response<CouponVO> useCoupon(@RequestBody CouponVO couponVO) {
        if(couponVO==null || couponVO.getId()==null){
            return Response.buildFailure("coupon not found", "404");
        }
        CouponVO usedCoupon = couponService.applyCoupon(couponVO);
        return Response.buildSuccess(usedCoupon);
    }








}

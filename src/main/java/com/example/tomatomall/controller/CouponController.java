package com.example.tomatomall.controller;

import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.tomatomall.dto.CreateCouponRequest;

import java.util.List;


@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public Response<List<CouponVO>> getCoupons(@RequestParam(required = false) String username) {
        if (username != null) {
            return Response.buildSuccess(couponService.getCouponsByUsername(username));
        }
        return Response.buildFailure("user not found", "404");
    }

    @GetMapping("/available")
    public Response<List<CouponVO>> getAvailableCoupons(@RequestParam(required = false) String username) {
        if (username != null) {
            return Response.buildSuccess(couponService.getAvailableCouponsByUsername(username));
        }
        return Response.buildFailure("user not found", "404");
    }

    @PostMapping
    public Response<List<CouponVO>> createCoupon(@RequestBody CreateCouponRequest request) {
        return Response.buildSuccess(couponService.createCouponsForMembers(request.getMembers(), request.getCouponVO()));
    }


    @PutMapping("/apply")
    public Response<CouponVO> useCoupon(@RequestBody CouponVO couponVO) {
        if(couponVO==null || couponVO.getId()==null){
            return Response.buildFailure("coupon not found", "404");
        }
        CouponVO usedCoupon = couponService.applyCoupon(couponVO);
        return Response.buildSuccess(usedCoupon);
    }








}

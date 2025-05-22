package com.example.tomatomall.service;

import com.example.tomatomall.po.Member;
import com.example.tomatomall.po.User;
import com.example.tomatomall.vo.CouponVO;

import java.util.List;

public interface CouponService {
    List<CouponVO> getCouponsByUsername(String username);
    List<CouponVO> getAvailableCouponsByUsername(String username);
    List<CouponVO> createCouponsForMembers(List<Member> members, CouponVO couponVO);
    CouponVO applyCoupon(CouponVO couponVO);
}

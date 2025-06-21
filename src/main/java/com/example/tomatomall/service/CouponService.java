package com.example.tomatomall.service;

import com.example.tomatomall.po.Member;
import com.example.tomatomall.po.User;
import com.example.tomatomall.vo.CouponVO;

import java.util.List;

public interface CouponService {
    /**
     * 根据用户名获取该用户的所有优惠券（包括已使用、过期等）
     *
     * @param username 用户名
     * @return 优惠券列表（可能包含已失效或已使用）
     */
    List<CouponVO> getCouponsByUsername(String username);
    /**
     * 根据用户名获取该用户当前可用的优惠券（未过期、未使用）
     *
     * @param username 用户名
     * @return 当前可用的优惠券列表
     */
    List<CouponVO> getAvailableCouponsByUsername(String username);
    /**
     * 为指定的会员列表批量创建优惠券
     *
     * @param members   要发放优惠券的会员列表
     * @param couponVO  优惠券信息（模板或规则）
     * @return 发放成功的优惠券信息列表
     */
    List<CouponVO> createCouponsForMembers(List<Member> members, CouponVO couponVO);
    /**
     * 用户在下单或结算时选择并使用某张优惠券
     *
     * @param couponVO 包含待使用的优惠券ID和相关信息
     * @return 使用后的优惠券信息（如已标记为使用）
     */
    CouponVO applyCoupon(CouponVO couponVO);
}

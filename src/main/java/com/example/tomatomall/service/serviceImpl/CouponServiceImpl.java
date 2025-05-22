package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Member;
import com.example.tomatomall.repository.CouponRepository;
import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.po.Coupon;
import com.example.tomatomall.vo.CouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponRepository couponRepository;

    @Override
    public List<CouponVO> getCouponsByUsername(String username) {
        List<Coupon> coupons = couponRepository.findByUsername(username);
        return coupons.stream().map(Coupon::toVO).collect(Collectors.toList());
    }

    @Override
    public List<CouponVO> getAvailableCouponsByUsername(String username) {
        List<Coupon> coupons = couponRepository.findAvailableCouponsByUsername(username, LocalDateTime.now());
        return coupons.stream().map(Coupon::toVO).collect(Collectors.toList());
    }



    @Override
    public List<CouponVO> createCouponsForMembers(List<Member> members, CouponVO couponVO) {
        List<CouponVO> result = new ArrayList<>();
        for (Member member : members) {
            Coupon coupon = new Coupon();
            coupon.setUsername(member.getUsername()); // 设置用户用户名
            coupon.setDiscountAmount(couponVO.getDiscountAmount());
            coupon.setMinCost(couponVO.getMinCost());
            coupon.setValidTime(couponVO.getValidTime());
            coupon.setUsed(couponVO.isUsed());
            couponRepository.save(coupon);
            result.add(coupon.toVO());
        }
        return result;
    }

    @Override
    public CouponVO applyCoupon(CouponVO couponVO) {
        Coupon coupon = couponRepository.findById(couponVO.getId()).orElse(null);
        coupon.setUsed(true);
        Coupon updatedCoupon = couponRepository.save(coupon);
        return updatedCoupon.toVO();
    }









}

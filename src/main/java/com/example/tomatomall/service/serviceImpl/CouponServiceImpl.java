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

/**
 * 优惠券服务实现类：实现 CouponService 接口中定义的所有业务逻辑。
 *
 * 功能包括：
 * - 获取用户所有优惠券
 * - 获取用户可用优惠券
 * - 管理员为多个会员批量发放优惠券
 * - 用户使用优惠券
 */
@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponRepository couponRepository;

    /**
     * 获取指定用户的所有优惠券（不区分是否已使用、是否过期）。
     *
     * @param username 用户名
     * @return 用户的所有优惠券信息列表（VO 格式）
     */
    @Override
    public List<CouponVO> getCouponsByUsername(String username) {
        List<Coupon> coupons = couponRepository.findByUsername(username);
        return coupons.stream().map(Coupon::toVO).collect(Collectors.toList());
    }

    /**
     * 获取指定用户当前仍有效、尚未使用的优惠券。
     *
     * @param username 用户名
     * @return 可用的优惠券列表（VO 格式）
     */
    @Override
    public List<CouponVO> getAvailableCouponsByUsername(String username) {
        List<Coupon> coupons = couponRepository.findAvailableCouponsByUsername(username, LocalDateTime.now());
        return coupons.stream().map(Coupon::toVO).collect(Collectors.toList());
    }


    /**
     * 用户使用优惠券（即将其标记为“已使用”）。
     *
     * @param couponVO 包含优惠券 ID 的对象
     * @return 标记为“已使用”后的优惠券（VO 格式）
     */
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

package com.example.tomatomall.vo;

import com.example.tomatomall.po.Coupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponVO {
    private Long id;
    private String username;
    private BigDecimal discountAmount;
    private BigDecimal minCost;
    private LocalDateTime startDate;
    private Integer validTime;
    private LocalDateTime endDate;
    private boolean isUsed;

    public Coupon toPO(){
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setUsername(username);
        coupon.setDiscountAmount(discountAmount);
        coupon.setMinCost(minCost);
        coupon.setStartDate(startDate);
        coupon.setValidTime(validTime);
        coupon.setEndDate(endDate);
        coupon.setUsed(isUsed);
        return coupon;
    }
}

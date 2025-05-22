package com.example.tomatomall.po;

import com.example.tomatomall.vo.CouponVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id; // id

    @Column(name = "username")
    private String username; // 用户名

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "min_cost", nullable = false)
    private BigDecimal minCost;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date", updatable = false)
    private LocalDateTime startDate;

    @Column(name = "valid_time", nullable = false)
    private Integer validTime; // 有效期，单位：天

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_used")
    private boolean isUsed;

    // 自动设置 startDate 和 endDate
    @PrePersist
    protected void onCreate() {
        this.startDate = LocalDateTime.now();
        if (validTime != null) {
            this.endDate = this.startDate.plusDays(validTime);
        } else {
            this.endDate = this.startDate; // fallback，如果没设置 validTime
        }
    }

    public CouponVO toVO() {
        CouponVO couponVO = new CouponVO();
        couponVO.setId(id);
        couponVO.setUsername(username);
        couponVO.setDiscountAmount(discountAmount);
        couponVO.setMinCost(minCost);
        couponVO.setStartDate(startDate);
        couponVO.setValidTime(validTime);
        couponVO.setEndDate(endDate);
        couponVO.setUsed(isUsed);
        return couponVO;
    }
}
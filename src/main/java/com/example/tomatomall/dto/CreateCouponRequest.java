package com.example.tomatomall.dto;

import com.example.tomatomall.po.Member;
import com.example.tomatomall.vo.CouponVO;
import lombok.Data;

import java.util.List;

@Data
public class CreateCouponRequest {
    private List<Member> members;
    private CouponVO couponVO;
}
package com.example.tomatomall.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class CheckoutRequest {
    private List<Long> cartItemIds;
    private Long shippingAddressId;
    private Long selectedCouponId;
    private String paymentMethod;
}


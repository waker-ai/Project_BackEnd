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
    private ShippingAddress shippingAddress;
    private String paymentMethod;
}

@Data
@Getter
@Setter
class ShippingAddress {
    private String name; //姓名
    private String phone; //手机号
    private String postalCode; //邮编
    private String address; //地址
}

package com.example.tomatomall.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetailVO {
    private Long orderId;
    private String createTime;
    private String paymentMethod;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private List<OrderItemDetail> items;

    @Data
    public static class OrderItemDetail {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
        private boolean reviewed;
    }
}
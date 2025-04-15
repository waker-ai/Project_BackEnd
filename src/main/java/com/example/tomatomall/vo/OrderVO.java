package com.example.tomatomall.vo;

import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private OrderStatusEnum status;
    private Date createTime;

    public Order toPO() {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(status);
        order.setCreateTime(createTime);
        return order;
    }

}

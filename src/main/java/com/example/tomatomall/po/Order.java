package com.example.tomatomall.po;

import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.vo.OrderVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //订单Id
    @Id
    @Column(name = "order_id")
    private Long orderId;

    //用户Id
    @Basic
    @Column(name = "user_id")
    private Long userId;
    //订单总金额
    @Basic
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    //支付方式
    @Basic
    @Column(name = "payment_method")
    private String paymentMethod;

    //订单支付状态
    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatusEnum status;
    //订单创建时间
    @Basic
    @Column(name = "create_time")
    private Date createTime;

    public OrderVO toVO() {
        OrderVO order = new OrderVO();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(status);
        order.setCreateTime(createTime);
        return order;
    }

}

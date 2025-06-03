package com.example.tomatomall.po;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts_orders_relation")
public class OrderItem {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Basic
    @Column(name = "order_id")
    private Long orderId;

    @Basic
    @Column(name = "product_id")
    private Long productId;

    @Basic
    @Column(name = "quantity")
    private Integer quantity;

    @Getter
    @Basic
    @Column(name = "reviewed")
    private boolean reviewed = false; // 新增字段，用于标记是否已评价
}

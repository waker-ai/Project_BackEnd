package com.example.tomatomall.repository;

import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.po.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(Long orderId);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatusEnum status);

    List<Order> findByStatusAndCreateTimeBefore(OrderStatusEnum orderStatusEnum, Date deadline);
}

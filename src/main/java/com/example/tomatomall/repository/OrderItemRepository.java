package com.example.tomatomall.repository;

import com.example.tomatomall.po.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    void deleteAllByOrderId(Long orderId);
}

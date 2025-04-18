package com.example.tomatomall.repository;

import com.example.tomatomall.po.CartItem;
import com.example.tomatomall.vo.CartItemVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem>findByUserIdAndProductId(Long userId, Long productId);

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndCartItemId(Long userId, Long cartItemId);

    void deleteByProductId(Long productId);
}

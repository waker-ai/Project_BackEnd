package com.example.tomatomall.repository;

import com.example.tomatomall.po.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByUsername(String username);
    @Query("SELECT c FROM Coupon c WHERE c.username = :username AND c.isUsed = false AND c.startDate <= :now AND c.endDate >= :now")
    List<Coupon> findAvailableCouponsByUsername(@Param("username") String username, @Param("now") LocalDateTime now);

}

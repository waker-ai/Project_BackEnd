package com.example.tomatomall.repository;

import com.example.tomatomall.po.Stockpile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockpileRepository extends JpaRepository<Stockpile, Long> {
    Optional<Stockpile> findByProductId(Long productId);

}



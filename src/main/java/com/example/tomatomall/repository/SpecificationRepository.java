package com.example.tomatomall.repository;

import com.example.tomatomall.po.Specification;
import com.example.tomatomall.po.Stockpile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {
    void deleteByProductId(Long productId);
}

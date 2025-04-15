package com.example.tomatomall.repository;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

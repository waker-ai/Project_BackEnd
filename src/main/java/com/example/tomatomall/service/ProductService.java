package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    List<ProductVO> getAllProducts();
    ProductVO getProduct(Long id);
    ProductVO updateProduct(ProductVO productVO);
    ProductVO createProduct(ProductVO productVO);
    boolean deleteProduct(Long id);
    void adjustStock(Long productId, Integer amount);
    Page<ProductVO> getProductsBySales(Pageable pageable);
    Page<ProductVO> getProductsByScore(Pageable pageable);

}

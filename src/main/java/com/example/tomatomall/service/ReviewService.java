package com.example.tomatomall.service;

import com.example.tomatomall.vo.ReviewVO;

import java.util.List;

public interface ReviewService {
    ReviewVO createReview(ReviewVO reviewVO);
    List<ReviewVO> getReviewsByProductId(Long productId);
    void updateProductAverageRating(Long productId);
}
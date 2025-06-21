package com.example.tomatomall.controller;

import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 评价控制器，提供商品评价相关的接口
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建新的商品评价
     * @param reviewVO 前端传入的评价视图对象
     * @return 创建成功的评价对象封装的响应
     */
    @PostMapping
    public Response<ReviewVO> createReview(@RequestBody ReviewVO reviewVO) {
        ReviewVO createdReview = reviewService.createReview(reviewVO);
        return Response.buildSuccess(createdReview);
    }

    /**
     * 根据商品ID获取该商品所有评价
     * @param productId 商品ID
     * @return 评价列表封装的响应
     */
    @GetMapping("/{productId}")
    public Response<List<ReviewVO>> getReviewsByProductId(@PathVariable("productId") long productId) {
        List<ReviewVO> reviews = reviewService.getReviewsByProductId(productId);
        return Response.buildSuccess(reviews);
    }
}
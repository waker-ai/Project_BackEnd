package com.example.tomatomall.controller;

import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Response<ReviewVO> createReview(@RequestBody ReviewVO reviewVO) {
        ReviewVO createdReview = reviewService.createReview(reviewVO);
        return Response.buildSuccess(createdReview);
    }

    @GetMapping("/{productId}")
    public Response<List<ReviewVO>> getReviewsByProductId(@PathVariable("productId") long productId) {
        List<ReviewVO> reviews = reviewService.getReviewsByProductId(productId);
        return Response.buildSuccess(reviews);
    }
}
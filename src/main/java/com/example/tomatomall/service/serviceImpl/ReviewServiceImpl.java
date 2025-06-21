package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.ReviewRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ReviewService;
import com.example.tomatomall.vo.ReviewVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.tomatomall.util.SecurityUtil;


import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewService 的实现类，负责商品评价的创建、查询及更新商品平均评分
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewRepository reviewRepository;


    /**
     * 创建商品评价
     * @param reviewVO 评价视图对象，包含评价内容、评分、订单及商品信息
     * @return 创建成功的评价视图对象
     */
    @Override
    public ReviewVO createReview(ReviewVO reviewVO) {
        Review review = new Review();
        User user = securityUtil.getCurrentUser();
        review.setOrderId(reviewVO.getOrderId());
        review.setProductId(reviewVO.getProductId());
        review.setUserId(user.getId());
        review.setRating(reviewVO.getRating());
        review.setComment(reviewVO.getComment());
        review.setPhotoUrl(reviewVO.getPhotoUrl());

//        logger.info("Order ID: {}", reviewVO.getOrderId());
//        logger.info("Product ID: {}", reviewVO.getProductId());
//        logger.info("User ID: {}", user.getId());
//        logger.info("Rating: {}", reviewVO.getRating());
//        logger.info("Comment: {}", reviewVO.getComment());
//        logger.info("PhotoUrl: {}", reviewVO.getPhotoUrl());
        reviewRepository.save(review);
        updateProductAverageRating(reviewVO.getProductId());

        // 更新订单项评价状态
        orderService.updateReviewStatus(review.getOrderId(), review.getProductId(), true);
        return reviewVO;
    }

    /**
     * 根据商品ID查询该商品的所有评价
     * @param productId 商品ID
     * @return 评价视图对象列表
     */
    @Override
    public List<ReviewVO> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        // 将 Review 实体列表转换为 ReviewVO 列表
        return reviews.stream()
                .map(Review::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 计算并更新指定商品的平均评分
     * @param productId 商品ID
     */
    @Override
    public void updateProductAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (!reviews.isEmpty()) {
            double sum = reviews.stream().mapToDouble(Review::getRating).sum();
            double averageRating = sum / reviews.size();

            Product product = productRepository.findById(productId).orElseThrow(TomatoException::productNotFound);
            product.setRate(averageRating);
            productRepository.save(product);
        }
    }

}
package com.example.tomatomall.service;

import com.example.tomatomall.vo.ReviewVO;

import java.util.List;

/**
 * 商品评价（评论）服务接口，用于管理用户对商品的评论及商品评分更新等功能。
 */
public interface ReviewService {

    /**
     * 创建一条新的商品评论。
     *
     * @param reviewVO 前端传来的评论视图对象，包含用户名、评论内容、评分、商品ID等
     * @return 创建成功的评论视图对象
     */
    ReviewVO createReview(ReviewVO reviewVO);

    /**
     * 根据商品ID获取该商品下的所有评论列表。
     *
     * @param productId 商品ID
     * @return 对应商品的评论视图对象列表
     */
    List<ReviewVO> getReviewsByProductId(Long productId);

    /**
     * 根据商品ID重新计算该商品的平均评分。
     * 通常在添加或修改评论后调用，确保商品评分实时更新。
     *
     * @param productId 商品ID
     */
    void updateProductAverageRating(Long productId);
}
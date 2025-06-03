package com.example.tomatomall.po;

import com.example.tomatomall.vo.ReviewVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_time")
    private Date createdTime;

    public ReviewVO toVO() {
        ReviewVO reviewVO = new ReviewVO();
        reviewVO.setId(id);
        reviewVO.setProductId(productId);
        reviewVO.setUserId(userId);
        reviewVO.setOrderId(orderId);
        reviewVO.setRating(rating);
        reviewVO.setComment(comment);
        reviewVO.setPhotoUrl(photoUrl);
        reviewVO.setCreateTime(createdTime);
        return reviewVO;
    }
}
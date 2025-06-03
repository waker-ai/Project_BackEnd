package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
public class ReviewVO {
    private Long id;
    private Long productId;
    private Long userId;
    private Long orderId;
    private Double rating;
    private String comment;
    private String photoUrl;
    private Date createTime;
}
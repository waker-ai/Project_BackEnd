package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class AdvertisementVO {
    private Integer id;
    private String title;
    private String content;
    private String imgUrl;
    private Integer productId;
}

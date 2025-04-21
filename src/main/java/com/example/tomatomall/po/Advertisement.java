package com.example.tomatomall.po;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "advertisements")
@Data
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "product_id", nullable = false)
    private Integer productId;
}

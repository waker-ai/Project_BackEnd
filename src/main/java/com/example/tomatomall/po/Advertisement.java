package com.example.tomatomall.po;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "advertisements")
@Data
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "img_url", nullable = false, length = 500)
    private String imgUrl;


}

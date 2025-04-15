package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "specifications")
@Data
@NoArgsConstructor
public class Specification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",nullable = false)
    private Long id;

    @Column(name="item",nullable = false, length = 50)
    private String item; // 规格名称

    @Column(name="value",nullable = false, length = 255)
    private String value; // 规格内容

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore // 关键！阻止 Jackson 序列化此字段
    private Product product;



}

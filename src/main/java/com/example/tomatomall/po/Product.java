package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.vo.SpecificationVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.tomatomall.po.Stockpile;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false,updatable = false)
    private Long id;//id

    @Column(name="category",length = 50)
    private String category;

    @Column(name ="title",nullable = false,length = 50)
    private String title;//商品名

    @Column(name="price",nullable = false,precision = 10,scale=2)
    private BigDecimal price;//价格

    @Column(name="rate",nullable = false)
    private double rate;//评分

    @Column(name="description",length = 255)
    private String description;//商品描述

    @Column(name="cover",length = 500)
    private String cover;//商品封面url

    @Column(name="detail",length = 500)
    private String detail;//商品详细说明

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true) // 关联 Specification
    private List<Specification> specifications;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Stockpile stockpile;  // 新增库存关联

    public ProductVO toVO(){
        ProductVO productVO = new ProductVO();
        productVO.setId(this.id);
        productVO.setCategory(this.category);
        productVO.setTitle(this.title);
        productVO.setPrice(this.price);
        productVO.setRate(this.rate);
        productVO.setDescription(this.description);
        productVO.setCover(this.cover);
        productVO.setDetail(this.detail);
        if (this.specifications != null && !this.specifications.isEmpty()) {
            List<SpecificationVO> specVOList = this.specifications.stream()
                    .map(spec -> {
                        SpecificationVO voSpec = new SpecificationVO();
                        voSpec.setItem(spec.getItem());
                        voSpec.setValue(spec.getValue());
                        return voSpec;
                    })
                    .collect(Collectors.toList());
            productVO.setSpecifications(specVOList);
        }
        return productVO;
    }

    public Stockpile getStockpile() {
        return stockpile;
    }

    public void setStockpile(Stockpile stockpile) {
        this.stockpile = stockpile;
    }


}

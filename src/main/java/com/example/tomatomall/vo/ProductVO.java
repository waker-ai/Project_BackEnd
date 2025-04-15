package com.example.tomatomall.vo;


import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Specification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ProductVO {
    private Long id; // 商品ID
    private String title; // 商品名称
    private BigDecimal price; // 商品价格
    private Double rate; // 商品评分
    private String description; // 商品描述
    private String cover; // 商品封面URL
    private String detail; // 商品详细说明
    private List<SpecificationVO>  specifications;
    private Integer amount;
    private Integer frozen;


    public Product toPO() {
        Product product = new Product();
        product.setId(this.id);
        product.setTitle(this.title);
        product.setPrice(this.price);
        product.setRate(this.rate);
        product.setDescription(this.description);
        product.setCover(this.cover);
        product.setDetail(this.detail);
        if (this.amount != null) {
            product.getStockpile().setAmount(this.amount);
        }
        if (this.frozen != null) {
            product.getStockpile().setFrozen(this.frozen);
        }
        if (this.specifications != null) {
            List<Specification> specificationList = this.specifications.stream()
                    .map(specVO -> {
                        Specification spec = new Specification();
                        spec.setItem(specVO.getItem());
                        spec.setValue(specVO.getValue());
                        // 这里你可以设置其他相关属性，如 product
                        return spec;
                    })
                    .collect(Collectors.toList());
            product.setSpecifications(specificationList);
        }
        return product;
    }

    public Integer getStockAmount(){
        return amount;
    }

    public void setStockAmount(Integer amount){
        this.amount=amount;
    }



}

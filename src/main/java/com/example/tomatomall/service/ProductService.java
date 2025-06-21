package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 商品服务接口，定义了商品管理相关的操作方法。
 */
public interface ProductService {

    /**
     * 获取所有商品的列表。
     * 可用于展示商品总览页。
     *
     * @return 包含所有商品信息的列表
     */
    List<ProductVO> getAllProducts();

    /**
     * 根据商品ID获取对应的商品信息。
     *
     * @param id 商品的唯一标识
     * @return 对应的商品视图对象 ProductVO，若不存在则返回 null
     */
    ProductVO getProduct(Long id);

    /**
     * 更新商品信息，包括基本信息、规格、库存等。
     *
     * @param productVO 前端传来的商品视图对象，包含更新内容
     * @return 更新后的商品视图对象 ProductVO
     */
    ProductVO updateProduct(ProductVO productVO);

    /**
     * 创建一个新的商品记录。
     *
     * @param productVO 商品视图对象，包含商品基本信息、规格、库存等
     * @return 创建成功后的商品视图对象
     */
    ProductVO createProduct(ProductVO productVO);

    /**
     * 根据商品ID删除指定商品。
     * 同时会删除购物车中与该商品相关的项。
     *
     * @param id 商品ID
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteProduct(Long id);

    /**
     * 按照销量降序分页获取商品列表。
     * 可用于“热销榜”页面。
     *
     * @param pageable 分页对象，包含页码、大小、排序方式等信息
     * @return 按销量排序的商品分页结果
     */
    Page<ProductVO> getProductsBySales(Pageable pageable);

    /**
     * 按照评分降序分页获取商品列表。
     * 可用于“好评榜”页面。
     *
     * @param pageable 分页对象
     * @return 按评分排序的商品分页结果
     */
    Page<ProductVO> getProductsByScore(Pageable pageable);

}

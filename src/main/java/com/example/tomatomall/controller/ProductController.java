package com.example.tomatomall.controller;


import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.service.StockpileService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * 商品控制器，提供商品及库存相关的REST API接口
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    //获取商品详情
    @Autowired
    private ProductService productService;

    @Autowired
    private StockpileService stockpileService;

    /**
     * 获取所有商品信息列表
     * @return 商品视图列表封装的响应对象
     */
    @GetMapping
    public Response<List<ProductVO>> getAllProducts() {
        return Response.buildSuccess(productService.getAllProducts());
    }

    /**
     * 获取指定ID的商品详情
     * @param id 商品ID
     * @return 商品视图封装的响应对象，若商品不存在返回404错误
     */
    @GetMapping("/{id}")
    public Response<ProductVO> getproduct(@PathVariable Long id){
        ProductVO product = productService.getProduct(id);
        if(product == null){
            return Response.buildFailure("Product not found", "404");
        }
        return Response.buildSuccess(productService.getProduct(id));
    }

    /**
     * 更新商品信息
     * @param productVO 前端传入的商品视图对象，必须包含ID
     * @return 更新后的商品信息或错误信息
     */
    @PutMapping
    public Response updateProduct(@RequestBody ProductVO productVO) {
        if(productVO==null || productVO.getId()==null){
            return Response.buildFailure("Product not found", "404");
        }
        ProductVO product = productService.updateProduct(productVO);
        if(product == null){
            return Response.buildFailure("Product not found", "404");
        }
        return Response.buildSuccess(product);
    }

    /**
     * 新建商品
     * @param productVO 新商品信息
     * @return 创建成功的商品视图
     */
    @PostMapping
    public Response createProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.createProduct(productVO));
    }

    /**
     * 删除指定ID的商品
     * @param id 商品ID
     * @return 删除结果提示
     */
    @DeleteMapping("/{id}")
    public Response deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return Response.buildSuccess("删除成功");
        }
        return Response.buildFailure("商品不存在", "400");
    }

    /**
     * 调整指定商品的库存数量（增加或减少）
     * @param productId 商品ID
     * @param amount 要调整的数量，正数为增加，负数为减少
     * @return 调整结果提示
     */
    @PatchMapping("/stockpile/{productId}")
    public Response adjustStockpile(@PathVariable Long productId, @RequestParam Integer amount) {
        Optional<Stockpile> stockpileOptional = stockpileService.adjustStockpile(productId, amount);
        if (stockpileOptional.isPresent()) {
            Stockpile stockpile=stockpileOptional.get();
            return Response.buildSuccess("调整库存成功");
        }
        return Response.buildFailure("商品不存在", "400");
    }

    /**
     * 获取指定商品的库存信息
     * @param productId 商品ID
     * @return 库存信息或404错误
     */
    @GetMapping("/stockpile/{productId}")
    public Response getStockpileByProductId(@PathVariable Long productId) {
        Optional<Stockpile> stockpile = stockpileService.getStockpile(productId);
        if (stockpile.isPresent()) {
            return Response.buildSuccess(stockpile.get());
        }
        return Response.buildFailure("商品库存信息不存在", "404");
    }

    /**
     * 按销量分页获取商品列表
     * @param page 页码，默认0
     * @param size 每页大小，默认10
     * @return 按销量排序的商品分页数据
     */
    @GetMapping("/by-sales")
    public Response<Page<ProductVO>> getProductBySales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<ProductVO> products = productService.getProductsBySales(PageRequest.of(page,size));
        return Response.buildSuccess(products);
    }

    /**
     * 按评分分页获取商品列表
     * @param page 页码，默认0
     * @param size 每页大小，默认10
     * @return 按评分排序的商品分页数据
     */
    @GetMapping("/by-score")
    public Response<Page<ProductVO>> getProductByScore(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size){
        Page<ProductVO> products = productService.getProductsByScore(PageRequest.of(page,size));
        return Response.buildSuccess(products);
    }




}

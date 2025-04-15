package com.example.tomatomall.controller;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.service.StockpileService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    //获取商品详情
    @Autowired
    private ProductService productService;

    @Autowired
    private StockpileService stockpileService;

    //获取商品列表
    @GetMapping
    public Response<List<ProductVO>> getAllProducts() {
        return Response.buildSuccess(productService.getAllProducts());
    }

    //获取指定商品信息
    @GetMapping("/{id}")
    public Response<ProductVO> getproduct(@PathVariable Long id){
        ProductVO product = productService.getProduct(id);
        if(product == null){
            return Response.buildFailure("Product not found", "404");
        }
        return Response.buildSuccess(productService.getProduct(id));
    }

    //更新商品信息
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

    @PostMapping
    public Response createProduct(@RequestBody ProductVO productVO) {
        return Response.buildSuccess(productService.createProduct(productVO));
    }

    //删除商品
    @DeleteMapping("/{id}")
    public Response deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return Response.buildSuccess("删除成功");
        }
        return Response.buildFailure("商品不存在", "400");
    }

    //调整指定商品的库存
    @PatchMapping("/stockpile/{productId}")
    public Response adjustStockpile(@PathVariable Long productId, @RequestParam Integer amount) {
        Optional<Stockpile> stockpileOptional = stockpileService.adjustStockpile(productId, amount);
        if (stockpileOptional.isPresent()) {
            Stockpile stockpile=stockpileOptional.get();
            return Response.buildSuccess("调整库存成功");
        }
        return Response.buildFailure("商品不存在", "400");
    }

    //获取指定商品的库存信息
    @GetMapping("/stockpile/{productId}")
    public Response getStockpileByProductId(@PathVariable Long productId) {
        Optional<Stockpile> stockpile = stockpileService.getStockpile(productId);
        if (stockpile.isPresent()) {
            return Response.buildSuccess(stockpile.get());
        }
        return Response.buildFailure("商品库存信息不存在", "404");
    }




}

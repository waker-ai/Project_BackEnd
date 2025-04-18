package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.SpecificationRepository;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.repository.ProductRepository;
<<<<<<< HEAD
import com.example.tomatomall.repository.StockpileRepository;
=======
import com.example.tomatomall.service.OssService;
>>>>>>> ee2e90e4d4c9dd73a325a8c6ce07a630a84261af
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import javax.transaction.Transactional;
=======
import java.io.IOException;
>>>>>>> ee2e90e4d4c9dd73a325a8c6ce07a630a84261af
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    private OssService ossService;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private StockpileRepository stockpileRepository;
    @Autowired
    private CartRepository cartRepository;

    @Override
    public List<ProductVO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            ProductVO productVO = product.toVO();

            // 查询库存信息并设置到 VO 中
            Optional<Stockpile> stockpileOpt = stockpileRepository.findByProductId(product.getId());
            stockpileOpt.ifPresent(stockpile -> {
                productVO.setAmount(stockpile.getAmount());
                productVO.setFrozen(stockpile.getFrozen());
                productVO.setStockAmount(stockpile.getStockAmount());
            });

            return productVO;
        }).collect(Collectors.toList());
    }

    @Override
    public ProductVO getProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            ProductVO productVO = product.toVO();

            // 查询库存信息并设置到 VO 中
            Optional<Stockpile> stockpileOpt = stockpileRepository.findByProductId(id);
            stockpileOpt.ifPresent(stockpile -> {
                productVO.setAmount(stockpile.getAmount());
                productVO.setFrozen(stockpile.getFrozen());
                productVO.setStockAmount(stockpile.getStockAmount());
            });
            return productVO;
        }
        return null;
    }

    @Override
    public ProductVO updateProduct(ProductVO productVO) {
        Product existproduct = productRepository.findById(productVO.getId()).orElse(null);
        if (existproduct != null) {
            // 更新产品信息
            existproduct.setTitle(productVO.getTitle());
            existproduct.setPrice(productVO.getPrice());
            existproduct.setRate(productVO.getRate());
            existproduct.setDescription(productVO.getDescription());
            existproduct.setCover(productVO.getCover());
            existproduct.setDetail(productVO.getDetail());
<<<<<<< HEAD
            if (productVO.getSpecifications() != null) {
                // 先删除原有的规格，防止重复数据
                specificationRepository.deleteByProductId(existproduct.getId());

                // 转换 SpecificationVO 到 Specification，并设置关联关系
                List<Specification> specifications = productVO.getSpecifications().stream()
                        .map(specVO -> {
                            Specification spec = new Specification();
                            spec.setItem(specVO.getItem());
                            spec.setValue(specVO.getValue());
                            spec.setProduct(existproduct); // 设置关联关系
                            return spec;
                        })
                        .collect(Collectors.toList());

                // 保存更新后的规格
                specificationRepository.saveAll(specifications);
=======
            existproduct.setSpecifications(productVO.getSpecifications());

            if (productVO.getCover() != null && !productVO.getCover().isEmpty()) {
                try {
                    String coverUrl = uploadCover(productVO.getCover());
                    existproduct.setCover(coverUrl);
                } catch (IOException e) {
                    // 处理上传失败的情况，可以设置一个默认头像或者记录日志
                    existproduct.setCover(null);
                }
>>>>>>> ee2e90e4d4c9dd73a325a8c6ce07a630a84261af
            }
            // 保存更新后的产品信息
            Product updatedProduct = productRepository.save(existproduct);
            return updatedProduct.toVO();
        }
        return null;
    }

    @Override
    public ProductVO createProduct(ProductVO productVO) {
        Product product = new Product();

        // 处理可能为空的字段，避免 NullPointerException
        product.setTitle(Optional.ofNullable(productVO.getTitle()).orElse("默认标题"));
        product.setPrice(Optional.ofNullable(productVO.getPrice()).orElse(BigDecimal.ZERO));  // 默认价格为 0
        product.setRate(Optional.ofNullable(productVO.getRate()).orElse(0.0)); // 默认评分 0.0
        product.setDescription(Optional.ofNullable(productVO.getDescription()).orElse("暂无描述"));
        product.setDetail(Optional.ofNullable(productVO.getDetail()).orElse("暂无详情"));

        Stockpile stockpile=new Stockpile();
        stockpile.setAmount(productVO.getStockAmount());
        stockpile.setFrozen(0);

<<<<<<< HEAD
        Product savedProduct = productRepository.save(product);

        stockpile.setProduct(savedProduct);
        stockpileRepository.save(stockpile);


        // ✅ 第二步：保存规格 Specification
        if (productVO.getSpecifications() != null) {
            List<Specification> specifications = productVO.getSpecifications().stream()
                    .map(specVO -> {
                        Specification spec = new Specification();
                        spec.setItem(specVO.getItem());
                        spec.setValue(specVO.getValue());
                        spec.setProduct(savedProduct); // 设置关联关系
                        return spec;
                    })
                    .collect(Collectors.toList());

            specificationRepository.saveAll(specifications);
        }
        Product fullProduct=productRepository.findById(savedProduct.getId()).orElse(savedProduct);
        return fullProduct.toVO();
    }

    @Transactional
    public void adjustStock(Long productId, Integer amount) {
        Stockpile stockpile = stockpileRepository.findByProductId(productId).orElse(null);
        if (stockpile == null) {
            stockpile = new Stockpile();
            stockpile.setProduct(productRepository.getReferenceById(productId));
        }
        stockpile.setAmount(amount);
        stockpileRepository.save(stockpile);
=======
        if (productVO.getCover() != null && !productVO.getCover().isEmpty()) {
            try {
                String coverUrl = uploadCover(productVO.getCover());
                product.setCover(coverUrl);
            } catch (IOException e) {
                // 处理上传失败的情况，可以设置一个默认头像或者记录日志
                product.setCover(null);
            }
        }
        productRepository.save(product);
        return product.toVO();
>>>>>>> ee2e90e4d4c9dd73a325a8c6ce07a630a84261af
    }

    private String uploadCover(String base64Image) throws IOException {
        // 解码Base64字符串
        byte[] imageBytes = Base64.getDecoder().decode(base64Image.split(",")[1]);
        // 使用OssService上传图片
        return ossService.uploadFile(imageBytes, "cover_" + System.currentTimeMillis() + ".png");
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            productRepository.deleteById(id);
            cartRepository.deleteByProductId(id);
            return true;
        }
        return false;
    }


}

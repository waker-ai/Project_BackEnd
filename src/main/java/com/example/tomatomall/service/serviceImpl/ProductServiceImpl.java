package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.SpecificationRepository;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private StockpileRepository stockpileRepository;

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
        product.setCover(Optional.ofNullable(productVO.getCover()).orElse("1")); // 默认封面为空字符串
        product.setDetail(Optional.ofNullable(productVO.getDetail()).orElse("暂无详情"));

        Stockpile stockpile=new Stockpile();
        stockpile.setAmount(productVO.getStockAmount());
        stockpile.setFrozen(0);

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
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }


}

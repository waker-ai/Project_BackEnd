package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.SpecificationRepository;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Transactional
public ProductVO updateProduct(ProductVO productVO) {
    Product existproduct = productRepository.findById(productVO.getId()).orElse(null);
    if (existproduct != null) {
        // 更新产品基本信息
        existproduct.setCategory(productVO.getCategory());
        existproduct.setTitle(productVO.getTitle());
        existproduct.setPrice(productVO.getPrice());
        existproduct.setRate(productVO.getRate());
        existproduct.setDescription(productVO.getDescription());
        existproduct.setCover(productVO.getCover());
        existproduct.setDetail(productVO.getDetail());

        if (productVO.getSpecifications() != null) {
            // 删除原有规格
            specificationRepository.deleteByProductId(existproduct.getId());

            // 清空原有集合，防止残留已删除的 Specification
            if (existproduct.getSpecifications() != null) {
                existproduct.getSpecifications().clear();
            } else {
                existproduct.setSpecifications(new ArrayList<>());
            }

            // 转换并创建新的 Specification 列表
            List<Specification> specifications = productVO.getSpecifications().stream()
                    .map(specVO -> {
                        Specification spec = new Specification();
                        spec.setItem(specVO.getItem());
                        spec.setValue(specVO.getValue());
                        spec.setProduct(existproduct); // 设置关联关系
                        return spec;
                    })
                    .collect(Collectors.toList());

            // 保存新的规格
            specificationRepository.saveAll(specifications);

            // 将新规格添加到 Product 的集合中
            existproduct.getSpecifications().addAll(specifications);
        }

        // 保存更新后的产品
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
        product.setCategory(Optional.ofNullable(productVO.getCategory()).orElse("默认分类"));
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
            cartRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }


    @Override
    public Page<ProductVO> getProductsBySales(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByOrderBySalesDesc(pageable);
        return productPage.map(Product::toVO);
    }
    public Page<ProductVO> getProductsByScore(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByOrderByRateDesc(pageable);
        return productPage.map(Product::toVO);
    }
}

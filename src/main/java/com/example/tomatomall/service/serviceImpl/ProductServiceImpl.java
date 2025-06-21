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

/**
 * 商品服务实现类，实现对商品的增删改查及库存管理等操作
 */
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

    /**
     * 获取所有商品及其库存信息
     * @return 商品视图对象列表
     */
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

    /**
     * 根据商品ID获取单个商品详细信息及库存
     * @param id 商品ID
     * @return 商品视图对象
     */
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

    /**
     * 更新商品信息及规格
     * @param productVO 商品视图对象
     * @return 更新后的商品视图对象，若商品不存在返回null
     */
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

    /**
     * 新增商品并保存库存及规格信息
     * @param productVO 商品视图对象
     * @return 新增后的商品视图对象
     */
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

    /**
     * 调整指定商品的库存数量
     * @param productId 商品ID
     * @param amount 新库存数量
     */
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

    /**
     * 根据商品ID删除商品及其购物车记录
     * @param id 商品ID
     * @return 删除成功返回true，否则返回false
     */
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

    /**
     * 分页获取按销量排序的商品列表
     * @param pageable 分页参数
     * @return 商品视图对象分页
     */
    @Override
    public Page<ProductVO> getProductsBySales(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByOrderBySalesDesc(pageable);
        return productPage.map(Product::toVO);
    }

    /**
     * 分页获取按评分排序的商品列表
     * @param pageable 分页参数
     * @return 商品视图对象分页
     */
    public Page<ProductVO> getProductsByScore(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByOrderByRateDesc(pageable);
        return productPage.map(Product::toVO);
    }
}

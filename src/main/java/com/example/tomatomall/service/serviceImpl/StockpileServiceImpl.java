package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.StockpileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 库存服务实现类，负责库存的增减和查询操作
 */
@Service
public class StockpileServiceImpl implements StockpileService {

    @Autowired
    private StockpileRepository stockpileRepository;

    /**
     * 调整指定商品的库存数量
     * @param productId 商品ID
     * @param amount 新的库存数量
     * @return 返回更新后的库存Optional，如果库存不存在则返回Optional.empty()
     */
    @Override
    public Optional<Stockpile> adjustStockpile(Long productId, Integer amount) {
        Optional<Stockpile> stockpileOptional = stockpileRepository.findByProductId(productId);
        if (stockpileOptional.isPresent()) {
            Stockpile stockpile = stockpileOptional.get();
            stockpile.setAmount(amount);
            return Optional.of(stockpileRepository.save(stockpile));
        }
        return Optional.empty();
    }

    /**
     * 根据商品ID查询库存信息
     * @param productId 商品ID
     * @return 库存信息的Optional，如果不存在则为空
     */
    @Override
    public Optional<Stockpile> getStockpile(Long productId) {
        // 实现具体逻辑，返回 Optional<Stockpile>
        return stockpileRepository.findByProductId(productId);
    }
}

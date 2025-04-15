package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.StockpileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StockpileServiceImpl implements StockpileService {

    @Autowired
    private StockpileRepository stockpileRepository;

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

    @Override
    public Optional<Stockpile> getStockpile(Long productId) {
        // 实现具体逻辑，返回 Optional<Stockpile>
        return stockpileRepository.findByProductId(productId);
    }
}

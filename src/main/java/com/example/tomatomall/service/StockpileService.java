package com.example.tomatomall.service;

import com.example.tomatomall.po.Stockpile;

import java.util.Optional;

public interface StockpileService {
    Optional<Stockpile> adjustStockpile(Long productId, Integer amount);

    Optional<Stockpile> getStockpile(Long productId);
}
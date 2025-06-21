package com.example.tomatomall.service;

import com.example.tomatomall.po.Stockpile;

import java.util.Optional;

/**
 * 库存管理服务接口。
 * 提供对商品库存（Stockpile）的读取和调整操作。
 */
public interface StockpileService {
    /**
     * 调整指定商品的库存数量。
     * 如果该商品尚未存在库存记录，可以选择创建或返回空。
     *
     * @param productId 商品的唯一标识 ID
     * @param amount    要设置的新库存数量（不是增减量，是最终数量）
     * @return 包含更新后库存对象的 Optional，如果库存不存在则返回 Optional.empty()
     */
    Optional<Stockpile> adjustStockpile(Long productId, Integer amount);

    /**
     * 获取指定商品的库存信息。
     *
     * @param productId 商品的唯一标识 ID
     * @return 对应库存对象的 Optional，若无记录则返回 Optional.empty()
     */
    Optional<Stockpile> getStockpile(Long productId);
}
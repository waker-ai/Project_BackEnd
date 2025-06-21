package com.example.tomatomall.service;

import com.example.tomatomall.vo.AdvertisementVO;

import java.util.List;

/**
 * 广告服务接口，定义广告相关的业务操作
 */
public interface AdvertisementService {

    /**
     * 获取所有广告列表
     * @return 广告视图对象列表
     */
    List<AdvertisementVO> getAllAdvertisements();

    /**
     * 创建新的广告
     * @param vo 广告视图对象，包含广告信息
     * @return 创建成功后的广告视图对象
     */
    AdvertisementVO createAdvertisement(AdvertisementVO vo);

    /**
     * 更新广告信息
     * @param vo 广告视图对象，必须包含广告ID及需更新的信息
     * @return 更新后的广告视图对象
     * @throws RuntimeException 如果广告ID为空或广告不存在，抛出异常
     */
    AdvertisementVO updateAdvertisement(AdvertisementVO vo);

    /**
     * 删除指定ID的广告
     * @param id 广告ID
     * @throws RuntimeException 如果广告不存在，抛出异常
     */
    void deleteAdvertisement(Integer id);
}

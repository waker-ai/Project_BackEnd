package com.example.tomatomall.service;

import com.example.tomatomall.vo.AdvertisementVO;

import java.util.List;

public interface AdvertisementService {
    List<AdvertisementVO> getAllAdvertisements();
    AdvertisementVO createAdvertisement(AdvertisementVO vo);
    AdvertisementVO updateAdvertisement(AdvertisementVO vo);
    void deleteAdvertisement(Integer id);
}

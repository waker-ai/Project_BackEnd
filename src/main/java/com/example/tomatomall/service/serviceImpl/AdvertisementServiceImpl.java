package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Override
    public List<AdvertisementVO> getAllAdvertisements() {
        return advertisementRepository.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO vo) {
        Advertisement ad = toEntity(vo);
        Advertisement saved = advertisementRepository.save(ad);
        return toVO(saved);
    }

    @Override
    public AdvertisementVO updateAdvertisement(AdvertisementVO vo) {
        if (vo.getId() == null) {
            throw new RuntimeException("广告ID不能为空");
        }
        Advertisement existing = advertisementRepository.findById(vo.getId())
                .orElseThrow(() -> new RuntimeException("广告不存在"));
        BeanUtils.copyProperties(vo, existing, "id"); // 保持ID不变
        existing.setImageUrl(vo.getImgUrl()); // imgUrl需要单独处理
        Advertisement updated = advertisementRepository.save(existing);
        return toVO(updated);
    }

    @Override
    public void deleteAdvertisement(Integer id) {
        if (!advertisementRepository.existsById(id)) {
            throw new RuntimeException("广告不存在");
        }
        advertisementRepository.deleteById(id);
    }

    private AdvertisementVO toVO(Advertisement ad) {
        AdvertisementVO vo = new AdvertisementVO();
        BeanUtils.copyProperties(ad, vo);
        vo.setImgUrl(ad.getImageUrl());
        return vo;
    }

    private Advertisement toEntity(AdvertisementVO vo) {
        Advertisement ad = new Advertisement();
        BeanUtils.copyProperties(vo, ad);
        ad.setImageUrl(vo.getImgUrl());
        return ad;
    }
}

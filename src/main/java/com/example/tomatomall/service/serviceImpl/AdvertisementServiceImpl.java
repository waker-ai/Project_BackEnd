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

/**
 * 广告服务实现类
 * 提供广告的增删改查功能
 */
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    /**
     * 查询所有广告信息
     * @return 广告视图对象列表
     */
    @Override
    public List<AdvertisementVO> getAllAdvertisements() {
        return advertisementRepository.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 创建新的广告
     * @param vo 广告视图对象
     * @return 保存后的广告视图对象
     */
    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO vo) {
        Advertisement ad = toEntity(vo);
        Advertisement saved = advertisementRepository.save(ad);
        return toVO(saved);
    }

    /**
     * 更新已有广告信息
     * @param vo 包含更新数据的广告视图对象，必须包含ID
     * @return 更新后的广告视图对象
     * @throws RuntimeException 如果广告ID为空或广告不存在，抛出异常
     */
    @Override
    public AdvertisementVO updateAdvertisement(AdvertisementVO vo) {
        if (vo.getId() == null) {
            throw new RuntimeException("广告ID不能为空");
        }
        Advertisement existing = advertisementRepository.findById(vo.getId())
                .orElseThrow(() -> new RuntimeException("广告不存在"));
        BeanUtils.copyProperties(vo, existing, "id"); // 保持ID不变
        existing.setImgUrl(vo.getImgUrl()); // imgUrl需要单独处理
        Advertisement updated = advertisementRepository.save(existing);
        return toVO(updated);
    }

    /**
     * 根据ID删除广告
     * @param id 广告ID
     * @throws RuntimeException 如果广告不存在，抛出异常
     */
    @Override
    public void deleteAdvertisement(Integer id) {
        if (!advertisementRepository.existsById(id)) {
            throw new RuntimeException("广告不存在");
        }
        advertisementRepository.deleteById(id);
    }

    /**
     * 将广告实体转换为视图对象
     * @param ad 广告实体
     * @return 广告视图对象
     */
    private AdvertisementVO toVO(Advertisement ad) {
        AdvertisementVO vo = new AdvertisementVO();
        BeanUtils.copyProperties(ad, vo);
        vo.setImgUrl(ad.getImgUrl());
        return vo;
    }

    /**
     * 将广告视图对象转换为实体对象
     * @param vo 广告视图对象
     * @return 广告实体
     */
    private Advertisement toEntity(AdvertisementVO vo) {
        Advertisement ad = new Advertisement();
        BeanUtils.copyProperties(vo, ad);
        ad.setImgUrl(vo.getImgUrl());
        return ad;
    }
}

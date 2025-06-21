package com.example.tomatomall.controller;

import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告管理控制器
 * 提供广告的增删改查接口
 */
@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    /**
     * 获取所有广告列表
     * @return 广告视图对象列表
     */
    @GetMapping
    public List<AdvertisementVO> getAllAdvertisements() {
        return advertisementService.getAllAdvertisements();
    }

    /**
     * 新建广告
     * @param vo 广告视图对象
     * @return 创建成功后的广告视图对象
     */
    @PostMapping
    public AdvertisementVO createAdvertisement(@RequestBody AdvertisementVO vo) {
        return advertisementService.createAdvertisement(vo);
    }

    /**
     * 更新广告信息
     * @param vo 广告视图对象，包含广告ID及要更新的内容
     * @return 返回操作结果提示字符串
     */
    @PutMapping
    public String updateAdvertisement(@RequestBody AdvertisementVO vo) {
        advertisementService.updateAdvertisement(vo);
        return "更新成功";
    }

    /**
     * 删除指定ID的广告
     * @param id 广告ID
     * @return 返回操作结果提示字符串
     */
    @DeleteMapping("/{id}")
    public String deleteAdvertisement(@PathVariable Integer id) {
        advertisementService.deleteAdvertisement(id);
        return "删除成功";
    }
}

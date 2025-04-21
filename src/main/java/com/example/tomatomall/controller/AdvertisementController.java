package com.example.tomatomall.controller;

import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping
    public List<AdvertisementVO> getAllAdvertisements() {
        return advertisementService.getAllAdvertisements();
    }

    @PostMapping
    public AdvertisementVO createAdvertisement(@RequestBody AdvertisementVO vo) {
        return advertisementService.createAdvertisement(vo);
    }

    @PutMapping
    public String updateAdvertisement(@RequestBody AdvertisementVO vo) {
        advertisementService.updateAdvertisement(vo);
        return "更新成功";
    }

    @DeleteMapping("/{id}")
    public String deleteAdvertisement(@PathVariable Integer id) {
        advertisementService.deleteAdvertisement(id);
        return "删除成功";
    }
}

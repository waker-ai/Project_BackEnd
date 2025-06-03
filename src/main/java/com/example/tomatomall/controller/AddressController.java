package com.example.tomatomall.controller;

import com.example.tomatomall.po.Address;
import com.example.tomatomall.service.AddressService;
import com.example.tomatomall.vo.AddressVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Response<List<AddressVO>> getUserAddresses(@RequestParam(required = false) String username) {
        if (username == null || username.isEmpty()) {
            return Response.buildFailure("username is required", "400");
        }
        return Response.buildSuccess(addressService.getUserAddresses(username));
    }

    @PostMapping
    public Response<AddressVO> addAddress(@RequestBody AddressVO addressVO) {
        if(addressVO != null){
            return Response.buildSuccess(addressService.addAddress(addressVO));
        }
        return Response.buildFailure("address not found", "404");
    }

    //  修改收货地址
    @PutMapping("/{id}")
    public Response<AddressVO> updateAddress(@PathVariable Long id, @RequestBody AddressVO addressVO) {
        if (addressVO != null) {
            return Response.buildSuccess(addressService.updateAddress(id, addressVO));
        }
        return Response.buildFailure("address update failed", "400");
    }

    //  删除收货地址
    @DeleteMapping("/{id}")
    public Response deleteAddress(@PathVariable Long id) {
        if (addressService.deleteAddress(id)) {
            return Response.buildSuccess("删除成功");
        }
        return Response.buildFailure("address delete failed", "400");
    }


    // 设置默认地址
    @PutMapping("/{id}/default")
    public Response setDefaultAddress(@PathVariable Long id, @RequestParam String username) {
        if (username != null && id != null) {
            return Response.buildSuccess(addressService.setDefaultAddress(username, id));
        }
        return Response.buildFailure("set default address failed", "400");
    }

    //  查询默认地址
    @GetMapping("/default")
    public Response<AddressVO> getDefaultAddress(@RequestParam String username) {
        if (username != null) {
            return Response.buildSuccess(addressService.getDefaultAddress(username));
        }
        return Response.buildFailure("default address not found", "404");
    }


}

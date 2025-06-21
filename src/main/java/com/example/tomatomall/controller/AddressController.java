package com.example.tomatomall.controller;

import com.example.tomatomall.service.AddressService;
import com.example.tomatomall.vo.AddressVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 * 提供增删改查及默认地址设置相关接口
 */
@RestController
@RequestMapping("api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取指定用户名的所有收货地址列表
     * @param username 用户名，必传参数
     * @return 包含地址列表的响应体，失败时返回错误信息
     */
    @GetMapping
    public Response<List<AddressVO>> getUserAddresses(@RequestParam(required = false) String username) {
        if (username == null || username.isEmpty()) {
            return Response.buildFailure("username is required", "400");
        }
        return Response.buildSuccess(addressService.getUserAddresses(username));
    }

    /**
     * 新增收货地址
     * @param addressVO 收货地址视图对象
     * @return 新增成功时返回新增后的地址信息，否则返回失败信息
     */
    @PostMapping
    public Response<AddressVO> addAddress(@RequestBody AddressVO addressVO) {
        if(addressVO != null){
            return Response.buildSuccess(addressService.addAddress(addressVO));
        }
        return Response.buildFailure("address not found", "404");
    }

    /**
     * 更新指定ID的收货地址
     * @param id 地址ID
     * @param addressVO 更新后的收货地址信息
     * @return 更新成功时返回更新后的地址信息，否则返回失败信息
     */
    @PutMapping("/{id}")
    public Response<AddressVO> updateAddress(@PathVariable Long id, @RequestBody AddressVO addressVO) {
        if (addressVO != null) {
            return Response.buildSuccess(addressService.updateAddress(id, addressVO));
        }
        return Response.buildFailure("address update failed", "400");
    }

    /**
     * 删除指定ID的收货地址
     * @param id 地址ID
     * @return 删除成功返回成功信息，否则返回失败信息
     */
    @DeleteMapping("/{id}")
    public Response deleteAddress(@PathVariable Long id) {
        if (addressService.deleteAddress(id)) {
            return Response.buildSuccess("删除成功");
        }
        return Response.buildFailure("address delete failed", "400");
    }


    /**
     * 设置指定用户名的默认收货地址
     * @param id 地址ID
     * @param username 用户名
     * @return 设置成功返回更新后的默认地址信息，否则返回失败信息
     */
    @PutMapping("/{id}/default")
    public Response setDefaultAddress(@PathVariable Long id, @RequestParam String username) {
        if (username != null && id != null) {
            return Response.buildSuccess(addressService.setDefaultAddress(username, id));
        }
        return Response.buildFailure("set default address failed", "400");
    }

    /**
     * 获取指定用户名的默认收货地址
     * @param username 用户名
     * @return 默认地址信息，若不存在则返回失败信息
     */
    @GetMapping("/default")
    public Response<AddressVO> getDefaultAddress(@RequestParam String username) {
        if (username != null) {
            return Response.buildSuccess(addressService.getDefaultAddress(username));
        }
        return Response.buildFailure("default address not found", "404");
    }


}

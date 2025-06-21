package com.example.tomatomall.service;

import com.example.tomatomall.vo.AddressVO;
import com.example.tomatomall.vo.Response;

import java.util.List;

/**
 * 地址服务接口，定义用户地址相关的操作
 */
public interface AddressService {
    /**
     * 添加新的收货地址
     * @param addressVO 地址信息对象
     * @return 保存后的地址对象，包含数据库生成的ID等信息
     */
    AddressVO addAddress(AddressVO addressVO);

    /**
     * 根据地址ID更新收货地址信息
     * @param id 地址ID
     * @param addressVO 需要更新的地址信息对象
     * @return 更新后的地址对象，如果地址ID不存在返回null
     */
    AddressVO updateAddress(Long id, AddressVO addressVO);

    /**
     * 删除指定ID的收货地址
     * @param id 地址ID
     * @return 删除成功返回true，失败（如地址不存在）返回false
     */
    boolean deleteAddress(Long id);

    /**
     * 查询指定用户名的所有收货地址
     * @param username 用户名
     * @return 地址列表，若无地址返回空列表
     */
    List<AddressVO> getUserAddresses(String username);

    /**
     * 设置指定用户名的某个地址为默认地址
     * @param username 用户名
     * @param id 地址ID
     * @return 设置成功返回true，失败返回false（如地址不属于该用户）
     */
    boolean setDefaultAddress(String username, Long id);

    /**
     * 获取指定用户名的默认收货地址
     * @param username 用户名
     * @return 默认地址对象，若不存在返回null
     */
    AddressVO getDefaultAddress(String username);
}

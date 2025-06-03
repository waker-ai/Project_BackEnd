package com.example.tomatomall.service;

import com.example.tomatomall.vo.AddressVO;
import com.example.tomatomall.vo.Response;

import java.util.List;

public interface AddressService {
    AddressVO addAddress(AddressVO addressVO);

    AddressVO updateAddress(Long id, AddressVO addressVO);

    boolean deleteAddress(Long id);

    List<AddressVO> getUserAddresses(String username);

    boolean setDefaultAddress(String username, Long id);

    AddressVO getDefaultAddress(String username);
}

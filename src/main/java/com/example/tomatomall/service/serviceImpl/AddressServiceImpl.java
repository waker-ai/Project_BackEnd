package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Address;
import com.example.tomatomall.repository.AddressRepository;
import com.example.tomatomall.service.AddressService;
import com.example.tomatomall.vo.AddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<AddressVO> getUserAddresses(String username) {
        List<Address> addresses = addressRepository.findAllByUsername(username);
        return addresses.stream().map(Address::toVO).collect(Collectors.toList());
    }


    @Override
    public AddressVO addAddress(AddressVO addressVO) {

        List<Address> existing = addressRepository.findAllByUsername(addressVO.getUsername());
        Address address = addressVO.toPO();
        if (existing.isEmpty()) {
            // 第一个地址，强制设为默认
            address.setDefaultAddress(true);
        } else {
            // 不是第一个地址，强制设为非默认
            address.setDefaultAddress(false);
        }

        Address saved = addressRepository.save(address);
        return saved.toVO();
    }

    @Override
    public AddressVO updateAddress(Long id, AddressVO addressVO) {
        Optional<Address> optionalAddress = addressRepository.findById(id);
        if (!optionalAddress.isPresent()) {
            return null;
        }
        Address address = optionalAddress.get();
        // 更新字段
        address.setAddresseeName(addressVO.getAddresseeName());
        address.setPhone(addressVO.getPhone());
        address.setDeliveryAddress(addressVO.getDeliveryAddress());
        address.setPostalCode(addressVO.getPostalCode());
//        if (Boolean.TRUE.equals(addressVO.getDefaultAddress())) {
//            addressRepository.resetDefaultAddress(address.getUsername());
//            address.setDefaultAddress(true);
//        }

        Address updated = addressRepository.save(address);
        return updated.toVO();
    }

    @Override
    public boolean deleteAddress(Long id) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null) {
            addressRepository.deleteById(id);
            return true;
        }
        return false;
    }




    @Transactional
    @Override
    public boolean setDefaultAddress(String username, Long id) {
        Optional<Address> optionalAddress = addressRepository.findById(id);
        if (!optionalAddress.isPresent() || !username.equals(optionalAddress.get().getUsername())) {
            return false;
        }
        addressRepository.resetDefaultAddress(username); // 批量设置为非默认
        Address address = optionalAddress.get();
        address.setDefaultAddress(true);
        addressRepository.save(address); // 设置当前地址为默认
        return true;
    }

    @Override
    public AddressVO getDefaultAddress(String username) {
        Address address = addressRepository.findFirstByUsernameAndDefaultAddressTrue(username);
        if (address == null) {
            return null;
        }
        return address.toVO();
    }
}

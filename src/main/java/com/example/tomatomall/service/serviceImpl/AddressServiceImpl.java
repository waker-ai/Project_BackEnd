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
/**
 * 收货地址服务实现类
 * 实现了地址的增删改查、默认地址设置等功能
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;
    /**
     * 根据用户名查询该用户所有收货地址
     * @param username 用户名
     * @return 地址视图列表
     */
    @Override
    public List<AddressVO> getUserAddresses(String username) {
        List<Address> addresses = addressRepository.findAllByUsername(username);
        return addresses.stream().map(Address::toVO).collect(Collectors.toList());
    }

    /**
     * 新增收货地址
     * 如果是该用户的第一个地址，则自动设置为默认地址，否则设置为非默认
     * @param addressVO 地址视图对象
     * @return 保存后的地址视图对象
     */
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

    /**
     * 更新指定ID的地址信息
     * @param id 地址ID
     * @param addressVO 新的地址视图对象
     * @return 更新后的地址视图对象，地址不存在返回null
     */
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

        Address updated = addressRepository.save(address);
        return updated.toVO();
    }

    /**
     * 删除指定ID的地址
     * @param id 地址ID
     * @return 删除成功返回true，地址不存在返回false
     */
    @Override
    public boolean deleteAddress(Long id) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null) {
            addressRepository.deleteById(id);
            return true;
        }
        return false;
    }


    /**
     * 设置指定用户的默认地址
     * 该操作是事务性的，会先将该用户所有地址设为非默认，再将指定地址设为默认
     * @param username 用户名
     * @param id 需要设置为默认的地址ID
     * @return 设置成功返回true，失败返回false（如地址不存在或不属于该用户）
     */
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

    /**
     * 查询指定用户的默认地址
     * @param username 用户名
     * @return 默认地址视图对象，若无默认地址返回null
     */
    @Override
    public AddressVO getDefaultAddress(String username) {
        Address address = addressRepository.findFirstByUsernameAndDefaultAddressTrue(username);
        if (address == null) {
            return null;
        }
        return address.toVO();
    }
}

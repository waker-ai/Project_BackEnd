package com.example.tomatomall.vo;

import com.example.tomatomall.po.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressVO {
    private Long id;
    private String username;
    private String addresseeName;      // 收件人姓名
    private String phone;
    private String deliveryAddress;  // 详细地址
    private Boolean defaultAddress;  // 默认地址
    private String postalCode;       // 邮政编码


    public Address toPO() {
        Address po = new Address();
        po.setId(this.id); // 一般VO传给PO不包含id，除非修改场景需要
        po.setUsername(this.username);
        po.setAddresseeName(this.addresseeName);
        po.setPhone(this.phone);
        po.setDeliveryAddress(this.deliveryAddress);
        po.setDefaultAddress(this.defaultAddress);
        po.setPostalCode(this.postalCode);
        return po;
    }
}

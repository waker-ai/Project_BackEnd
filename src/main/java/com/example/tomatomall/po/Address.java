package com.example.tomatomall.po;

import com.example.tomatomall.vo.AddressVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id; // id

    @Column(name = "username")
    private String username; // 用户名

    @Column(name = "adresseename", nullable = false)
    private String addresseeName;//收件人姓名

    @Column(name = "phone", length = 11)
    private String phone;

    @Column(name = "delivery_address", nullable = false)
    private String  deliveryAddress;

    @Column(name = "default_address", nullable = false)
    private  Boolean  defaultAddress;

    @Column(name = "postal_code", nullable = false)
    private String  postalCode;


    public AddressVO toVO() {
        AddressVO vo = new AddressVO();
        vo.setId(this.id);
        vo.setUsername(this.username);
        vo.setAddresseeName(this.addresseeName);
        vo.setPhone(this.phone);
        vo.setDeliveryAddress(this.deliveryAddress);
        vo.setDefaultAddress(this.defaultAddress);
        vo.setPostalCode(this.postalCode);
        return vo;
    }

}

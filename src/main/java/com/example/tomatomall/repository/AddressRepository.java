package com.example.tomatomall.repository;

import com.example.tomatomall.po.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUsername(String username);

    Address findFirstByUsernameAndDefaultAddressTrue(String username);

    @Modifying
    @Transactional // 这一句很关键，如果你没加上，update 不会生效
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.username = :username")
    void resetDefaultAddress(@Param("username") String username);

}
package com.example.tomatomall.repository;

import com.example.tomatomall.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
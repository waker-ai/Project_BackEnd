package com.example.tomatomall.service;


import java.util.Map;

public interface AliPayable {
    boolean payNotify(Map<String, String> params);
}

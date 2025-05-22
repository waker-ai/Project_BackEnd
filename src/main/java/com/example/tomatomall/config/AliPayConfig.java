package com.example.tomatomall.config;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.service.serviceImpl.CartServiceImpl;
import lombok.Getter;
import lombok.Setter;

import org.apache.logging.log4j.util.StringBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

// 读取配置文件中的支付宝配置
@Component
@Getter
@Setter
public class AliPayConfig {
    private static final Logger logger = LoggerFactory.getLogger(AliPayConfig.class);

    @Value("${alipay.server-url}")
    private String serverUrl;
    @Value("${alipay.return-url}")
    private String returnUrl;
    @Value("${alipay.app-id}")
    private String appId;
    @Value("${alipay.private-key}")
    private String appPrivateKey;
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    @Value("http://nde26b89.natappfree.cc/api/members/notify")
    private String notifyUrl;

    private static String format = "json";

    private static String charset = "utf-8";

    private static String signType = "RSA2";

    /**
     *        使用支付宝沙箱
     *        使用时可以根据自己的需要做修改，包括参数名、返回值、具体实现
     *        在bizContent中放入关键的信息：tradeName、price、name
     *        返回的form是一个String类型的html页面
     */
    public String pay(JSONObject bizContent, Map<String, String> returnParams) {
//        logger.info("支付宝支付配置: " + serverUrl + " " + appId + " " + appPrivateKey + " " + alipayPublicKey + " " + notifyUrl + " " + returnUrl);
//        logger.info("支付请求 bizContent" + bizContent.toJSONString());
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, appPrivateKey, format, charset,
                alipayPublicKey, signType);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        request.setNotifyUrl(notifyUrl);
        logger.info("notifyUrl: " + notifyUrl);

        request.setReturnUrl(returnUrl);
//        logger.info("returnUrl: " + returnUrl);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        String form;
//        logger.info("支付宝 bizContent: " + bizContent.toJSONString());
        try {
            form = alipayClient.pageExecute(request).getBody();
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈
            System.err.println("支付宝 pageExecute 失败，原因：" + e.getMessage());
            throw TomatoException.payError();
        }
//        System.out.println("支付宝返回的 form: " + form);
        return form;
    }
}
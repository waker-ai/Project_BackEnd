package com.example.tomatomall.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.AliPayable;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.config.AliPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Map<String, AliPayable> notifyServiceList = new HashMap<>();
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    AliPayConfig alipayConfig;

    @PostConstruct
    public void init() {
        notifyServiceList.put("orderService",  orderService);
    }

    @PostMapping("/{orderId}/pay")
    public void pay(@PathVariable(value = "orderId") Long orderId, HttpServletResponse httpServletResponse) {
        orderService.pay(orderId, httpServletResponse);
    }


    @PostMapping("/notify")
    public void notify(@RequestParam(value = "service") String notifyService, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = httpServletRequest.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 2. 验证支付宝签名（防止伪造请求）
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),  // 从配置中读取沙箱公钥
                    "UTF-8",
                    "RSA2"
            );
        } catch (Exception e) {
            httpServletResponse.getWriter().print("fail");
            return;
        }

        if (!signVerified) {
            httpServletResponse.getWriter().print("fail");
            return;
        }
        if (httpServletRequest.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> new_params = new HashMap<>();
            Map<String, String[]> requestParams = httpServletRequest.getParameterMap();
            for (String name : requestParams.keySet()) {
                new_params.put(name, httpServletRequest.getParameter(name));
            }
            for (String pair : new_params.get("body").split(";")) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    new_params.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            AliPayable service = notifyServiceList.get(notifyService);
            if (service != null) {
                if(service.payNotify(new_params))
                {
                    httpServletResponse.getWriter().print("success");
                } else {
                    httpServletResponse.getWriter().print("fail");
                }
            } else {
                httpServletResponse.getWriter().print("fail");
            }
        }
        httpServletResponse.getWriter().print("fail");
    }

    @GetMapping("returnUrl")
    public void returnUrl(@RequestParam(value = "url") String url, HttpServletResponse httpServletResponse) {
        // visit url
        String filePath = "src/main/java/com.example/tomatomall/template/returnUrl.html";
        String html = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder fileContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            html = fileContent.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html = String.format(html, url);
        try {
            httpServletResponse.getWriter().write(html);// 直接将完整的表单html输出到页面
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
        } catch (IOException e) {
            throw TomatoException.payError();
        }
    }


}

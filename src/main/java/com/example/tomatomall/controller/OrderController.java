package com.example.tomatomall.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.AliPayable;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.config.AliPayConfig;
import com.example.tomatomall.service.serviceImpl.CartServiceImpl;
import com.example.tomatomall.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);


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
    public Response<Map<String, Object>> pay(@PathVariable(value = "orderId") Long orderId, HttpServletResponse httpServletResponse) {
        return Response.buildSuccess(orderService.pay(orderId, httpServletResponse));
    }


    @PostMapping("/notify")
    public void notify( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> Params = httpServletRequest.getParameterMap();
        for (String name : Params.keySet()) {
            String[] values = Params.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }
//        logger.info("支付宝回调参数：" + params);
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
            logger.error("签名验证失败", e);  // 记录详细的异常信息
            httpServletResponse.getWriter().print("fail");
            return;
        }

        if (!signVerified) {
            logger.info("支付宝回调参数：" + params);
            logger.info("支付宝公钥：" + alipayConfig.getAlipayPublicKey());
            logger.info("签名值：" + params.get("sign"));
            logger.info("签名类型：" + params.get("sign_type"));
            logger.error("支付宝签名验证失败, 请求参数: {}", params);
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
            AliPayable service = orderService; //这里把service写死了,之后可以进行拓展
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
        } else {
            logger.warn("支付宝支付状态不为成功: {}", httpServletRequest.getParameter("trade_status")); // 添加日志记录
            httpServletResponse.getWriter().print("fail");
        }
    }

    @GetMapping("/returnUrl")
    @ResponseBody
    public void returnUrl(HttpServletResponse httpServletResponse) {
        // 直接重定向到前端地址
        String url = "http://localhost:3000/#/cart"; // 或者你可以根据需要修改为动态URL
        try {
            // 发送重定向请求
            httpServletResponse.sendRedirect(url);
        } catch (IOException e) {
            logger.error("重定向失败", e);
            e.printStackTrace();
        }
        // visit url
//        String filePath = "src/main/java/com/example/tomatomall/template/returnUrl.html";
//        String html = "";
//        url = "http://localhost:3000/#/cart";
//        String filePath = url;
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            StringBuilder fileContent = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//                fileContent.append(line).append("\n");
//            }
//
//            html = fileContent.toString();
//        } catch (IOException e) {
//            logger.error("读取返回页面文件失败, 文件路径: {}", filePath, e);  // 记录详细错误
//            e.printStackTrace();
//        }
//
//        html = String.format(html, url);
//        try {
//            httpServletResponse.getWriter().write(html);// 直接将完整的表单html输出到页面
//            httpServletResponse.getWriter().flush();
//            httpServletResponse.getWriter().close();
//        } catch (IOException e) {
//            throw TomatoException.payError();
//        }
    }


}

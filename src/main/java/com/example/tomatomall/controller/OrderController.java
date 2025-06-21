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
import com.example.tomatomall.vo.OrderDetailVO;
import com.example.tomatomall.vo.OrderVO;
import java.util.List;

/**
 * 订单控制器，提供订单相关的接口
 * 包括获取订单详情、历史订单列表、订单支付、支付宝支付异步通知及支付完成重定向等
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);


    @Autowired
    private OrderService orderService;

    /**
     * 存放所有支持支付宝异步通知的服务，key为服务名称
     * 目前只注册了 orderService，方便未来扩展
     */
    private Map<String, AliPayable> notifyServiceList = new HashMap<>();

    @Autowired
    AliPayConfig alipayConfig;

    /**
     * 初始化方法，将订单服务加入支付宝通知处理服务列表
     */
    @PostConstruct
    public void init() {
        notifyServiceList.put("orderService",  orderService);
    }

    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 包含订单详情的Response对象
     */
    @GetMapping("/{orderId}")
    public Response<OrderDetailVO> get(@PathVariable(value = "orderId") Long orderId) {
        OrderDetailVO orderDetail = orderService.getOrderDetail(orderId);
        return Response.buildSuccess(orderDetail);
    }

    /**
     * 获取当前用户的历史订单列表
     * @return 订单视图列表的Response对象
     */
    @GetMapping("/history")
    public Response<List<OrderVO>> getHistoryOrders() {
        return Response.buildSuccess(orderService.getHistoryOrders());
    }

    /**
     * 触发订单支付流程，通常返回支付页面相关信息（如支付宝表单）
     * @param orderId 订单ID
     * @param httpServletResponse HTTP响应对象
     * @return 支付相关信息封装的Map
     */
    @PostMapping("/{orderId}/pay")
    public Response<Map<String, Object>> pay(@PathVariable(value = "orderId") Long orderId, HttpServletResponse httpServletResponse) {
        return Response.buildSuccess(orderService.pay(orderId, httpServletResponse));
    }

    /**
     * 支付宝支付异步通知接口
     * 负责接收支付宝服务器的支付结果异步回调，验证签名并通知业务服务进行订单状态更新
     *
     * @param httpServletRequest  HTTP请求对象，包含支付宝回调参数
     * @param httpServletResponse HTTP响应对象，用于返回处理结果
     * @throws IOException IO异常
     */
    @PostMapping("/notify")
    public void notify( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        logger.info("enter notify");
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
        logger.info("支付宝签名验证开始");
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
        logger.info("验证结果: " + signVerified);

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
            logger.info("orderService: " + orderService);
            if (service != null) {logger.info("prepare to call payNotify");
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

    /**
     * 支付完成后页面重定向接口
     * 支付完成后重定向到购物车或前端指定页面
     *
     * @param httpServletResponse HTTP响应对象，用于发送重定向
     */
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
    }
}

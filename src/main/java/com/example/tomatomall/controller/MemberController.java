
package com.example.tomatomall.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.tomatomall.config.AliPayConfig;
import com.example.tomatomall.po.Member;
import com.example.tomatomall.service.MemberService;
import com.example.tomatomall.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private AliPayConfig alipayConfig;

    @PostMapping("/charge")
    public Response<String> chargeMembership(@RequestParam String username,
                                               @RequestParam int months,
                                               @RequestParam BigDecimal amount,
                                             @RequestParam String membershipLevel
    ) {
        String paymentForm = memberService.createMembershipPayment(username, months, amount, membershipLevel);
        if (paymentForm != null) {
            return Response.buildSuccess(paymentForm);
        } else {
            return Response.buildFailure("Failed to create payment form","500");
        }
    }

    @GetMapping("/{username}")
    public Response<Member> getMember(@PathVariable String username) {
        Member member = memberService.getMemberByUsername(username);
        if(member == null) {
            return Response.buildFailure("Failed to get member","500");
        }
        return Response.buildSuccess(member);
    }
    @GetMapping("/checkmember")
    public Response<Boolean> checkMember(@RequestParam String username) {
        boolean isMember=memberService.isMember(username);
        return Response.buildSuccess(isMember);
    }

    @GetMapping("/list")
    public Response<List<Member>> getMemberList() {
        List<Member> members=memberService.getAllMembers();
        return Response.buildSuccess(members);
    }

    @PostMapping("/notify")
    public void notify(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {

        logger.info("进入会员支付回调");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = httpServletRequest.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }

        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );
        } catch (Exception e) {
            logger.error("签名验证失败", e);
            httpServletResponse.getWriter().print("fail");
            return;
        }

        if (!signVerified) {
            logger.error("支付宝签名验证失败, 请求参数: {}", params);
            httpServletResponse.getWriter().print("fail");
            return;
        }

        if ("TRADE_SUCCESS".equals(httpServletRequest.getParameter("trade_status"))) {
            String body = params.get("body");
            Map<String, String> bodyParams = parseBody(body);
            String username = bodyParams.get("username");
            int months = Integer.parseInt(bodyParams.get("months"));
            String memberLevel = bodyParams.get("level");

            boolean success = memberService.handleSuccessfulPayment(username, months,memberLevel);
            if (success) {
                httpServletResponse.getWriter().print("success");
            } else {
                httpServletResponse.getWriter().print("fail");
            }
        } else {
            logger.warn("支付宝支付状态不为成功: {}", httpServletRequest.getParameter("trade_status"));
            httpServletResponse.getWriter().print("fail");
        }
    }

    @GetMapping("/returnUrl")
    public void returnUrl(HttpServletResponse httpServletResponse) {
        String url = "http://localhost:3000/#/dashboard"; // 修改为会员页面的URL
        try {
            httpServletResponse.sendRedirect(url);
        } catch (IOException e) {
            logger.error("重定向失败", e);
        }
    }

    private Map<String, String> parseBody(String body) {
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split(";")) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return result;
    }
}
package com.example.tomatomall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class MemberControllerTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        token = obtainAccessToken();
    }

    /**
     * 登录并获取 token
     */
    private String obtainAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .param("username", "manager")
                        .param("password", "123456")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("data").asText(); // token 应在 data 字段
    }

    /**
     * 测试会员充值接口
     */
    @Test
    public void testChargeMembership() throws Exception {
        mockMvc.perform(post("/api/members/charge")
                        .param("username", "manager")
                        .param("months", "2")
                        .param("amount", "20.00")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 测试获取会员信息接口
     */
    @Test
    public void testGetMember() throws Exception {
        mockMvc.perform(get("/api/members/manager")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 模拟支付宝 notify 回调（注意：实际支付验证需跳过签名或 mock）
     */
    @Test
    public void testNotify() throws Exception {
        MockHttpServletRequestBuilder request = post("/api/members/notify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("trade_status", "TRADE_SUCCESS")
                .param("body", "username:manager;months:1");
        // 注意：真实环境应提供合法签名参数

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 测试支付宝 returnUrl 重定向
     */
    @Test
    public void testReturnUrl() throws Exception {
        mockMvc.perform(get("/api/members/returnUrl"))
                .andExpect(status().is3xxRedirection())
                .andDo(MockMvcResultHandlers.print());
    }
}

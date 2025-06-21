package com.example.tomatomall.service.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.tomatomall.config.AliPayConfig;
import com.example.tomatomall.po.Member;
import com.example.tomatomall.repository.MemberRepository;
import com.example.tomatomall.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员服务实现类，负责会员相关的业务逻辑处理
 */
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AliPayConfig aliPayConfig;

    /**
     * 生成会员充值支付表单
     * @param username 用户名
     * @param months 充值的会员时长（月）
     * @param amount 支付金额
     * @param level 会员等级
     * @return 支付表单HTML字符串（由支付宝SDK生成）
     */
    @Override
    public String createMembershipPayment(String username, int months, BigDecimal amount,String level) {
        // 生成订单号，这里简单使用时间戳加用户名
        String outTradeNo = System.currentTimeMillis() + "_" + username;

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", amount.toString());
        bizContent.put("subject", "会员充值 " + months + " 个月 -"+level +"级");
        bizContent.put("body", "username:" + username + ";months:" + months+";level:" + level);

        Map<String, String> returnParams = new HashMap<>();
        // 这里可以添加一些返回参数，如果需要的话

        try {
            String form = aliPayConfig.pay(bizContent, returnParams);
            // 这里可以保存订单信息到数据库
            return form;
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建或更新会员信息
     * @param username 用户名
     * @param months 会员有效期（月）
     * @param level 会员等级
     * @return 更新后的会员实体
     */
    @Override
    public Member createOrUpdateMembership(String username, int months,String level) {
        Member member = memberRepository.findByUsername(username)
                .orElse(new Member());

        if (member.getId() == null) {
            member.setUsername(username);
            member.setMembershipLevel(Member.MembershipLevel.valueOf(level.toUpperCase()));
            member.setStartDate(LocalDate.now());
        } else {
            member.setMembershipLevel(upgradeMembershipLevel(member.getMembershipLevel()));
        }

        member.setEndDate(LocalDate.now().plusMonths(months));
        member.setActive(true);

        return memberRepository.save(member);
    }

    @Override
    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElse(null); // 返回null而不是抛出异常
    }

    /**
     * 定时任务：每天凌晨检查并更新会员状态
     * 如果会员过期，将其状态设置为非激活
     */
    @Override
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void checkAndUpdateMembershipStatus() {
        List<Member> members = memberRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Member member : members) {
            if (member.getEndDate().isBefore(today)) {
                member.setActive(false);
                memberRepository.save(member);
            }
        }
    }

    /**
     * 升级会员等级的逻辑
     * @param currentLevel 当前会员等级
     * @return 升级后的会员等级
     */
    private Member.MembershipLevel upgradeMembershipLevel(Member.MembershipLevel currentLevel) {
        switch (currentLevel) {
            case BRONZE:
                return Member.MembershipLevel.SILVER;
            case SILVER:
                return Member.MembershipLevel.GOLD;
            case GOLD:
                return Member.MembershipLevel.PLATINUM;
            default:
                return Member.MembershipLevel.PLATINUM;
        }
    }

    /**
     * 处理支付成功后的业务逻辑
     * @param username 用户名
     * @param months 充值月份数
     * @param level 会员等级
     * @return 处理是否成功
     */
    @Override
    @Transactional
    public boolean handleSuccessfulPayment(String username, int months,String level) {
        try {
            Member member = createOrUpdateMembership(username, months, level);
            return member != null;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 判断某用户是否为活跃会员
     * @param username 用户名
     * @return 是否为活跃会员
     */
    @Override
    public boolean isMember(String username) {
        Member member = getMemberByUsername(username);
        return member != null&&member.isActive();
    }

    /**
     * 获取所有会员列表
     * @return 会员列表
     */
    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
}
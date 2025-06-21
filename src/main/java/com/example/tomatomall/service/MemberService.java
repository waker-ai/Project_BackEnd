
package com.example.tomatomall.service;

import com.example.tomatomall.po.Member;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员服务接口，定义与会员相关的业务操作
 */
public interface MemberService {

    /**
     * 创建或更新指定用户的会员信息。
     * 如果用户已经是会员，则延长会员时间；否则创建新的会员记录。
     *
     * @param username 用户名
     * @param months 开通/续费的月份数
     * @param level 会员等级（如 GOLD、SILVER 等）
     * @return 更新后的 Member 实体对象
     */
    Member createOrUpdateMembership(String username, int months,String level);

    /**
     * 根据用户名查询会员信息。
     *
     * @param username 用户名
     * @return 对应的会员对象，如果不是会员则返回 null
     */
    Member getMemberByUsername(String username);

    /**
     * 创建会员支付订单（如生成支付链接或二维码）
     *
     * @param username 用户名
     * @param months 购买的月份数
     * @param amount 支付金额
     * @param level 会员等级
     * @return 支付跳转地址或支付凭证等信息
     */
    String createMembershipPayment(String username, int months, BigDecimal amount,String level);

    /**
     * 处理会员支付成功后的回调逻辑：如更新会员状态和过期时间等。
     *
     * @param username 用户名
     * @param months 购买的月份数
     * @param level 会员等级
     * @return 成功返回 true，失败返回 false
     */
    boolean handleSuccessfulPayment(String username, int months,String level);

    /**
     * 判断用户是否为会员（且未过期）
     *
     * @param username 用户名
     * @return 是有效会员返回 true，否则返回 false
     */
    boolean isMember(String username);

    /**
     * 获取所有会员信息列表（用于后台管理）
     *
     * @return 所有会员的列表
     */
    List<Member> getAllMembers();
}

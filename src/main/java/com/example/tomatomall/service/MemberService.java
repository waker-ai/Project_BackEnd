// FILEPATH: E:/lab3后端/src/main/java/com/example/tomatomall/service/MemberService.java

package com.example.tomatomall.service;

import com.example.tomatomall.po.Member;

import java.math.BigDecimal;
import java.util.List;

public interface MemberService {
    Member createOrUpdateMembership(String username, int months,String level);
    Member getMemberByUsername(String username);
    void checkAndUpdateMembershipStatus();
    String createMembershipPayment(String username, int months, BigDecimal amount,String level);
    // FILEPATH: E:/lab3后端/src/main/java/com/example/tomatomall/service/MemberService.java

    boolean handleSuccessfulPayment(String username, int months,String level);
    boolean isMember(String username);
    List<Member> getAllMembers();
}

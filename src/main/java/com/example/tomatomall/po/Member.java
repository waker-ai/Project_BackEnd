// FILEPATH: E:/lab3后端/src/main/java/com/example/tomatomall/po/Member.java

package com.example.tomatomall.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@Entity
@Table(name = "membership")

public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private MembershipLevel membershipLevel;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isActive;

    // Getters and setters

    // 枚举类型定义会员等级
    public enum MembershipLevel {
        BRONZE(new BigDecimal("10")),
        SILVER(new BigDecimal("20")),
        GOLD(new BigDecimal("30")),
        PLATINUM(new BigDecimal("40"));

        private final BigDecimal price;

        MembershipLevel(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }

}
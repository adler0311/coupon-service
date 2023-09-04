package com.example.couponservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"customerId", "coupon_id"})
)
public class CustomerCoupon {
    @Id
    private UUID id;

    private Long customerId;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private LocalDateTime usedAt;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    public void use() {
        if (coupon.getUsageExpAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("기간이 만료된 쿠폰입니다");
        }

        this.usedAt = LocalDateTime.now();
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}

package com.example.couponservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CustomerCoupon {
    @Id
    private UUID id;

    private Long customerId;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private LocalDateTime usedAt;

    public void use() {
        this.usedAt = LocalDateTime.now();
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}

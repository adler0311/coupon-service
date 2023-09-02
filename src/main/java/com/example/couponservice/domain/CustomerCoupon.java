package com.example.couponservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}

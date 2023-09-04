package com.example.couponservice.service.dto;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
public class IssueCustomerCouponIn {
    @NonNull
    private Long couponId;
    @NonNull
    private Long userId;

    public CustomerCoupon toEntity(Coupon coupon) {
        return CustomerCoupon.builder()
                .id(UUID.randomUUID())
                .coupon(coupon)
                .customerId(userId)
                .issuedAt(LocalDateTime.now())
                .build();
    }

}

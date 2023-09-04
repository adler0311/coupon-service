package com.example.couponservice.service.dto;

import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.domain.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerCouponOut {
    private LocalDateTime usedAt;
    private String couponName;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageExpAt;

    private Integer discountAmount;

    private DiscountType discountType;

    private LocalDateTime issuedAt;


    public static CustomerCouponOut fromEntity(CustomerCoupon customerCoupon) {
        return CustomerCouponOut.builder()
                .couponName(customerCoupon.getCoupon().getName())
                .usedAt(customerCoupon.getUsedAt())
                .issuedAt(customerCoupon.getIssuedAt())
                .discountAmount(customerCoupon.getCoupon().getDiscountAmount())
                .usageExpAt(customerCoupon.getCoupon().getUsageExpAt())
                .usageStartAt(customerCoupon.getCoupon().getUsageStartAt())
                .discountType(customerCoupon.getCoupon().getDiscountType())
                .build();
    }
}

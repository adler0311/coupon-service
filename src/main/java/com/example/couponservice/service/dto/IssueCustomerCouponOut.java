package com.example.couponservice.service.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class IssueCustomerCouponOut {
    private UUID customerCouponId;
    public IssueCustomerCouponOut(UUID issuedCustomerCouponId) {
        customerCouponId = issuedCustomerCouponId;
    }
}

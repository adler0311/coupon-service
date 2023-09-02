package com.example.couponservice.service;

import com.example.couponservice.service.dto.IssueCustomerCouponIn;

import java.util.UUID;

public interface CustomerCouponService {
    UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn);
}

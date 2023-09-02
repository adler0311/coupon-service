package com.example.couponservice.service;

import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;

import java.util.UUID;

public interface CustomerCouponService {
    UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn);
    void useCustomerCoupon(UUID customerCouponId, UseCustomerCouponIn useCustomerCouponIn);
}

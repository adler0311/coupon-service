package com.example.couponservice.service;

import com.example.couponservice.service.dto.CustomerCouponOut;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerCouponService {
    UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn);
    void useCustomerCoupon(UUID customerCouponId, UseCustomerCouponIn useCustomerCouponIn);

    Page<CustomerCouponOut> getCustomerCoupons(Long customerId, Pageable pageable);
}

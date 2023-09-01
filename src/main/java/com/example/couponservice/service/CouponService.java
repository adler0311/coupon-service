package com.example.couponservice.service;

import com.example.couponservice.service.dto.CreateCouponIn;

public interface CouponService {

    Long createCoupon(CreateCouponIn createCouponIn);
}

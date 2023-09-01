package com.example.couponservice.controller;

import com.example.couponservice.service.CouponService;
import com.example.couponservice.service.dto.CreateCouponIn;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/coupons")
@RestController
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<Long> createCoupon(@Validated @RequestBody CreateCouponIn createCouponIn) {
        return ResponseEntity.ok(couponService.createCoupon(createCouponIn));
    }
}

package com.example.couponservice.service.exception;

public class CouponAlreadyIssued extends  Exception {
    public CouponAlreadyIssued(String message) {
        super(message);
    }
}

package com.example.couponservice.service.exception;

public class CouponOutOfStock extends Exception {
    public CouponOutOfStock() {
        super("재고가 부족합니다");
    }
}

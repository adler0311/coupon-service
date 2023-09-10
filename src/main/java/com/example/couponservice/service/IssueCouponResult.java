package com.example.couponservice.service;

public enum IssueCouponResult {
    SUCCESS("success"), OUT_OF_STOCK("outOfStock"), ISSUED_BEFORE("issuedBefore")
    ;

    IssueCouponResult(String v) {

    }
}

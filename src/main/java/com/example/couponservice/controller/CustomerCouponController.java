package com.example.couponservice.controller;

import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.IssueCustomerCouponOut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping("/customer-coupons")
@RestController
public class CustomerCouponController {

    private final CustomerCouponService customerCouponService;

    public CustomerCouponController(CustomerCouponService customerCouponService) {
        this.customerCouponService = customerCouponService;
    }


    @PostMapping
    public ResponseEntity<IssueCustomerCouponOut> issueCustomerCoupon(@RequestBody IssueCustomerCouponIn issueCustomerCouponIn) {
        UUID issuedCustomerCouponId = customerCouponService.issueCustomerCoupon(issueCustomerCouponIn);
        return ResponseEntity.ok(new IssueCustomerCouponOut(issuedCustomerCouponId));
    }

}

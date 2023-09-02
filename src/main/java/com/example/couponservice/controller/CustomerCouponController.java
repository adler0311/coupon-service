package com.example.couponservice.controller;

import com.example.couponservice.service.CustomerCouponService;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import com.example.couponservice.service.dto.IssueCustomerCouponOut;
import com.example.couponservice.service.dto.UseCustomerCouponIn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{customerCouponId}")
    public ResponseEntity<String> useCustomerCoupon(@PathVariable UUID customerCouponId, @RequestBody UseCustomerCouponIn useCustomerCouponIn) {
        customerCouponService.useCustomerCoupon(customerCouponId, useCustomerCouponIn);
        return ResponseEntity.ok().build();
    }

}

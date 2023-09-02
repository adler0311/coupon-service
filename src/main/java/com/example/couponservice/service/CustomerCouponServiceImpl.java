package com.example.couponservice.service;

import com.example.couponservice.domain.Coupon;
import com.example.couponservice.domain.CustomerCoupon;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CustomerCouponRepository;
import com.example.couponservice.service.dto.IssueCustomerCouponIn;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {
    private final CouponRepository couponRepository;
    private final CustomerCouponRepository customerCouponRepository;

    public CustomerCouponServiceImpl(CouponRepository couponRepository, CustomerCouponRepository customerCouponRepository) {
        this.couponRepository = couponRepository;
        this.customerCouponRepository = customerCouponRepository;
    }

    @Override
    public UUID issueCustomerCoupon(IssueCustomerCouponIn issueCustomerCouponIn) {
        Optional<Coupon> coupon = couponRepository.findById(issueCustomerCouponIn.getCouponId());
        if (coupon.isEmpty()) {
            throw new IllegalArgumentException();
        }

        CustomerCoupon newCustomerCoupon = customerCouponRepository.save(issueCustomerCouponIn.toEntity(coupon.get()));
        return newCustomerCoupon.getId();
    }
}
